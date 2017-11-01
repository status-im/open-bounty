(ns commiteth.scheduler
  (:require [commiteth.eth.core :as eth]
            [commiteth.eth.multisig-wallet :as multisig]
            [commiteth.eth.token-data :as token-data]
            [commiteth.github.core :as github]
            [commiteth.db.issues :as issues]
            [commiteth.db.bounties :as db-bounties]
            [commiteth.bounties :as bounties]
            [commiteth.util.crypto-fiat-value :as fiat-util]
            [commiteth.util.util :refer [eth-decimal->str]]
            [clojure.tools.logging :as log]
            [mount.core :as mount]
            [clj-time.core :as t]
            [clj-time.coerce :as time-coerce]
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
      (when-let [contract-address (multisig/find-created-multisig-address receipt)]
        (let [issue   (issues/update-contract-address issue-id contract-address)
              {owner        :owner
               repo         :repo
               comment-id   :comment_id
               issue-number :issue_number} issue
              balance-eth-str (eth/get-balance-eth contract-address 6)
              balance-eth (read-string balance-eth-str)]
          (bounties/update-bounty-comment-image issue-id
                                                owner
                                                repo
                                                issue-number
                                                contract-address
                                                balance-eth
                                                balance-eth-str
                                                {})
          (github/update-comment owner
                                 repo
                                 comment-id
                                 issue-number
                                 contract-address
                                 balance-eth
                                 balance-eth-str
                                 {}))))))


(defn deploy-contract [owner-address issue-id]
  (let [transaction-hash (multisig/deploy-multisig owner-address)]
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
           balance-eth :balance_eth
           tokens :tokens
           winner-login :winner_login} (db-bounties/pending-bounties)
          :let [value (eth/get-balance-hex contract-address)]]
    (if (empty? payout-address)
      (log/error "Cannot sign pending bounty - winner has no payout address")
      (let [execute-hash (multisig/send-all contract-address payout-address)]
        (log/info "Payout self-signed, called sign-all(" contract-address payout-address ") tx:" execute-hash)
        (db-bounties/update-execute-hash issue-id execute-hash)
        (github/update-merged-issue-comment owner
                                            repo
                                            comment-id
                                            contract-address
                                            (eth-decimal->str balance-eth)
                                            tokens
                                            winner-login)))))

(defn update-confirm-hash
  "Gets transaction receipt for each pending payout and updates DB confirm_hash with tranaction ID of commiteth bot account's confirmation."
  []
  (doseq [{issue-id     :issue_id
           execute-hash :execute_hash} (db-bounties/pending-payouts)]
    (log/info "pending payout:" execute-hash)
    (when-let [receipt (eth/get-transaction-receipt execute-hash)]
      (log/info "execution receipt for issue #" issue-id ": " receipt)
      (when-let [confirm-hash (multisig/find-confirmation-tx-id receipt)]
        (log/info "confirm hash:" confirm-hash)
        (db-bounties/update-confirm-hash issue-id confirm-hash)))))


(defn older-than-3h?
  [timestamp]
  (let [now (t/now)
        ts (time-coerce/from-date timestamp)
        diff (t/in-hours (t/interval ts now))]
    (println "hour diff:" diff)
    (> diff 3)))

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
           balance-eth :balance_eth
           tokens :tokens
           confirm-id :confirm_hash
           payee-login :payee_login
           updated :updated} (db-bounties/confirmed-payouts)]
    (log/debug "confirmed payout:" payout-hash)
    (if-let [receipt (eth/get-transaction-receipt payout-hash)]
      (let [tokens (multisig/token-balances contract-address)
            eth-balance (eth/get-balance-wei contract-address)]
        (if (or
             (some #(> (second %) 0.0) tokens)
             (> eth-balance 0))
          (do
            (log/info "Contract still has funds")
            (when (multisig/is-confirmed? contract-address confirm-id)
              (log/info "Detected bounty with funds and confirmed payout, calling executeTransaction")
              (let [execute-tx-hash (multisig/execute-tx contract-address confirm-id)]
                (log/info "execute tx:" execute-tx-hash))))

          (do
            (log/info "Payout has succeeded, saving payout receipt for issue #" issue-id ": " receipt)
            (db-bounties/update-payout-receipt issue-id receipt)
            (github/update-paid-issue-comment owner
                                              repo
                                              comment-id
                                              contract-address
                                              (eth-decimal->str balance-eth)
                                              tokens
                                              payee-login))))
      (when (older-than-3h? updated)
        (log/info "Resetting payout hash for issue" issue-id "as it has not been mined in 3h")
        (db-bounties/reset-payout-hash issue-id)))))

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

(defn update-bounty-token-balances
  "Helper function for updating internal ERC20 token balances to token multisig contract. Will be called periodically for all open bounty contracts."
  [bounty-addr]
  (doseq [[tla token-data] (token-data/as-map)]
    (let [balance (multisig/token-balance bounty-addr tla)]
      (when (> balance 0)
        (do
          (log/debug "bounty at" bounty-addr "has" balance "of token" tla)
          (let [internal-balance (multisig/token-balance-in-bounty bounty-addr tla)]
            (when (not= balance internal-balance)
              (log/info "balances not in sync, calling watch")
              (multisig/watch-token bounty-addr tla))))))))

