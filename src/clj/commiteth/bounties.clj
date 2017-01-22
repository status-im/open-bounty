(ns commiteth.bounties
  (:require [commiteth.db.issues :as issues]
            [commiteth.db.users :as users]
            [commiteth.db.repositories :as repos]
            [commiteth.eth.core :as eth]
            [commiteth.github.core :as github]
            [commiteth.eth.core :as eth]
            [clojure.tools.logging :as log]))


(def ^:const label-name "bounty")

(defn has-bounty-label?
  [issue]
  (let [labels (:labels issue)]
    (some #(= label-name (:name %)) labels)))


(defn add-bounty-for-issue [repo-map issue]
  (log/debug "add-bounty-for-issue" issue)
  (let [{issue-id     :id
         issue-number :number
         issue-title  :title} issue
        {repo :repo
         repo-id :repo_id
         user :login} repo-map
        created-issue (issues/create repo-id issue-id issue-number issue-title)
        repo-owner    (:address (users/get-repo-owner repo-id))]
    (log/info (format "Issue %s/%s/%s labeled as bounty" user repo issue-number))
    (if (= 1 created-issue)
      (let [transaction-hash (eth/deploy-contract repo-owner)]
        (log/info "Contract deployed, transaction-hash:" transaction-hash )
        (issues/update-transaction-hash issue-id transaction-hash))
      (log/debug "Issue already exists in DB, ignoring"))))


(defn add-bounties-for-existing-issues [repo-map]
  (let [{repo :repo
         user :login} repo-map
        issues (github/get-issues user repo)
        bounty-issues (filter has-bounty-label? issues)]
    (log/debug bounty-issues)
    (log/debug "adding bounties for"
               (count bounty-issues) " existing issues")
    (doall
     (map (partial add-bounty-for-issue repo-map) bounty-issues))))
