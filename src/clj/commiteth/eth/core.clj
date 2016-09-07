(ns commiteth.eth.core
  (:require [clojure.data.json :as json]
            [org.httpkit.client :refer [post]]
            [clojure.java.io :as io]
            [overtone.at-at :refer [every mk-pool]]
            [commiteth.config :refer [env]]
            [commiteth.db.issues :as issues]
            [clojure.tools.logging :as log]
            [commiteth.github.core :as github]))

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
    (when-let [error (:error result)]
      (log/error "Method: " method ", error: " error))
    (:result result)))

(defn hex->big-integer
  [hex]
  (new BigInteger (subs hex 2) 16))

(defn from-wei
  [wei]
  (/ wei 1000000000000000000))

(defn get-balance-wei
  [account]
  (hex->big-integer (eth-rpc "eth_getBalance" [account "latest"])))

(defn get-balance-eth
  [account digits]
  (->> (get-balance-wei account) from-wei double (format (str "%." digits "f"))))

(defn send-transaction
  [from to value & [params]]
  (eth-rpc "personal_signAndSendTransaction" [(merge params {:from  from
                                                             :to    to
                                                             :value value})
                                              (eth-password)]))

(defn get-transaction-receipt
  [hash]
  (eth-rpc "eth_getTransactionReceipt" [hash]))

(defn deploy-contract
  []
  (let [contract-code (-> "contracts/wallet.data" io/resource slurp)]
    (send-transaction (eth-account) nil 1
      {:gas  1248650
       :data contract-code})))

;; @todo: move to another ns

(def pool (mk-pool))

(defn update-issue-contract-address []
  (for [{issue-id         :issue_id
         transaction-hash :transaction_hash} (issues/list-pending-deployments)]
    (when-let [receipt (get-transaction-receipt transaction-hash)]
      (log/info "transaction receipt for issue #" issue-id ": " receipt)
      (when-let [contract-address (:contractAddress receipt)]
        (let [issue   (issues/update-contract-address issue-id contract-address)
              {user         :login
               repo         :repo
               issue-number :issue_number} issue
              balance (get-balance-eth contract-address 4)]
          (github/post-comment user repo issue-number contract-address balance))))))

(every (* 5 60 1000) update-issue-contract-address pool)
