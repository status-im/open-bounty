(ns commiteth.bounties
  (:require [commiteth.db.issues :as issues]
            [commiteth.db.users :as users]
            [commiteth.db.repositories :as repos]
            [commiteth.db.comment-images :as comment-images]
            [commiteth.eth.core :as eth]
            [commiteth.github.core :as github]
            [commiteth.eth.core :as eth]
            [commiteth.util.png-rendering :as png-rendering]
            [clojure.tools.logging :as log]))


(def ^:const label-name "bounty")

(defn has-bounty-label?
  [issue]
  (let [labels (:labels issue)]
    (some #(= label-name (:name %)) labels)))


(defn add-bounty-for-issue [repo repo-id issue]
  (let [{issue-id     :id
         issue-number :number
         issue-title  :title} issue
        created-issue (issues/create repo-id issue-id issue-number issue-title)
        repo-owner-address    (:address (users/get-repo-owner repo-id))]
    (log/debug "Adding bounty for issue " repo issue-number "owner address: " repo-owner-address)
    (if (= 1 created-issue)
      (if (empty? repo-owner-address)
        (log/error "Unable to deploy bounty contract because"
                   "repo owner has no Ethereum addres")
        (do
          (log/debug "deploying contract to " repo-owner-address)
          (let [transaction-hash (eth/deploy-contract repo-owner-address)]
            (if (nil? transaction-hash)
              (log/error "Failed to deploy contract to" repo-owner-address)
              (log/info "Contract deployed, transaction-hash:"
                        transaction-hash ))
            (issues/update-transaction-hash issue-id transaction-hash))))
      (log/debug "Issue already exists in DB, ignoring"))))


(defn add-bounties-for-existing-issues [full-name]
  (let [{repo-id :repo_id
         owner :owner
         repo :repo} (repos/get-repo full-name)
        issues (github/get-issues owner repo)
        bounty-issues (filter has-bounty-label? issues)]
    (log/debug "adding bounties for"
               (count bounty-issues) " existing issues")
    (doall
     (map (partial add-bounty-for-issue repo repo-id) bounty-issues))))


(defn update-bounty-comment-image [issue-id owner repo issue-number contract-address balance balance-str]
  (let [hash (github/github-comment-hash owner repo issue-number balance)
        issue-url (str owner "/" repo "/issues/" (str issue-number))
        png-data (png-rendering/gen-comment-image
                  contract-address
                  balance-str
                  issue-url)]
    (log/debug "update-bounty-comment-image" issue-id owner repo issue-number)
    (log/debug contract-address balance-str)
    (log/debug "hash" hash)

    (if png-data
      (comment-images/save-image! issue-id hash png-data)
      (log/error "Failed ot generate PNG"))))
