(ns commiteth.routes.webhooks
  (:require
   [ring.util.http-response :refer [internal-server-error]]
   [cheshire.core :as json]
   [clojure.string :as str :refer [join]]
   [clojure.tools.logging :as log]
   [commiteth.bounties :as bounties]
   [commiteth.db
    [issues :as issues]
    [pull-requests :as pull-requests]
    [repositories :as repositories]
    [users :as users]]
   [commiteth.github.core :as github]
   [commiteth.util.digest :refer [hex-hmac-sha1]]
   ;; TODO(oskarth): Bad form, put these in better namespace
   [commiteth.routes.services :refer
    [user-whitelisted?
     add-bounties-for-existing-issues?]]
   [compojure.core :refer [defroutes POST]]
   [crypto.equality :as crypto]
   [ring.util.http-response :refer [ok forbidden]]
   [commiteth.db.bounties :as bounties-db]
   [clojure.string :as string])
  (:import java.lang.Integer))

(defn find-issue-event
  [events type owner]
  (first (filter #(and
                   (= owner (get-in % [:actor :login]))
                   (= type (:event %)))
                 events)))


(defn labeled-as-bounty?
  [action issue]
  (and
   (= "labeled" action)
   (= bounties/label-name (get-in issue [:label :name]))))

(defn find-commit-sha
  [user repo issue-number event-types]
  (log/debug "find-commit-sha" user repo issue-number event-types)
  (some identity (map #(->
                        (github/get-issue-events user repo issue-number)
                        (find-issue-event % user)
                        :commit_id)
                      event-types)))


(defn handle-issue-labeled
  [webhook-payload]
  (log/debug "handle-issue-labeled")
  (let [{issue :issue} webhook-payload
        {repo-id :id
         repo-name :name} (:repository webhook-payload)]
    (bounties/maybe-add-bounty-for-issue repo-name repo-id issue)))

