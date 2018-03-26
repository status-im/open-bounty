(ns commiteth.eth.core
  (:require [clojure.data.json :as json]
            [org.httpkit.client :refer [post]]
            [clojure.java.io :as io]
            [commiteth.config :refer [env]]
            [clojure.string :refer [join]]
            [taoensso.tufte :as tufte :refer (defnp p profiled profile)]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [mount.core :as mount]
            [pandect.core :as pandect]
            [commiteth.util.util :refer [json-api-request]])
  (:import [org.web3j
            protocol.Web3j
            protocol.http.HttpService
            protocol.core.DefaultBlockParameterName
            protocol.core.methods.response.EthGetTransactionCount
            protocol.core.methods.request.RawTransaction
            utils.Numeric
            crypto.Credentials
            crypto.TransactionEncoder
            crypto.WalletUtils]))

(defn eth-rpc-url [] (env :eth-rpc-url "http://localhost:8545"))
(defn eth-account [] (:eth-account env))
(defn eth-password [] (:eth-password env))
(defn gas-estimate-factor [] (env :gas-estimate-factor 1.0))
(defn auto-gas-price? [] (env :auto-gas-price false))
(defn offline-signing? [] (env :offline-signing true))

(def web3j-obj (atom nil))
(def creds-obj (atom nil))

(defn wallet-file-path []
  (env :eth-wallet-file))

(defn wallet-password []
  (env :eth-password))

(defn create-web3j []
 (or @web3j-obj
      (swap! web3j-obj (constantly (Web3j/build (HttpService. (eth-rpc-url)))))))

(defn creds []
  (or @creds-obj
      (let [password  (wallet-password)
            file-path (wallet-file-path)]
        (if (and password file-path)
          (swap! creds-obj
                 (constantly (WalletUtils/loadCredentials
                               password
                               file-path)))
          (throw (ex-info "Make sure you provided proper credentials in appropriate resources/config.edn"
                          {:password password :file-path file-path}))))))

(defn get-signed-tx [gas-price gas-limit to data]
  "Create a sign a raw transaction.
   'From' argument is not needed as it's already
   encoded in credentials.
   See https://web3j.readthedocs.io/en/latest/transactions.html#offline-transaction-signing"
  (let [web3j (create-web3j)
        nonce (.. (.ethGetTransactionCount web3j 
                                           (env :eth-account) 
                                           DefaultBlockParameterName/LATEST)
                  sendAsync
                  get
                  getTransactionCount)
        tx (RawTransaction/createTransaction
             nonce
             gas-price
             gas-limit
             to
             data)
        signed (TransactionEncoder/signMessage tx (creds))
        hex-string (Numeric/toHexString signed)]
    hex-string))
(defn eth-gasstation-gas-price
  []
  (let [data (json-api-request "https://ethgasstation.info/json/ethgasAPI.json")
        avg-price (-> (get data "average")
                      bigint)
        avg-price-gwei (/ avg-price (bigint 10))]
    (->> (* (bigint (Math/pow 10 9)) avg-price-gwei) ;; for some reason the API returns 10x gwei price
        .toBigInteger)))


(defn gas-price-from-config []
  (-> (:gas-price env 20000000000) ;; 20 gwei default
      str
      BigInteger.))

(defn gas-price
  []
  (if (auto-gas-price?)
    (try
      (eth-gasstation-gas-price)
      (catch Throwable t
        (log/error "Failed to get gas price with ethgasstation API" t)
        (gas-price-from-config)))
    (gas-price-from-config)))

(defn safe-read-str [s]
  (if (nil? s)
    (do
      (log/error "JSON response is nil")
      nil)
    (try
      (json/read-str s :key-fn keyword)
      (catch Exception ex
        (do (log/error "Exception when parsing json string:"
                       s
                       "message:"
                       ex)

            nil)))))

(defn eth-rpc
  [method params]
  (let [request-id (rand-int 4096)
        body     (json/write-str {:jsonrpc "2.0"
                                  :method  method
                                  :params  params
                                  :id      request-id})
        options  {:headers {"content-type" "application/json"}
                  :body body}
        response  @(post (eth-rpc-url) options)
        result   (safe-read-str (:body response))]
    (log/debug body "\n" result)

    (cond
      ;; Ignore any responses that have mismatching request ID
      (not= (:id result) request-id)
      (log/error "Geth returned an invalid json-rpc request ID, ignoring response")

      ;; If request ID matches but contains error, throw
      (:error result)
      (throw (ex-info "Error submitting transaction via eth-rpc" (:error result)))

      :else
      (:result result))))

(defn hex->big-integer
  [hex]
  (new BigInteger (subs hex 2) 16))

(defn integer->hex
  "Convert integer to 0x prefixed hex string. Works with native ints and BigInteger"
  [n]
  (str "0x" (.toString (BigInteger. (str n)) 16)))


