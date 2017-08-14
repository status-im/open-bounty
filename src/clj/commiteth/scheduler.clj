(ns commiteth.scheduler
  (:require [commiteth.eth.core :as eth]
            [commiteth.eth.multisig-wallet :as wallet]
            [commiteth.github.core :as github]
            [commiteth.db.issues :as issues]
            [commiteth.db.bounties :as db-bounties]
            [commiteth.bounties :as bounties]
            [commiteth.util.util :refer [decimal->str]]
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
              balance-str (eth/get-balance-eth contract-address 6)
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


(defn deploy-contract [owner-address issue-id]
  (let [transaction-hash (eth/deploy-contract owner-address)]
      (if (nil? transaction-hash)
        (log/error "Failed to deploy contract to" owner-address)
        (log/info "Contract deployed, transaction-hash:"
                  transaction-hash ))
      (issues/update-transaction-hash issue-id transaction-hash)))


(defn redeploy-failed-contracts
  "If the bot account runs out of gas, we end up with transaction-id in db, but with nothing written to blockchain. In this case we should try to re-deploy the contract."
  []
  (doseq [{issue-id :issue_id
           transaction-hash :transaction_hash
           owner-address :owner_address} (issues/list-failed-deployments)]
    (when (nil? (eth/get-transaction-receipt transaction-hash))
      (log/info "Detected nil transaction receipt for pending contract deployment for issue" issue-id ", re-deploying contract")
      (deploy-contract owner-address issue-id))))


(defn deploy-pending-contracts
  "Under high-concurrency circumstances or in case geth is in defunct state, a bounty contract may not deploy successfully when the bounty label is addded to an issue. This function deploys such contracts."
  []
  (doseq [{issue-id :issue_id
           owner-address :owner_address} (db-bounties/pending-contracts)]
    (log/debug "Trying to re-deploy failed bounty contract deployment, issue-id:" issue-id)
    (deploy-contract owner-address issue-id)))

(defn self-sign-bounty
  "Walks through all issues eligible for bounty payout and signs corresponding transaction"
  []
  (doseq [{contract-address :contract_address
           issue-id         :issue_id
           payout-address   :payout_address
           repo :repo
           owner :owner
           comment-id :comment_id
           issue-number :issue_number
           balance :balance
           winner-login :winner_login} (db-bounties/pending-bounties)
          :let [value (eth/get-balance-hex contract-address)]]
    (if (empty? payout-address)
      (log/error "Cannot sign pending bounty - winner has no payout address")
      (let [execute-hash (wallet/send-all contract-address payout-address)]
        (db-bounties/update-execute-hash issue-id execute-hash)
        (github/update-merged-issue-comment owner
                                            repo
                                            comment-id
                                            contract-address
                                            (decimal->str balance)
                                            winner-login)))))

(defn update-confirm-hash
  "Gets transaction receipt for each pending payout and updates confirm_hash"
  []
  (doseq [{issue-id     :issue_id
           execute-hash :execute_hash} (db-bounties/pending-payouts)]
    (log/debug "pending payout:" execute-hash)
    (when-let [receipt (eth/get-transaction-receipt execute-hash)]
      (log/info "execution receipt for issue #" issue-id ": " receipt)
      (when-let [confirm-hash (wallet/find-confirmation-hash receipt)]
        (db-bounties/update-confirm-hash issue-id confirm-hash)))))

(defn update-payout-receipt
  "Gets transaction receipt for each confirmed payout and updates payout_hash"
  []
  (doseq [{issue-id    :issue_id
           payout-hash :payout_hash
           contract-address :contract_address
           repo :repo
           owner :owner
           comment-id :comment_id
           issue-number :issue_number
           balance :balance
           payee-login :payee_login} (db-bounties/confirmed-payouts)]
    (log/debug "confirmed payout:" payout-hash)
    (when-let [receipt (eth/get-transaction-receipt payout-hash)]
      (log/info "payout receipt for issue #" issue-id ": " receipt)
      (db-bounties/update-payout-receipt issue-id receipt)
      (github/update-paid-issue-comment owner
                                        repo
                                        comment-id
                                        contract-address
                                        (decimal->str balance)
                                        payee-login))))


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
      (let [current-balance-eth-str (eth/get-balance-eth contract-address 6)
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
    ;; TODO: disabled for now. looks like it may cause extraneus
    ;; contract deployments and costs
    #_(redeploy-failed-contracts)
    (deploy-pending-contracts)
    (update-issue-contract-address)
    (update-confirm-hash)
    (update-payout-receipt)
    (self-sign-bounty)
    (update-balances)
    (log/debug "run-periodic-tasks done")))


(mount/defstate scheduler
  :start (let [every-minute (rest
                             (periodic-seq (t/now)
                                           (t/minutes 1)))
               stop-fn (chime-at every-minute
                                 run-periodic-tasks
                                 {:error-handler (fn [e]
                                                   (log/error "Scheduled task failed" e)
                                                   (throw e))})]
           (log/info "started scheduler")
           (bounties/update-bounty-issue-titles)
           stop-fn)
  :stop (do
          (log/info "stopping scheduler")
          (scheduler)))
