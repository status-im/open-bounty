(ns commiteth.scheduler
  (:require [commiteth.eth.core :as eth]
            [commiteth.github.core :as github]
            [commiteth.db.issues :as issues]
            [overtone.at-at :refer [every mk-pool]]
            [clojure.tools.logging :as log]
            [mount.core :as mount]))

(def pool (mk-pool))

(defn update-issue-contract-address []
  (println "Hi.")
  (for [{issue-id         :issue_id
         transaction-hash :transaction_hash} (issues/list-pending-deployments)]
    (when-let [receipt (eth/get-transaction-receipt transaction-hash)]
      (log/info "transaction receipt for issue #" issue-id ": " receipt)
      (when-let [contract-address (:contractAddress receipt)]
        (let [issue   (issues/update-contract-address issue-id contract-address)
              {user         :login
               repo         :repo
               issue-number :issue_number} issue
              balance (eth/get-balance-eth contract-address 4)]
          (github/post-comment user repo issue-number contract-address balance))))))

(mount/defstate scheduler :start (every (* 5 1000) update-issue-contract-address pool))
