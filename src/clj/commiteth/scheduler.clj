(ns commiteth.scheduler
  (:require [commiteth.eth.core :as eth]
            [commiteth.eth.multisig-wallet :as wallet]
            [commiteth.github.core :as github]
            [commiteth.db.issues :as issues]
            [commiteth.db.bounties :as db-bounties]
            [commiteth.bounties :as bounties]
            [clojure.tools.logging :as log]
            [mount.core :as mount]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [chime :refer [chime-at]]))

(defn update-issue-contract-address
  "For each pending deployment: gets transaction receipt, updates db
  state (contract-address, comment-id) and posts github comment"
  []
  (doseq [{issue-id         :issue_id
           transaction-hash :transaction_hash} (issues/list-pending-deployments)]
    (log/debug "pending deployment:" transaction-hash)
    (when-let [receipt (eth/get-transaction-receipt transaction-hash)]
      (log/info "transaction receipt for issue #" issue-id ": " receipt)
      (when-let [contract-address (:contractAddress receipt)]
        (let [issue   (issues/update-contract-address issue-id contract-address)
              {owner        :owner
               repo         :repo
               comment-id   :comment_id
               issue-number :issue_number} issue
              balance-str (eth/get-balance-eth contract-address 8)
              balance (read-string balance-str)]
          (bounties/update-bounty-comment-image issue-id
                                                owner
                                                repo
                                                issue-number
                                                contract-address
                                                balance
                                                balance-str)
          (github/update-comment owner
                                 repo
                                 comment-id
                                 issue-number
                                 contract-address
                                 balance
                                 balance-str))))))

(defn self-sign-bounty
  "Walks through all issues eligible for bounty payout and signs corresponding transaction"
  []
  (doseq [{contract-address :contract_address
           issue-id         :issue_id
           payout-address   :payout_address} (db-bounties/pending-bounties-list)
          :let [value (eth/get-balance-hex contract-address)]]
    (->>
     (wallet/execute contract-address payout-address value)
     (db-bounties/update-execute-hash issue-id))))

(defn update-confirm-hash
  "Gets transaction receipt for each pending payout and updates confirm_hash"
  []
  (doseq [{issue-id     :issue_id
           execute-hash :execute_hash} (db-bounties/pending-payouts-list)]
    (log/debug "pending payout:" execute-hash)
    (when-let [receipt (eth/get-transaction-receipt execute-hash)]
      (log/info "execution receipt for issue #" issue-id ": " receipt)
      (when-let [confirm-hash (wallet/find-confirmation-hash receipt)]
        (db-bounties/update-confirm-hash issue-id confirm-hash)))))

(defn update-payout-receipt
  "Gets transaction receipt for each confirmed payout and updates payout_hash"
  []
  (doseq [{issue-id    :issue_id
           payout-hash :payout_hash} (db-bounties/confirmed-payouts-list)]
    (log/debug "confirmed payout:" payout-hash)
    (when-let [receipt (eth/get-transaction-receipt payout-hash)]
      (log/info "payout receipt for issue #" issue-id ": " receipt)
      ;;TODO: not sure if saving the transaction-receipt clojure map as
      ;; a string is a good idea
      (db-bounties/update-payout-receipt issue-id (str receipt)))))


(defn abs
  "(abs n) is the absolute value of n"
  [n]
  (cond
    (not (number? n)) (throw (IllegalArgumentException.
                              "abs requires a number"))
    (neg? n) (- n)
    :else n))

(defn float=
  ([x y] (float= x y 0.0000001))
  ([x y epsilon]
   (log/debug x y epsilon)
   (let [scale (if (or (zero? x) (zero? y)) 1 (abs x))]
     (<= (abs (- x y)) (* scale epsilon)))))

(defn update-balances
  []
  (doseq [{contract-address :contract_address
           owner            :owner
           repo             :repo
           comment-id       :comment_id
           issue-id         :issue_id
           old-balance      :balance
           issue-number     :issue_number} (db-bounties/open-bounty-contracts)]
    (when comment-id
      (let [current-balance-eth-str (eth/get-balance-eth contract-address 8)
            current-balance-eth (read-string current-balance-eth-str)]
        (log/debug "update-balances" current-balance-eth
                   current-balance-eth-str owner repo issue-number)
        (when-not (float= old-balance current-balance-eth)
          (log/debug "balances differ")
          (issues/update-balance contract-address current-balance-eth)
          (bounties/update-bounty-comment-image issue-id
                                                owner
                                                repo
                                                issue-number
                                                contract-address
                                                current-balance-eth
                                                current-balance-eth-str)
          (github/update-comment owner
                                 repo
                                 comment-id
                                 issue-number
                                 contract-address
                                 current-balance-eth
                                 current-balance-eth-str))))))


(defn run-periodic-tasks [time]
  (do
    (log/debug "run-periodic-tasks" time)
    (update-issue-contract-address)
    (update-confirm-hash)
    (update-payout-receipt)
    (self-sign-bounty)
    (update-balances)
    (log/debug "run-periodic-tasks done")))


(mount/defstate scheduler
  :start (let [every-minute (rest
                             (periodic-seq (t/now)
                                           (-> 1 t/minutes)))
               stop-fn (chime-at every-minute
                                 run-periodic-tasks
                                 {:error-handler (fn [e]
                                                   (log/error "Scheduled task failed" e)
                                                   (throw e))})]
           (log/info "started scheduler")
           stop-fn)
  :stop (do
          (log/info "stopping scheduler")
          (scheduler)))
