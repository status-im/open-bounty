(ns commiteth.bounties
  (:require [commiteth.db.issues :as issues]
            [commiteth.db.users :as users]
            [commiteth.db.repositories :as repos]
            [commiteth.db.comment-images :as comment-images]
            [commiteth.db.bounties :as db-bounties]
            [commiteth.eth.core :as eth]
            [commiteth.eth.token-data :as token-data]
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
          (let [resp (github/post-deploying-comment owner
                                                    repo
                                                    issue-number
                                                    transaction-hash)
                _ (log/info "post-deploying-comment response:" resp)
                comment-id (:id resp)]
            (issues/update-comment-id issue-id comment-id))
          (issues/update-transaction-hash issue-id transaction-hash))
        (log/error "Failed to deploy contract to" owner-address)))))

(defn watch-tokens [issue-id contract-address watch-hash]
  "Helper function for updating internal ERC20 token balances to token multisig contract.
   Will be called periodically for all open bounty contracts, or on-demand via /api/watchTokens endpoint."
  (try
    (doseq [[tla token-data] (token-data/as-map)]
      (let [balance (multisig/token-balance contract-address tla)]
        (log/info "balance for " tla ":" balance)
        (when (> balance 0)
          (do
            (log/info "bounty at" contract-address "has" balance "of token" tla)
            (let [internal-balance (multisig/token-balance-in-bounty contract-address tla)]
              (when (and (nil? watch-hash) 
                         (not= balance internal-balance))
                (log/info "balances not in sync, calling watch")
                (let [watch-hash (multisig/watch-token contract-address tla)]
                  (db-bounties/update-watch-hash issue-id watch-hash))))))))
    (catch Throwable ex 
      (log/error "watch-tokens exception:" ex)
      (clojure.stacktrace/print-stack-trace ex))))
      
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