(defn strip-decimal
  [s]
  (str/replace s #"\..*" ""))


(defn adjust-gas-estimate
  "Multiply given estimate by factor"
  [gas]
  (let [factor (gas-estimate-factor)]
    (if (= 1.0 factor)
      gas
      (let [adjust (fn [x] (+ x (* factor x)))]
        (-> gas
            hex->big-integer
            adjust
            strip-decimal
            read-string
            integer->hex)))))

(defn estimate-gas
  [from to value & [params]]
  (let [geth-estimate (eth-rpc
                       "eth_estimateGas" [(merge params {:from  from
                                                         :to    to
                                                         :value value})])
        adjusted-gas (adjust-gas-estimate geth-estimate)]

    (log/debug "estimated gas (geth):" geth-estimate)
    (log/debug "bumped estimate:" adjusted-gas)
    adjusted-gas))


(defn from-wei
  [wei]
  (/ wei 1000000000000000000))

(defn eth->wei
  [eth]
  (biginteger (* eth 1000000000000000000)))


(defn hex->eth
  [hex digits]
  (->> hex hex->big-integer from-wei double (format (str "%." digits "f"))))

(defn get-balance-hex
  [account]
  (eth-rpc "eth_getBalance" [account "latest"]))

(defn get-balance-wei
  [account]
  (hex->big-integer (get-balance-hex account)))

(defn get-balance-eth
  [account digits]
  (p :get-balance-eth
     (hex->eth (get-balance-hex account) digits)))

(defn- format-param
  [param]
  (if (number? param)
    (format "%064x" param)
    (clojure.string/replace (format "%64s" (subs param 2)) " " "0")))

(defn get-transaction-receipt
  [hash]
  (eth-rpc "eth_getTransactionReceipt" [hash]))

(defn format-call-params
  [method-id & params]
  (let [params (join (map format-param params))]
    (str method-id params)))

(defn call
  [contract method-id & params]
  (let [data (apply format-call-params method-id params)]
    (eth-rpc "eth_call" [{:to contract :data data} "latest"])))

(defn execute
  [from contract method-id gas-limit & params]
  (let [data (apply format-call-params method-id params)
        gas-price (gas-price)
        value (format "0x%x" 0)
        params (cond-> {:data data
                        :from  from
                        :value value}
                 gas-price
                 (merge {:gasPrice (integer->hex gas-price)})
                 contract
                 (merge {:to contract}))
        gas (if gas-limit gas-limit 
              (estimate-gas from contract value params))
        params (if (offline-signing?)
                 (get-signed-tx (biginteger gas-price)
                                      (hex->big-integer gas)
                                      contract
                                      data) 
                 (assoc params :gas gas))]
    (if (offline-signing?)
      (eth-rpc
        "eth_sendRawTransaction"
        [params])
      (eth-rpc
        "personal_sendTransaction"
        [params (eth-password)]))))

(defn hex-ch->num
  [ch]
  (read-string (str "0x" ch)))

(defn strip-0x
  [x]
  (str/replace x #"(?i)^0x" ""))

(defn upper-ch [ch]
  (first (str/upper-case ch)))

(defn lower-ch [ch]
  (first (str/lower-case ch)))

(defn sha3 [x]
  (pandect/keccak-256 x))


(defn sig->method-id [signature]
  (let [s (apply str (take 8 (sha3 signature)))]
    (str "0x" s)))

(defn event-sig->topic-id [signature]
  (let [s (sha3 signature)]
    (str "0x" s)))

(defn valid-address?
  "Validate given ethereum address. Checksum validation is performed
  and input is case-sensitive"
  ([address]
   (valid-address? address false))
  ([address checksum-validate?]
   (log/debug "valid-address?" address)
   ;; logic based on
   ;;  https://github.com/cilphex/ethereum-address/blob/master/index.js
   (and (boolean (re-matches #"(?i)^(0x)?[0-9a-f]{40}$" address))
        (if checksum-validate?
          (let [addr (strip-0x address)
               hash (sha3 (str/lower-case addr))]
           (log/debug "address hash" hash "addr" addr)
           (->>
            (map-indexed (fn [idx _]
                           (let [hash-ch-int (hex-ch->num (nth hash idx))
                                 ch (nth addr idx)
                                 ch-lower (lower-ch ch)
                                 ch-upper (upper-ch ch)]
                             (or (and (> hash-ch-int 7)
                                      (not= ch-upper ch))
                                 (and (<= hash-ch-int 7)
                                      (not= ch-lower ch)))))
                         addr)
            (filter true?)
            (empty?)))
          true))))

(mount/defstate
  eth-core
  :start
  (do
    (swap! web3j-obj (constantly nil))
    (swap! creds-obj (constantly nil))
    (log/info "eth/core started"))
  :stop
  (log/info "eth/core stopped"))


