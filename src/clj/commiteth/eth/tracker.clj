(ns commiteth.eth.tracker
  (:require [clojure.data.json :as json]
            [org.httpkit.client :refer [post]]
            [commiteth.db.issues :as issues]
            [commiteth.config :refer [env]]
            [commiteth.eth.web3j :refer [web3j-obj]]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [clj-time.core :as t])
  (:import [org.web3j
            protocol.Web3j
            protocol.http.HttpService
            protocol.core.DefaultBlockParameterName]))

(defn eth-rpc-url [] (env :eth-rpc-url "http://localhost:8545"))

(defn get-nonce []
  (.. (.ethGetTransactionCount @web3j-obj 
                               (env :eth-account) 
                               DefaultBlockParameterName/PENDING)
      sendAsync
      get
      getTransactionCount))

(defprotocol ITxTracker
  (try-reserve-nonce [this]
    "Fetch current nonce via eth_getTransactionCount
    and either return it if it is not in use yet,
    or throw an exception")
  (drop-nonce [this nonce]
    "If tx execution returned with an error,
    release previously reserved nonce")
  (track-tx [this tx-info]
    "Record tx data after successful submission")
  (untrack-tx [this tx-info]
    "Mark tx as successfully mined")
  (prune-txs [this txs]
    "Release nonces related to txs param
    and the ones that have expired (haven't been mined after a certain timeout)")
  )

(defrecord SequentialTxTracker [current-tx]
  ITxTracker
  (try-reserve-nonce [this]
    (let [nonce (get-nonce)]
      (if (or (nil? @current-tx)
              (> nonce (:nonce @current-tx)))
        (:nonce (reset! current-tx {:nonce nonce}))
        (throw (Exception. (str "Attempting to re-use old nonce" nonce))))))
  (drop-nonce [this nonce]
    (reset! current-tx nil))
  (track-tx [this tx-info]
    (reset! current-tx tx-info))
  (untrack-tx [this tx-info]
    (when (= (:nonce tx-info) (:nonce @current-tx))
      (reset! current-tx nil)))
  (prune-txs [this unmined-txs]
    (when (or ((set (map :tx-hash unmined-txs)) (:tx-hash @current-tx))
            (and (:timestamp @current-tx)
               (t/before? (:timestamp @current-tx) (t/minus (t/now) (t/minutes 30)))))
      (log/errorf "Current nonce unmined for 30 minutes, force reset. Tx hash: %s, type: %s" 
                  (:tx-hash @current-tx) (:type @current-tx))
      (reset! current-tx nil)))

  ) 

(defrecord ParallelTxTracker [current-txs]
  ITxTracker
  (try-reserve-nonce [this]
    (let [nonce (get-nonce)
          monitored-nonces (set (keys @current-txs))
          first-available-nonce (some #(if (monitored-nonces %1) nil %1) (iterate inc nonce))]
      (swap! current-txs assoc first-available-nonce nil)
      first-available-nonce))

  (drop-nonce [this nonce]
    (swap! current-txs dissoc nonce))

  (track-tx [this tx-info]
    (swap! current-txs update (:nonce tx-info) merge tx-info))

  (untrack-tx [this tx-info]
    (when (contains? (set (keys @current-txs)) (:nonce tx-info))
      (swap! current-txs dissoc (:nonce tx-info))))

  (prune-txs [this unmined-txs]
    (swap! current-txs
           (fn [txs]
             (let [unmined-tx-hashes (set (map :tx-hash unmined-txs))
                   time-threshold (t/minus (t/now) (t/minutes 10))
                   nonces-to-remove
                   (->> txs
                        vals
                        (filter #(or (unmined-tx-hashes (:tx-hash %1))
                                     (and (:timestamp %1)
                                          (t/before? (:timestamp %1) time-threshold))))
                        (map :nonce))]
               (apply dissoc txs nonces-to-remove)))))
  )


(def tx-tracker (ParallelTxTracker. (atom nil)))

(defn try-reserve-nonce! []
  (try-reserve-nonce tx-tracker))

(defn drop-nonce! [nonce]
  (drop-nonce tx-tracker nonce))

(defn track-tx! 
  "Store tx data in tx-tracker and DB"
  [{:keys [issue-id tx-hash type]
                  :as tx-info}]
  (track-tx tx-tracker tx-info)
  (issues/save-tx-info! issue-id tx-hash type))
  
(defn untrack-tx! 
  "Mark tx data stored in tx-tracker and DB as successfully mined"
  [{:keys [issue-id result type]
                    :as tx-info}]
  (untrack-tx tx-tracker tx-info)
  (issues/save-tx-result! issue-id result type))

(defn prune-txs! [unmined-txs]
  "Release nonces related to unmined txs,
   and set relevant DB fields to null thereby
   marking them as candidates for re-execution"
  (doseq [{issue-id :issue_id
           tx-hash :tx_hash
           type :type} unmined-txs]
    (log/infof "issue %s: resetting tx operation: %s for hash: %s" issue-id type tx-hash)
    (issues/save-tx-info! issue-id nil type))
  (prune-txs tx-tracker unmined-txs))

