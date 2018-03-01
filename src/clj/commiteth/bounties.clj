(ns commiteth.bounties
  (:require [commiteth.db.issues :as issues]
            [commiteth.db.users :as users]
            [commiteth.db.repositories :as repos]
            [commiteth.db.comment-images :as comment-images]
            [commiteth.eth.core :as eth]
            [commiteth.github.core :as github]
            [commiteth.eth.multisig-wallet :as multisig]
            [commiteth.util.png-rendering :as png-rendering]
            [clojure.tools.logging :as log]))


(def ^:const label-name "bounty")

;; max number of issues allowed to be created per repo
(def max-issues-limit 100)

(defn has-bounty-label?
  [issue]
  (let [labels (:labels issue)]
    (some #(= label-name (:name %)) labels)))

(defn deploy-contract [owner owner-address repo issue-id issue-number]
  (if (empty? owner-address)
    (log/error "Unable to deploy bounty contract because"
               "repo owner has no Ethereum addres")
    (do
      (log/info "deploying contract to " owner-address)
      (if-let [transaction-hash (multisig/deploy-multisig owner-address)]
        (do
          (log/info "Contract deployed, transaction-hash:"
                    transaction-hash)
          (->> (github/post-deploying-comment owner
                                              repo
                                              issue-number
                                              transaction-hash)
               :id
               (issues/update-comment-id issue-id))
          (issues/update-transaction-hash issue-id transaction-hash))
        (log/error "Failed to deploy contract to" owner-address)))))
      
(defn add-bounty-for-issue [repo repo-id issue]
  (let [{issue-id     :id
         issue-number :number
         issue-title  :title} issue
        created-issue (issues/create repo-id issue-id issue-number issue-title)
        {owner-address :address
         owner :owner} (users/get-repo-owner repo-id)]
    (log/debug "Adding bounty for issue " repo issue-number "owner address: " owner-address)
    (if (= 1 created-issue)
      (deploy-contract owner owner-address repo issue-id issue-number)
      (log/debug "Issue already exists in DB, ignoring"))))

(defn maybe-add-bounty-for-issue [repo repo-id issue]
  (let [res (issues/get-issues-count repo-id)
        {count :count} res
        limit-reached? (> count max-issues-limit)
        _ (log/debug "*** get-issues-count" repo-id " " res " " count " " limit-reached?)]
    (if limit-reached?
      (log/debug "Total issues for repo limit reached " repo " " count)
      (add-bounty-for-issue repo repo-id issue))))


;; We have a max-limit to ensure people can't add more issues and
;; drain bot account until we have economic design in place
(defn add-bounties-for-existing-issues [full-name]
  (let [{repo-id :repo_id
         owner :owner
         repo :repo} (repos/get-repo full-name)
        issues (github/get-issues owner repo)
        bounty-issues (filter has-bounty-label? issues)
        max-bounties (take max-issues-limit bounty-issues)]
    (log/debug (str "adding bounties for" (count bounty-issues)
                    " existing issues (total " (count bounty-issues) ")"))
    (doall
     (map (partial maybe-add-bounty-for-issue repo repo-id) max-bounties))))


(defn update-bounty-comment-image [issue-id owner repo issue-number contract-address eth-balance eth-balance-str tokens]
  (let [hash (github/github-comment-hash owner repo issue-number eth-balance)
        issue-url (str owner "/" repo "/issues/" (str issue-number))
        png-data (png-rendering/gen-comment-image
                  contract-address
                  eth-balance-str
                  tokens
                  issue-url)]
    (log/debug "update-bounty-comment-image" issue-id owner repo issue-number)
    (log/debug contract-address eth-balance-str)
    (log/debug "hash" hash)

    (if png-data
      (comment-images/save-image! issue-id hash png-data)
      (log/error "Failed ot generate PNG"))))


(defn update-bounty-issue-titles
  "Update stored titles for bounty issues if changed on Github side"
  []
  (log/debug "update-bounty-issue-titles")
  (for [{:keys [title issue_number repo owner]}
        (issues/get-issue-titles)]
    (let [gh-issue (github/get-issue owner repo issue_number)]
      (if-not (= title (:title gh-issue))
        (do
          (log/info "Updating changed title for issue" (:id gh-issue))
          (issues/update-issue-title (:id gh-issue) (:title gh-issue)))))))
