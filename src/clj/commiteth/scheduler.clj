(ns commiteth.scheduler
  (:require [commiteth.eth.core :as eth]
            [commiteth.eth.multisig-wallet :as wallet]
            [commiteth.github.core :as github]
            [commiteth.db.issues :as issues]
            [commiteth.db.users :as users]
            [commiteth.db.bounties :as bounties]
            [overtone.at-at :refer [every mk-pool]]
            [clojure.tools.logging :as log]
            [mount.core :as mount]))

(def pool (mk-pool))

(defn update-issue-contract-address
  "For each pending deployment:
      gets transasction receipt, updates db state, posts github comment and adds owners to the wallet"
  []
  (for [{issue-id         :issue_id
         transaction-hash :transaction_hash} (issues/list-pending-deployments)]
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
  (for [{contract-address :contract_address
         issue-id         :issue_id
         payout-address   :payout_address} (bounties/pending-bounties-list)]
    (let [value (eth/get-balance-hex contract-address)]
      (->>
        (wallet/execute contract-address payout-address value)
        (bounties/update-confirm-hash issue-id)))))

(defn update-balance
  []
  (for [{contract-address :contract_address
         login            :login
         repo             :repo
         comment-id       :comment_id
         issue-number     :issue_number} (bounties/list-wallets)]
    (when comment-id
      (let [balance (eth/get-balance-eth contract-address 8)]
        (github/update-comment login repo comment-id issue-number balance)))))

(mount/defstate scheduler :start
  (do
    (every (* 5 60 1000) update-issue-contract-address pool)
    (every (* 60 1000) self-sign-bounty pool)
    (every (* 5 60 1000) update-balance pool)))