(defn update-contract-internal-balances
  "It is required in our current smart contract to manually update it's internal balance when some tokens have been added."
  []
  (doseq [{bounty-address :contract_address}
          (db-bounties/open-bounty-contracts)]
    (update-bounty-token-balances bounty-address)))


(defn get-bounty-funds
  "Get funds in given bounty contract.
  Returns map of asset -> balance
   + key total-usd -> current total USD value for all funds"
  [bounty-addr]
  (let [token-balances (multisig/token-balances bounty-addr)
        eth-balance (read-string (eth/get-balance-eth bounty-addr 4))
        all-funds
        (merge token-balances
               {:ETH eth-balance})]
    (merge all-funds {:total-usd (fiat-util/bounty-usd-value all-funds)})))


(defn update-issue-usd-value
  [bounty-addr]
  (let [funds (get-bounty-funds bounty-addr)]
      (issues/update-usd-value bounty-addr
                               (:total-usd funds))))

(defn update-open-issue-usd-values
  "Sum up current USD values of all crypto assets in a bounty and store to DB"
  []
  (doseq [{bounty-addr :contract_address}
          (db-bounties/open-bounty-contracts)]
    (update-issue-usd-value bounty-addr)))

(defn update-balances
  []
  (doseq [{contract-address :contract_address
           owner            :owner
           repo             :repo
           comment-id       :comment_id
           issue-id         :issue_id
           db-balance-eth   :balance_eth
           db-tokens        :tokens
           issue-number     :issue_number} (db-bounties/open-bounty-contracts)]
    (when comment-id
      (let [balance-eth-str (eth/get-balance-eth contract-address 6)
            balance-eth (read-string balance-eth-str)
            token-balances (multisig/token-balances contract-address)]
        (log/debug "update-balances" balance-eth
                   balance-eth-str token-balances owner repo issue-number)

        (when (or
               (not (float= db-balance-eth balance-eth))
               (not= db-tokens token-balances))
          (log/debug "balances differ")
          (log/debug "ETH (db):" db-balance-eth (type db-balance-eth) )
          (log/debug "ETH (chain):" balance-eth (type balance-eth) )
          (log/debug "ETH cmp:" (float= db-balance-eth balance-eth))
          (log/debug "tokens (db):" db-tokens (type db-tokens) (type (:SNT db-tokens)))
          (log/debug "tokens (chain):" token-balances (type token-balances) (type (:SNT token-balances)))
          (log/debug "tokens cmp:" (= db-tokens token-balances))

          (issues/update-eth-balance contract-address balance-eth)
          (issues/update-token-balances contract-address token-balances)
          (bounties/update-bounty-comment-image issue-id
                                                owner
                                                repo
                                                issue-number
                                                contract-address
                                                balance-eth
                                                balance-eth-str
                                                token-balances)
          (github/update-comment owner
                                 repo
                                 comment-id
                                 issue-number
                                 contract-address
                                 balance-eth
                                 balance-eth-str
                                 token-balances)
          (update-issue-usd-value contract-address))))))


(defn wrap-in-try-catch [func]
  (try
    (catch Throwable t
      (log/error t))))

(defn run-tasks [tasks]
  (doall
   (map (fn [func] (wrap-in-try-catch (func)))
        tasks)))

(defn run-1-min-interval-tasks [time]
  (do
    (log/debug "run-1-min-interval-tasks" time)
    ;; TODO: disabled for now. looks like it may cause extraneus
    ;; contract deployments and costs
    (run-tasks
     [;;redeploy-failed-contracts
      deploy-pending-contracts
      update-issue-contract-address
      update-confirm-hash
      update-payout-receipt
      self-sign-bounty
      update-contract-internal-balances
      update-balances])
    (log/debug "run-1-min-interval-tasks done")))


(defn run-10-min-interval-tasks [time]
  (do
    (log/debug "run-1-min-interval-tasks" time)
    (run-tasks
     [update-open-issue-usd-values])
    (log/debug "run-10-min-interval-tasks done")))


(mount/defstate scheduler
  :start (let [every-minute (rest
                             (periodic-seq (t/now)
                                           (t/minutes 1)))
               every-10-minutes (rest
                                 (periodic-seq (t/now)
                                               (t/minutes 10)))
               error-handler (fn [e]
                               (log/error "Scheduled task failed" e)
                               (throw e))
               stop-fn (chime-at every-minute
                                 run-1-min-interval-tasks
                                 {:error-handler error-handler})
               stop-fn2 (chime-at every-10-minutes
                                  run-10-min-interval-tasks
                                  {:error-handler error-handler})]
           (log/info "started scheduler")
           (bounties/update-bounty-issue-titles)
           (fn [] (do (stop-fn) (stop-fn2))))
  :stop (do
          (log/info "stopping scheduler")
          (scheduler)))
