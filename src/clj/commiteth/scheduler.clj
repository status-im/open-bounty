(ns commiteth.scheduler
  (:require [commiteth.eth.core :as eth]
            [commiteth.eth.multisig-wallet :as multisig]
            [commiteth.eth.token-data :as token-data]
            [commiteth.github.core :as github]
            [commiteth.db.issues :as issues]
            [commiteth.util.util :refer [to-map]]
            [taoensso.tufte :as tufte :refer (defnp p profiled profile)]
            [commiteth.db.bounties :as db-bounties]
            [commiteth.bounties :as bounties]
            [commiteth.util.crypto-fiat-value :as fiat-util]
            [commiteth.util.util :refer [eth-decimal->str]]
            [clojure.tools.logging :as log]
            [mount.core :as mount]
            [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.coerce :as time-coerce]
            [clj-time.periodic :refer [periodic-seq]]
            [chime :refer [chime-at]]))

(tufte/add-basic-println-handler! {})
(tufte/add-handler! :file (fn [{stats :stats}]
                            (log/info "Profiling stats:" stats)))

(comment

  (profile {} (update-issue-contract-address))
  (profile {} (deploy-pending-contracts))
  (profile {} (self-sign-bounty))
  (profile {} (update-confirm-hash))
  (profile {} (update-watch-hash))
  (profile {} (update-payout-receipt))
  (profile {} (update-contract-internal-balances))
  (profile {} (update-balances))
  (profile {}
           (doseq [i (range 5)]
             (update-contract-internal-balances) 
             (update-balances)))

  )
(defn update-issue-contract-address
  "For each pending deployment: gets transaction receipt, updates db
  state (contract-address, comment-id) and posts github comment"
  []
  (log/info "In update-issue-contract-address")
  (p :update-issue-contract-address
     (doseq [{:keys [issue-id transaction-hash]} (issues/list-pending-deployments)]
    (log/info "pending deployment:" transaction-hash)
    (try 
      (when-let [receipt (eth/get-transaction-receipt transaction-hash)]
        (log/info "update-issue-contract-address: transaction receipt for issue #"
                  issue-id ": " receipt)
        (if-let [contract-address (multisig/find-created-multisig-address receipt)]
          (let [{:keys [owner repo comment-id issue-number] :as issue}
                (issues/update-contract-address issue-id contract-address)
                balance-eth-str (eth/get-balance-eth contract-address 6)
                balance-eth (read-string balance-eth-str)
                tokens {}]
            (log/info "Updating comment")
            (github/update-comment (to-map issue-id
                                           owner
                                           repo
                                           comment-id
                                           issue-number
                                           contract-address
                                           balance-eth
                                           balance-eth-str
                                           tokens)))
          (log/error "Failed to find contract address in tx logs")))
      (catch Throwable ex 
        (do (log/error "update-issue-contract-address exception:" ex)
            (clojure.stacktrace/print-stack-trace ex))))))
  (log/info "Exit update-issue-contract-address"))


(defn deploy-pending-contracts
  "Under high-concurrency circumstances or in case geth is in defunct state, a bounty contract may not deploy successfully when the bounty label is addded to an issue. This function deploys such contracts."
  []
    (p :deploy-pending-contracts
  (doseq [{:keys [issue-id issue-number owner owner-address repo]} 
          (db-bounties/pending-contracts)]
    (log/debug "Trying to re-deploy failed bounty contract deployment, issue-id:" issue-id)
    (bounties/deploy-contract owner owner-address repo issue-id issue-number))))

(defn self-sign-bounty
  "Walks through all issues eligible for bounty payout and signs corresponding transaction"
  []
  (log/info "In self-sign-bounty")
  (p :self-sign-bounty
     (doseq [{:keys [contract-address winner-address issue-id winner-login] :as issue}
             (db-bounties/pending-bounties)]
       (try
         (let [value (eth/get-balance-hex contract-address)]
           (when-not (empty? winner-address)
             (let [execute-hash (multisig/send-all contract-address winner-address)]
               (log/info "Payout self-signed, called sign-all(" contract-address winner-address ") tx:" execute-hash)
               (db-bounties/update-execute-hash-and-winner-login issue-id execute-hash winner-login)))
           (github/update-comment issue)) 
         (catch Throwable ex 
           (do (log/error "self-sign-bounty exception:" ex)
               (clojure.stacktrace/print-stack-trace ex))))))
  (log/info "Exit self-sign-bounty"))