(defn handle-issue-closed
  [{{{owner :login} :owner repo :name}   :repository
    {issue-id :id issue-number :number} :issue}]
  (log/debug "handle-issue-closed" owner repo issue-number issue-id)

  (when (issues/is-bounty-issue? issue-id)
    (log/debug "Updating issue status to closed")
    (issues/update-open-status issue-id false))

  #_(when-let [commit-sha (find-commit-sha owner repo issue-number ["referenced" "closed"])]
      (log/debug (format "Issue %s/%s/%s closed with commit %s"
                         owner repo issue-number commit-sha))
      (log/info "NOT considering event as bounty winner")
      ;; TODO: disabled for now since the system is meant to be used
      ;;  exclusively via pull requests. issue closed event without a PR
      ;;  closed via merge first means that the referencing commit was
      ;;  pushed directly to master and thus never accepted by the
      ;;  maintainer (could be that the bounty hunter had write access
      ;;  to master, but that scenario should be very rare and better
      ;;  not to support it)
      #_(issues/close commit-sha issue-id)))

(defn handle-issue-reopened
  [{{issue-id :id} :issue}]
  (when (issues/is-bounty-issue? issue-id)
    (issues/update-open-status issue-id true)))

(def ^:const keywords
  [#"(?i)close:?\s+#(\d+)"
   #"(?i)closes:?\s+#(\d+)"
   #"(?i)closed:?\s+#(\d+)"
   #"(?i)fix:?\s+#(\d+)"
   #"(?i)fixes:?\s+#(\d+)"
   #"(?i)fixed:?\s+#(\d+)"
   #"(?i)resolve:?\s?#(\d+)"
   #"(?i)resolves:?\s+#(\d+)"
   #"(?i)resolved:?\s+#(\d+)"])

(defn extract-issue-number
  [pr-body pr-title]
  (let [cleaned-body (str/replace pr-body #"(?m)^\[comment.*$" "")
        extract (fn [source]
                  (mapcat #(keep
                            (fn [s]
                              (try (let [issue-number (Integer/parseInt (second s))]
                                     (when (pos? issue-number)
                                       issue-number))
                                   (catch NumberFormatException _)))
                            (re-seq % source)) keywords))]
    (log/debug cleaned-body)
    (concat (extract cleaned-body)
            (extract pr-title))))


(defn ensure-bounty-issue
  "Checks if an issue has a bounty label attached and returns its number"
  [user repo issue-number]
  (when-let [issue (github/get-issue user repo issue-number)]
    (when (bounties/has-bounty-label? issue)
      issue-number)))


(defn handle-claim
  [user-id login name avatar_url owner repo repo-id bounty-issue-number pr-id pr-number head-sha merged? event-type]
  (users/create-user user-id login name nil avatar_url)
  (let [issue (github/get-issue owner repo bounty-issue-number)
        open-or-edit? (contains? #{:opened :edited} event-type)
        close? (= :closed event-type)
        pr-data {:repo_id   repo-id
                 :pr_id     pr-id
                 :pr_number pr-number
                 :user_id   user-id
                 :issue_number bounty-issue-number
                 :issue_id (:id issue)
                 :state event-type}]

    ;; TODO: in the opened case if the submitting user has no
    ;; Ethereum address stored, we could post a comment to the
    ;; Github PR explaining that payout is not possible if the PR is
    ;; merged
    (cond
      open-or-edit? (do
                      (log/info "PR with reference to bounty issue"
                                bounty-issue-number "opened")
                      (pull-requests/save (merge pr-data {:state :opened
                                                          :commit_sha head-sha})))
      close? (if merged?
               (do (log/info "PR with reference to bounty issue"
                             bounty-issue-number "merged")
                   (pull-requests/save
                    (merge pr-data {:state :merged
                                    :commit_sha head-sha}))
                   (issues/update-commit-sha (:id issue) head-sha))
               (do (log/info "PR with reference to bounty issue"
                             bounty-issue-number "closed with no merge")
                   (pull-requests/save
                    (merge pr-data {:state :closed
                                    :commit_sha head-sha})))))))


(defn handle-pull-request-event
  ;; when a PR is opened, only consider it as a claim if:
  ;; * PR references an existing bounty-issue
  ;;
  ;; when a PR is merged via close event, only consider it a bounty
  ;; claim being accepted if:
  ;; * PR exists in DB
  ;; * PR references an existing bounty-issue
  [event-type
   {{{owner :login} :owner
     repo           :name
     repo-id        :id}    :repository
    {{user-id    :id
      login      :login
      avatar_url :avatar_url
      name       :name} :user
     pr-id              :id
     merged?         :merged
     {head-sha :sha} :head
     pr-number       :number
     pr-body         :body
     pr-title        :title} :pull_request}]
  (log/debug "handle-pull-request-event" event-type owner repo repo-id login pr-body pr-title)
  (log/debug (extract-issue-number pr-body pr-title))
  (if-let [bounty-issue-number (->>
                                  (extract-issue-number pr-body pr-title)
                                  (first)
                                  (ensure-bounty-issue owner repo))]
    (do
      (log/debug "Referenced bounty issue found" owner repo bounty-issue-number)
      (handle-claim user-id
                    login name
                    avatar_url
                    owner repo
                    repo-id
                    bounty-issue-number
                    pr-id
                    pr-number
                    head-sha
                    merged?
                    event-type))
    (when (= :edited event-type)
      (pull-requests/remove pr-id))))


(defn handle-issue-edited
  [webhook-payload]
  (let [gh-issue (:issue webhook-payload)
        issue-id (:id gh-issue)
        new-title (:title gh-issue)]
    (issues/update-issue-title issue-id new-title)))


(defn handle-issue
  [webhook-payload]
  (when-let [action (:action webhook-payload)]
    (log/debug "handle-issue" action)
    (when (labeled-as-bounty? action webhook-payload)
      (handle-issue-labeled webhook-payload))
    (when (and
           (= "closed" action)
           (bounties/has-bounty-label? (:issue webhook-payload)))
      (handle-issue-closed webhook-payload))
    (when (= "edited" action)
      (handle-issue-edited webhook-payload))
    (when (= "reopened" action)
      (handle-issue-reopened webhook-payload)))
  (ok))

(defn enable-repo-2 [repo-id full-repo]
  (log/debug "enable-repo-2" repo-id full-repo)
  ;; TODO(oskarth): Add granular permissions to enable creation of label
  #_(github/create-label full-repo)
  (repositories/update-repo repo-id {:state 2})
  (when (add-bounties-for-existing-issues?)
    (bounties/add-bounties-for-existing-issues full-repo)))

(defn disable-repo-2 [repo-id full-repo]
  (log/debug "disable-repo-2" repo-id full-repo)
  (repositories/update-repo repo-id {:state 0}))

(defn full-repo->owner [full-repo]
  (try
    (let [[owner _] (str/split full-repo #"/")]
      owner)
    (catch Exception e
      (log/error "exception when parsing repo" e)
      nil)))

;; NOTE(oskarth): Together with {enable,disable}-repo-2 above, this replaces
;; handle-toggle-repo for Github App.
(defn handle-add-repo [user-id username owner-avatar-url repo can-create?]
  (let [repo-id   (:id repo)
        repo-name (:name repo)
        full-repo (:full_name repo)
        _ (log/info "handle-installation add pre repo" (pr-str repo) " " (pr-str full-repo))
        owner (full-repo->owner full-repo)
        _ (log/info "handle-installation add" full-repo " " owner)
        db-user   (users/get-user user-id)]
  (log/info "handle-add-repo"
            (pr-str {:user-id          user-id
                     :name             username
                     :owner-avatar-url owner-avatar-url
                     :repo-id          repo-id
                     :repo             repo-name
                     :full-repo        full-repo}))
  (cond (not can-create?)
        {:status 400
         :body "Please join our Riot - chat.status.im/#/register and request
           access in our #openbounty room to have your account whitelisted"}

        (empty? (:address db-user))
        {:status 400
         :body "Please add your ethereum address to your profile first"}

        :else
        (try
          (let [_ (log/info "handle-add-repo pre-create")
                db-item (repositories/create
                         {:id repo-id ;; XXX: Being rename twice... silly.
                          :name repo ;; XXX: Is this name of repo?
                          :owner-avatar-url owner-avatar-url
                          :user_id user-id
                          :owner owner})
                _ (log/info "handle-add-repo db-item" db-item)
                is-enabled (= 2 (:state db-item))]
            (if is-enabled
              (disable-repo-2 repo-id full-repo)
              (enable-repo-2 repo-id full-repo))
            (ok {:enabled (not is-enabled)
                 :id repo-id
                 :full_name full-repo}))
          (catch Exception e
            (log/error "exception when enabling repo" e)
            (repositories/update-repo repo-id {:state -1})
            (internal-server-error))))))

(defn handle-installation [{:keys [action repositories sender]}]
  ;; TODO(oskarth): Handle other installs, like disable.
  (when (= action "created")
    (let [user-id (:id sender)
          username (:login sender)
          owner-avatar-url (:avatar_url sender)
          first-repo (first repositories)
          can-create? (user-whitelisted? (:username username))]
      (log/info "handle-installation created"
                (pr-str {:user-id user-id
                         :name username
                         :owner-avatar-url owner-avatar-url
                         :repos repositories}))
      (doseq [repo repositories]
        (log/info "handle-installation add pre repo" repo)
        (handle-add-repo user-id username owner-avatar-url repo can-create?))))
  (ok))

(defn handle-installation-repositories [{:keys [action sender] :as payload}]
  ;; TODO(oskarth): Handle other installs, like disable.
  ;; TODO(oskarth): Also support remove in :repositories_removed
  ;; TODO(oskarth): Also support case when :repository_selection is all - does it work differently?
  (when (= action "added")
    (let [repositories (:repositories_added payload)
          user-id (:id sender)
          username (:login sender)
          owner-avatar-url (:avatar_url sender)
          first-repo (first repositories)
          can-create? (user-whitelisted? (:username username))]
      (log/info "handle-installation-integration created"
                (pr-str {:user-id user-id
                         :name username
                         :owner-avatar-url owner-avatar-url
                         :repos repositories}))
      (doseq [repo repositories]
        (log/info "handle-installation-integration add pre repo" repo)
        (handle-add-repo user-id username owner-avatar-url repo can-create?))))
  (ok))

(defn handle-pull-request
  [pull-request]
  (let [action (keyword (:action pull-request))]
    (when (contains? #{:opened
                       :edited
                       :closed} action)
      (handle-pull-request-event action pull-request))
    (ok)))


(defn validate-secret [webhook-payload raw-payload github-signature]
  ;; used for oauth app webhooks. secret is repo-specific
  (let [full-name (get-in webhook-payload [:repository :full_name])
        repo (repositories/get-repo full-name)
        secret (:hook_secret repo)]
    (and (not (string/blank? secret))
         (crypto/eq? github-signature
                     (str "sha1=" (hex-hmac-sha1 secret raw-payload))))))


(defn validate-secret-one-hook [webhook-payload raw-payload github-signature]
  ;; used for GH app webhooks. secret is shared
  (let [secret (github/webhook-secret)
        ;; XXX remove below once verified in logs
        debug-secret (apply str (take 5 (github/webhook-secret)))]
    (log/debug "validate secret for GH app"  debug-secret)
    (and (not (string/blank? secret))
         (crypto/eq? github-signature
                     (str "sha1=" (hex-hmac-sha1 secret raw-payload))))))

(defroutes webhook-routes
  (POST "/webhook" {:keys [headers body]}
        (log/debug "webhook POST, headers" headers)
        (let [raw-payload (slurp body)
              payload (json/parse-string raw-payload true)]
          (log/debug "webhook POST, payload" payload)
          (if (validate-secret payload raw-payload (get headers "x-hub-signature"))
            (do
              (log/debug "Github secret validation OK")
              (log/debug "x-github-event" (get headers "x-github-event"))
              (case (get headers "x-github-event")
                "issues" (handle-issue payload)
                "pull_request" (handle-pull-request payload)
                (ok)))
            (forbidden))))
  (POST "/webhook-app" {:keys [headers body]}
        (log/debug "webhook-app POST, headers" headers)
        (let [raw-payload (slurp body)
              payload (json/parse-string raw-payload true)]
          (log/info "webhook-app POST, payload:" (pr-str payload))
          (if (validate-secret-one-hook payload raw-payload (get headers "x-hub-signature"))
            (do
              (log/debug "Github secret validation OK app")
              (log/info "x-github-event app" (get headers "x-github-event"))
              (case (get headers "x-github-event")
                "issues" (handle-issue payload)
                "pull_request" (handle-pull-request payload)
                "installation" (handle-installation payload)
                "installation_repositories" (handle-installation-repositories payload)

                ;; NOTE(oskarth): These two webhooks are / will be deprecated on
                ;; November 22, 2017 but they keep being called. According to
                ;; documentation they should contain same format.
                ;; https://developer.github.com/webhooks/
                "integration_installation" (handle-installation payload)
                "integration_installation_repositories" (handle-installation-repositories payload)
                (ok)))
            (forbidden)))))


