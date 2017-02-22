(ns commiteth.scheduler
  (:require [commiteth.eth.core :as eth]
            [commiteth.eth.multisig-wallet :as wallet]
            [commiteth.github.core :as github]
            [commiteth.db.issues :as issues]
            [commiteth.db.bounties :as db-bounties]
            [commiteth.bounties :as bounties]
            [clojure.tools.logging :as log]
            [mount.core :as mount])
  (:import [sun.misc ThreadGroupUtils]
           [java.lang.management ManagementFactory]))

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
              {owner        :login
               repo         :repo
               issue-number :issue_number} issue
              balance (eth/get-balance-eth contract-address 4)
              issue-url (str owner "/" repo "/issues/" (str issue-number))]
          (bounties/update-bounty-comment-image issue-id
                                                issue-url
                                                contract-address
                                                balance)
          (->> (github/post-comment owner
                                    repo
                                    issue-number
                                    contract-address
                                    balance)
               :id
               (issues/update-comment-id issue-id)))))))

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

(defn update-payout-hash
  "Gets transaction receipt for each confirmed payout and updates payout_hash"
  []
  (doseq [{issue-id    :issue_id
           payout-hash :payout_hash} (db-bounties/confirmed-payouts-list)]
    (log/debug "confirmed payout:" payout-hash)
    (when-let [receipt (eth/get-transaction-receipt payout-hash)]
      (log/info "payout receipt for issue #" issue-id ": " receipt)
      (db-bounties/update-payout-receipt issue-id receipt))))

(defn update-balance
  []
  (doseq [{contract-address :contract_address
           owner            :login
           repo             :repo
           comment-id       :comment_id
           issue-id         :issue_id
           old-balance      :balance
           issue-number     :issue_number} (db-bounties/list-wallets)]
    (when comment-id
      (let [current-balance-hex (eth/get-balance-hex contract-address)
            current-balance-eth (eth/hex->eth current-balance-hex 8)
            issue-url (str owner "/" repo "/issues/" (str issue-number))]
        (when-not (= old-balance current-balance-hex)
          (issues/update-balance contract-address current-balance-hex)
          (bounties/update-bounty-comment-image issue-id
                                                issue-url
                                                contract-address
                                                current-balance-eth)
          (github/update-comment owner
                                 repo
                                 comment-id
                                 issue-number
                                 contract-address
                                 current-balance-eth))))))

(def scheduler-thread-name "SCHEDULER_THREAD")

(defn get-thread-by-name
  [name]
  (let [root          (ThreadGroupUtils/getRootThreadGroup)
        threads-count (.getThreadCount (ManagementFactory/getThreadMXBean))
        threads       ^"[Ljava.lang.Thread;" (make-array Thread threads-count)]
    (.enumerate root threads true)
    (first (filter #(= name (.getName %)) threads))))

(defn every
  [ms tasks]
  (.start (new Thread
            (fn []
              (while (not (.isInterrupted (Thread/currentThread)))
                (do (try
                      (Thread/sleep ms)
                      (catch InterruptedException _
                        (.interrupt (Thread/currentThread))))
                    (doseq [task tasks]
                      (try (task)
                           (catch Exception e (log/error e)))))))
            scheduler-thread-name)))

(defn stop-scheduler []
  (when-let [scheduler (get-thread-by-name scheduler-thread-name)]
    (log/debug "Stopping scheduler thread")
    (.interrupt scheduler)))

(defn restart-scheduler [ms tasks]
  (stop-scheduler)
  (log/debug "Starting scheduler thread")
  (while (get-thread-by-name scheduler-thread-name)
    (log/debug "Waiting")
    (Thread/sleep 1))
  (every ms tasks))

(mount/defstate scheduler
  :start (restart-scheduler 60000
           [update-issue-contract-address
            update-confirm-hash
            update-payout-hash
            self-sign-bounty
            update-balance])
  :stop (stop-scheduler))
