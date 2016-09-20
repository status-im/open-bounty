(ns commiteth.scheduler
  (:require [commiteth.eth.core :as eth]
            [commiteth.eth.multisig-wallet :as wallet]
            [commiteth.github.core :as github]
            [commiteth.db.issues :as issues]
            [commiteth.db.users :as users]
            [commiteth.db.bounties :as bounties]
            [overtone.at-at :refer [every mk-pool stop-and-reset-pool!]]
            [clojure.tools.logging :as log]
            [mount.core :as mount]))

(def pool (mk-pool))

(defn update-issue-contract-address
  "For each pending deployment:
      gets transasction receipt, updates db state, posts github comment and adds owners to the wallet"
  []
  (doseq [{issue-id         :issue_id
           transaction-hash :transaction_hash} (issues/list-pending-deployments)]
    (log/debug "pending deployment:" transaction-hash)
    (when-let [receipt (eth/get-transaction-receipt transaction-hash)]
      (log/info "transaction receipt for issue #" issue-id ": " receipt)
      (when-let [contract-address (:contractAddress receipt)]
        (let [issue              (issues/update-contract-address issue-id contract-address)
              {user         :login
               repo         :repo
               repo-id      :repo_id
               issue-number :issue_number} issue
              balance            (eth/get-balance-eth contract-address 4)
              repo-owner-address (:address (users/get-repo-owner repo-id))
              {comment-id :id} (github/post-comment user repo issue-number balance)]
          (issues/update-comment-id issue-id comment-id)
          (wallet/add-owner contract-address (eth/eth-account))
          (wallet/add-owner contract-address repo-owner-address))))))

(defn self-sign-bounty
  "Walks through all issues eligible for bounty payout and signs corresponding transaction"
  []
  (doseq [{contract-address :contract_address
           issue-id         :issue_id
           payout-address   :payout_address} (bounties/pending-bounties-list)
          :let [value (eth/get-balance-hex contract-address)]]
    (->>
      (wallet/execute contract-address payout-address value)
      (bounties/update-confirm-hash issue-id))))

(defn update-balance
  []
  (doseq [{contract-address :contract_address
           login            :login
           repo             :repo
           comment-id       :comment_id
           issue-number     :issue_number} (bounties/list-wallets)]
    (when comment-id
      (let [{old-balance :balance} (issues/get-balance contract-address)
            current-balance-hex (eth/get-balance-hex contract-address)
            current-balance-eth (eth/hex->eth current-balance-hex 8)]
        (when-not (= old-balance current-balance-hex)
          (issues/update-balance contract-address current-balance-hex)
          (github/update-comment login repo comment-id issue-number current-balance-eth))))))

(mount/defstate scheduler
  :start (do
           (every (* 1 60 1000) update-issue-contract-address pool)
           (every (* 1 60 1000) self-sign-bounty pool)
           (every (* 1 60 1000) update-balance pool))
  :stop (do
          (log/info "Stopping scheduler pool")
          (stop-and-reset-pool! pool)))

(defn stop []
  (log/info "Stopping scheduler pool")
  (stop-and-reset-pool! pool))