(defn update-confirm-hash
  "Gets transaction receipt for each pending payout and updates DB confirm_hash with tranaction ID of commiteth bot account's confirmation."
  []
  (log/info "In update-confirm-hash")
  (p :update-confirm-hash
     (doseq [{:keys [issue-id execute-hash]} (db-bounties/pending-payouts)]
    (log/info "pending payout:" execute-hash)
    (when-let [receipt (eth/get-transaction-receipt execute-hash)]
      (log/info "execution receipt for issue #" issue-id ": " receipt)
      (when-let [confirm-hash (multisig/find-confirmation-tx-id receipt)]
        (log/info "confirm hash:" confirm-hash)
        (db-bounties/update-confirm-hash issue-id confirm-hash)))))
  (log/info "Exit update-confirm-hash"))


(defn update-watch-hash
  "Sets watch-hash to NULL for bounties where watch tx has been mined. Used to avoid unneeded watch transactions in update-bounty-token-balances"
  []
  (p :update-watch-hash
     (doseq [{:keys [issue-id watch-hash]} (db-bounties/pending-watch-calls)]
    (log/info "pending watch call" watch-hash)
    (when-let [receipt (eth/get-transaction-receipt watch-hash)]
      (db-bounties/update-watch-hash issue-id nil)))))


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
  (log/info "In update-payout-receipt")
  (p :update-payout-receipt
     (doseq [{:keys [payout-hash contract-address confirm-hash issue-id updated] :as issue}
             (db-bounties/confirmed-payouts)]
    (log/debug "confirmed payout:" payout-hash)
    (try
      (if-let [receipt (eth/get-transaction-receipt payout-hash)]
        (let [contract-tokens (multisig/token-balances contract-address)
              contract-eth-balance (eth/get-balance-wei contract-address)]
          (if (or
                (some #(> (second %) 0.0) contract-tokens)
                (> contract-eth-balance 0))
            (do
              (log/info "Contract still has funds")
              (when (multisig/is-confirmed? contract-address confirm-hash)
                (log/info "Detected bounty with funds and confirmed payout, calling executeTransaction")
                (let [execute-tx-hash (multisig/execute-tx contract-address confirm-hash)]
                  (log/info "execute tx:" execute-tx-hash))))

            (do
              (log/info "Payout has succeeded, saving payout receipt for issue #" issue-id ": " receipt)
              (github/update-comment (assoc issue :payout-receipt receipt)))))
        (when (older-than-3h? updated)
          (log/info "Resetting payout hash for issue" issue-id "as it has not been mined in 3h")
          (db-bounties/reset-payout-hash issue-id)))
      (catch Throwable ex 
        (do (log/error "update-payout-receipt exception:" ex)
            (clojure.stacktrace/print-stack-trace ex))))))
  (log/info "Exit update-payout-receipt")
  )

(defn abs
  "(abs n) is the absolute value of n"
  [n]
  (cond
    (not (number? n)) (throw (IllegalArgumentException.
                              "abs requires a number"))
    (neg? n) (- n)
    :else n))


