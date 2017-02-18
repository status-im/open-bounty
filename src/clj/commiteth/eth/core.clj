(ns commiteth.eth.core
  (:require [clojure.data.json :as json]
            [org.httpkit.client :refer [post]]
            [clojure.java.io :as io]
            [commiteth.config :refer [env]]
            [clojure.string :refer [join]]
            [clojure.tools.logging :as log]
            [clojure.string :as str]))

(def eth-rpc-url "http://localhost:8545")
(defn eth-account [] (:eth-account env))
(defn eth-password [] (:eth-password env))

(defn eth-rpc
  [method params]
  (let [body     (json/write-str {:jsonrpc "2.0"
                                  :method  method
                                  :params  params
                                  :id      1})
        options  {:body body}
        response (:body @(post eth-rpc-url options))
        result   (json/read-str response :key-fn keyword)]
    (log/debug body "\n" result)
    (when-let [error (:error result)]
      (log/error "Method: " method ", error: " error))
    (:result result)))

(defn estimate-gas
  [from to value & [params]]
  (eth-rpc "eth_estimateGas" [(merge params {:from  from
                                             :to    to
                                             :value value})]))

(defn hex->big-integer
  [hex]
  (new BigInteger (subs hex 2) 16))

(defn from-wei
  [wei]
  (/ wei 1000000000000000000))

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

(defn send-transaction
  [from to value & [params]]
  ;; todo: estimate gas instead of hardcoding
  (let [gas (format "0x%x" 2600000)
        args (merge params {:from  from
                            :value value
                            :gas   gas})]
    (eth-rpc
     "personal_sendTransaction"
     [(if (not (nil? to))
                                           (merge args {:to to})
                                           args)
                                         (eth-password)])))

(defn get-transaction-receipt
  [hash]
  (eth-rpc "eth_getTransactionReceipt" [hash]))

(defn- format-param
  [param]
  (if (number? param)
    (format "%064x" param)
    (clojure.string/replace (format "%64s" (subs param 2)) " " "0")))


(defn deploy-contract
  [owner]
  (let [contract-code (-> "contracts/wallet.data" io/resource slurp)
        owner1        (format-param (eth-account))
        owner2        (format-param owner)
        data          (str contract-code owner1 owner2)
        value         (format "0x%x" 1)]
    (send-transaction (eth-account) nil value {:data data})))

(defn- format-call-params
  [method-id & params]
  (let [params (join (map format-param params))]
    (str method-id params)))

(defn call
  [contract method-id & params]
  (let [data (apply format-call-params method-id params)]
    (eth-rpc "eth_call" [{:to contract :data data} "latest"])))

(defn execute
  [from contract method-id & params]
  (let [data (apply format-call-params method-id params)
        value (format "0x%x" 1)]
    (send-transaction from contract value {:data data})))
