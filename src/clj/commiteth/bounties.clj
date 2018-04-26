(ns commiteth.bounties
  (:require [commiteth.db.issues :as issues]
            [commiteth.db.users :as users]
            [commiteth.db.repositories :as repos]
            [commiteth.db.comment-images :as comment-images]
            [commiteth.eth.core :as eth]
            [commiteth.eth.tracker :as tracker]
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
    (log/errorf "issue %s: Unable to deploy bounty contract because repo owner has no Ethereum addres" issue-id)
    (try
      (log/infof "issue %s: Deploying contract to %s" issue-id owner-address)
      (if-let [tx-info (multisig/deploy-multisig {:owner owner-address
                                                  :internal-tx-id [:deploy issue-id]})]
        (do
          (log/infof "issue %s: Contract deployed, transaction-hash: %s" issue-id (:tx-hash tx-info))
          (let [resp (github/post-deploying-comment owner
                                                    repo
                                                    issue-number
                                                    (:tx-hash tx-info))
                comment-id (:id resp)]
            (log/infof "issue %s: post-deploying-comment response: %s" issue-id resp)
            (issues/update-comment-id issue-id comment-id))
          (tracker/track-tx! tx-info))
        (log/errorf "issue %s Failed to deploy contract to %s" issue-id owner-address))
      (catch Exception ex (log/errorf ex "issue %s: deploy-contract exception" issue-id)))))

(defn add-bounty-for-issue [repo repo-id issue]
  (let [{issue-id     :id
         issue-number :number
         issue-title  :title} issue
        created-issue (issues/create repo-id issue-id issue-number issue-title)
        {owner-address :address
         owner :owner} (users/get-repo-owner repo-id)]
    (log/debug "issue %s: Adding bounty for issue %s/%s - owner address: %s"
               issue-id repo issue-number owner-address)
    (if (= 1 created-issue)
      (deploy-contract owner owner-address repo issue-id issue-number)
      (log/debug "issue %s: Issue already exists in DB, ignoring"))))

(defn maybe-add-bounty-for-issue [repo repo-id issue]
  (let [res (issues/get-issues-count repo-id)
        {count :count} res
        limit-reached? (> count max-issues-limit)]
    (log/debug "*** get-issues-count" repo-id " " res " " count " " limit-reached?)
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
        (issues/update-issue-title (:id gh-issue) (:title gh-issue))))))

(defn assert-keys [m ks]
  (doseq [k ks]
    (when-not (find m k)
      (throw (ex-info (format "Expected key missing from provided map: %s" k) {:map m})))))

(defn bounty-state
  "Given a map as returned by `owner-bounties` return the state the provided bounty is in.

  The lifecycle of a bounty is a sequence of the following states:
  :opened > :funded > :claimed > :merged > :paid

  As well as various states that are only reached under specific conditins:
  - :multiple-claims
  - :pending-contributor-address
  - :pending-maintainer-confirmation"
  [bounty]
  (assert-keys bounty [:winner_login :payout_address :confirm_hash :payout_hash
                       :claims :tokens :contract_address])
  (if-let [merged? (:winner_login bounty)]
    (cond
      (nil? (:payout_address bounty)) :pending-contributor-address
      (nil? (:confirm_hash bounty))   :pending-maintainer-confirmation
      (:payout_hash bounty)           :paid
      :else                           :merged)
    (cond ; not yet merged
      (< 1 (count (:claims bounty)))  :multiple-claims
      (= 1 (count (:claims bounty)))  :claimed
      (seq (:tokens bounty))          :funded
      (:contract_address bounty)      :opened)))

(comment
  (def user 97496)

  (clojure.pprint/pprint
   (count (bounties/owner-bounties user)))

  (clojure.pprint/pprint
   (frequencies (map bounty-state (bounties/owner-bounties user))))

  )
