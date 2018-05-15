(ns commiteth.bounties
  (:require [commiteth.db.issues :as issues]
            [commiteth.db.bounties :as db-bounties]
            [commiteth.db.users :as users]
            [commiteth.db.repositories :as repos]
            [commiteth.eth.core :as eth]
            [commiteth.eth.tracker :as tracker]
            [commiteth.github.core :as github]
            [commiteth.util.util :refer [to-map]]
            [commiteth.eth.multisig-wallet :as multisig]
            [commiteth.model.bounty :as bnt] 
            [clojure.tools.logging :as log]))


(def ^:const label-name "bounty")

;; max number of issues allowed to be created per repo
(def max-issues-limit 100)

(defn has-bounty-label?
  [issue]
  (let [labels (:labels issue)]
    (some #(= label-name (:name %)) labels)))

(defn transition [{:keys [issue-id tx-info] :as bounty} state]
  (let [bounty-not= (fn [current db]
                      (some #(not= (%1 current) (%1 db)) 
                            (disj (set (keys current)) :tx-info)))
        bounty-from-db (issues/get-issue-by-id issue-id)
        bounty (and (bounty-not= bounty bounty-from-db)
                    (merge bounty-from-db bounty))]
    (when bounty
      (case state
        :deploying
        (tracker/track-tx! tx-info)

        :opened
        (do 
          (tracker/untrack-tx! {:issue-id (:issue-id bounty) 
                                :tx-hash (:transaction-hash bounty) 
                                :result (:contract-address bounty) 
                                :type :deploy})
          (github/update-bounty-comment-image bounty))

        :pending-sob-confirmation
        (tracker/track-tx! tx-info)

        :pending-maintainer-confirmation
        (tracker/untrack-tx! tx-info)

        :paid-with-receipt
        (db-bounties/update-payout-receipt issue-id (:payout-receipt bounty))

        :watch-set
        (tracker/track-tx! tx-info)

        :watch-reset
        (tracker/untrack-tx! tx-info)

        :update-balances
        (issues/update-balances (:contract-address bounty)
                                (:balance-eth bounty)
                                (:tokens bounty)
                                (:value-usd bounty))

        )
    (github/update-comment bounty state))))
(defn deploy-contract [owner-address  issue-id]
  (if (empty? owner-address)
    (log/errorf "issue %s: Unable to deploy bounty contract because repo owner has no Ethereum addres" issue-id)
    (try
      (log/infof "issue %s: Deploying contract to %s" issue-id owner-address)
      (if-let [tx-info (multisig/deploy-multisig {:owner owner-address
                                                  :internal-tx-id [:deploy issue-id]})]
        (do
          (log/infof "issue %s: Contract deployed, transaction-hash: %s" issue-id (:tx-hash tx-info))
          (transition {:issue-id issue-id
                       :transaction-hash (:tx-hash tx-info)
                       :tx-info tx-info} :deploying))
        (log/errorf "issue %s Failed to deploy contract to %s" issue-id owner-address))
      (catch Exception ex (log/errorf ex "issue %s: deploy-contract exception" issue-id)))))

(defn add-bounty-for-issue [repo repo-id issue]
  (let [{issue-id     :id
         issue-number :number
         issue-title  :title} issue
        created-issue (issues/create repo-id issue-id issue-number issue-title)
        {:keys [address owner]} (users/get-repo-owner repo-id)]
    (log/debug "issue %s: Adding bounty for issue %s/%s - owner address: %s"
               issue-id repo issue-number address)
    (if (= 1 created-issue)
      (deploy-contract address issue-id)
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
  (let [{:keys [repo-id
         owner repo] } (repos/get-repo full-name)
        issues (github/get-issues owner repo)
        bounty-issues (filter has-bounty-label? issues)
        max-bounties (take max-issues-limit bounty-issues)]
    (log/debug (str "adding bounties for" (count bounty-issues)
                    " existing issues (total " (count bounty-issues) ")"))
    (doall
     (map (partial maybe-add-bounty-for-issue repo repo-id) max-bounties))))


(defn update-bounty-issue-titles
  "Update stored titles for bounty issues if changed on Github side"
  []
  (log/debug "update-bounty-issue-titles")
  (for [{:keys [title issue-number repo owner]}
        (issues/get-issue-titles)]
    (let [gh-issue (github/get-issue owner repo issue-number)]
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
  ;; Some bounties have been paid out manually, the payout hash
  ;; was set properly but winner_login was not
  (let [open-claims (fn open-claims [bounty]
                      (filter bnt/open? (:claims bounty)))]
    (if-let [merged-or-paid? (or (:winner_login bounty)
                                 (:payout_hash bounty))]
      (cond
        (:payout_receipt bounty)        :paid-with-receipt
        (:payout_hash bounty)           :paid
        (nil? (:payout_address bounty)) :pending-contributor-address
        ;; `confirm_hash` is set by us as soon as a PR is merged and the
        ;; contributor address is known. Usually end users should not need
        ;; to be aware of this step.
        (nil? (:confirm_hash bounty))   :pending-sob-confirmation
        ;; `payout_hash` is set when the bounty issuer signs the payout
        (nil? (:payout_hash bounty))    :pending-maintainer-confirmation
        :else                           :merged)
      (cond ; not yet merged
        (< 1 (count (open-claims bounty)))  :multiple-claims
        (= 1 (count (open-claims bounty)))  :claimed
        (seq (:tokens bounty))          :funded
        (:contract_address bounty)      :opened))))



(comment
  (def user 97496)

  (clojure.pprint/pprint
   (count (bounties/owner-bounties user)))

  (clojure.pprint/pprint
   (frequencies (map bounty-state (bounties/owner-bounties user))))

  )
