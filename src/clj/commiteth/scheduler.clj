(ns commiteth.scheduler
  (:require [commiteth.eth.core :as eth]
            [commiteth.eth.multisig-wallet :as wallet]
            [commiteth.github.core :as github]
            [commiteth.db.issues :as issues]
            [commiteth.db.users :as users]
            [overtone.at-at :refer [every mk-pool]]
            [clojure.tools.logging :as log]
            [mount.core :as mount]))

(def pool (mk-pool))

(defn update-issue-contract-address []
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
              repo-owner-address (:address (users/get-repo-owner repo-id))]
          (github/post-comment user repo issue-number contract-address balance)
          (wallet/add-owner contract-address (eth/eth-account))
          (wallet/add-owner contract-address repo-owner-address))))))

(mount/defstate scheduler :start (every (* 5 60 1000) update-issue-contract-address pool))