(defn update-bounty-token-balances
  "Helper function for updating internal ERC20 token balances to token multisig contract. Will be called periodically for all open bounty contracts."
  [issue-id bounty-addr watch-hash]
  #_(log/info "In update-bounty-token-balances for issue" issue-id)
  (doseq [[tla token-data] (token-data/as-map)]
    (try
      (let [balance (multisig/token-balance bounty-addr tla)]
        (when (> balance 0)
          (do
            (log/info "bounty at" bounty-addr "has" balance "of token" tla)
            (let [internal-balance (multisig/token-balance-in-bounty bounty-addr tla)]
              (when (and (nil? watch-hash)
                         (not= balance internal-balance))
                (log/info "balances not in sync, calling watch")
                (let [hash (multisig/watch-token bounty-addr tla)]
                  (db-bounties/update-watch-hash issue-id hash)))))))
      (catch Throwable ex 
        (do (log/error "update-bounty-token-balances exception:" ex)
            (clojure.stacktrace/print-stack-trace ex)))))
  #_(log/info "Exit update-bounty-token-balances"))
  

(defn update-contract-internal-balances
  "It is required in our current smart contract to manually update it's internal balance when some tokens have been added."
  []
  (log/info "In update-contract-internal-balances")
  (p :update-contract-internal-balances
     (doseq [{:keys [issue-id bounty-address watch-hash]}
             (db-bounties/open-bounty-contracts)]
       (update-bounty-token-balances issue-id bounty-address watch-hash)))
  (log/info "Exit update-contract-internal-balances"))


(defn float=
  ([x y] (float= x y 0.0000001))
  ([x y epsilon]
   (log/debug x y epsilon)
   (let [scale (if (or (zero? x) (zero? y)) 1 (abs x))]
     (<= (abs (- x y)) (* scale epsilon)))))

(defn map-float= [m1 m2]
  (and (= (set (keys m1)) (set (keys m2)))
       (every? #(float= (get m1 %1) (get m2 %1)) (keys m1))))

(defn update-balances
  []
  (log/info "In update-balances")
  (p :update-balances
     (doseq [{:keys [contract-address owner 
                     repo balance-eth tokens
                     issue-number
                     comment-id] :as issue}
             (db-bounties/open-bounty-contracts)]
       (try
         (when comment-id
           (let [balance-eth-str (eth/get-balance-eth contract-address 6)
                 current-balance-eth (read-string balance-eth-str)
                 token-balances (multisig/token-balances contract-address)]
             (log/debug "update-balances" balance-eth
                        balance-eth-str token-balances owner repo issue-number)

             (when (or
                     (not (float= current-balance-eth balance-eth))
                     (not (map-float= tokens token-balances)))
               (log/info "balances differ")
               (log/info "ETH (db):" balance-eth (type balance-eth) )
               (log/info "ETH (chain):" current-balance-eth (type current-balance-eth) )
               (log/info "ETH cmp:" (float= balance-eth current-balance-eth))
               (log/info "tokens (db):" tokens (type tokens) (type (:SNT tokens)))
               (log/info "tokens (chain):" token-balances (type token-balances) (type (:SNT token-balances)))
               (log/debug "tokens cmp:" (= tokens token-balances))

               (issues/update-balances contract-address 
                                       current-balance-eth token-balances
                                       (fiat-util/bounty-usd-value 
                                         (merge token-balances {:ETH current-balance-eth})))
               (github/update-comment issue))))
         (catch Throwable ex 
           (do (log/error "update-balances exception:" ex)
               (clojure.stacktrace/print-stack-trace ex))))))
  (log/info "Exit update-balances"))


(defn wrap-in-try-catch [func]
  (try
    (func)
    (catch Throwable t
      (log/error t))))

(defn run-tasks [tasks]
  (doall
   (map (fn [func] (wrap-in-try-catch func))
        tasks)))

(defn run-1-min-interval-tasks [time]
  (do
    (log/info "run-1-min-interval-tasks" time)
    ;; TODO: disabled for now. looks like it may cause extraneus
    ;; contract deployments and costs
    (run-tasks
     [deploy-pending-contracts
      update-issue-contract-address
      update-confirm-hash
      update-payout-receipt
      update-watch-hash
      self-sign-bounty
      ])
    (log/info "run-1-min-interval-tasks done")))


(defn run-10-min-interval-tasks [time]
  (do
    (log/info "run-10-min-interval-tasks" time)
    (run-tasks
     [update-contract-internal-balances
      update-balances])
    (log/info "run-10-min-interval-tasks done")))


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
