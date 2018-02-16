(ns commiteth.eth.core
  (:require [clojure.data.json :as json]
            [org.httpkit.client :refer [post]]
            [clojure.java.io :as io]
            [commiteth.config :refer [env]]
            [commiteth.eth.web3j :as web3j]
            [clojure.string :refer [join]]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [pandect.core :as pandect]
            [commiteth.util.util :refer [json-api-request]]))

(defn eth-rpc-url [] (env :eth-rpc-url "http://localhost:8545"))
(defn eth-account [] (:eth-account env))
(defn eth-password [] (:eth-password env))
(defn gas-estimate-factor [] (env :gas-estimate-factor 1.0))
(defn auto-gas-price? [] (env :auto-gas-price false))
(defn offline-signing? [] (env :offline-signing false))

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
        _ (log/info "eth-rpc method:" method)
        _ (log/info "eth-rpc response:" response)
        result   (json/read-str (:body response) :key-fn keyword)]
    (log/debug body "\n" result)

    (if (= (:id result) request-id)
      (:result result)
      (do
        (log/error "Geth returned an invalid json-rpc request ID,"
                   "ignoring response")
        (when-let [error (:error result)]
          (log/error "Method: " method ", error: " error))))))

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
  (hex->eth (get-balance-hex account) digits))

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
                 (web3j/get-signed-tx (biginteger gas-price)
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
