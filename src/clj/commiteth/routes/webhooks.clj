(ns commiteth.routes.webhooks
  (:require [compojure.core :refer [defroutes POST]]
            [commiteth.github.core :as github]
            [commiteth.db.pull-requests :as pull-requests]
            [commiteth.db.issues :as issues]
            [commiteth.db.users :as users]
            [commiteth.eth.core :as eth]
            [ring.util.http-response :refer [ok]]
            [clojure.string :refer [join]]
            [clojure.tools.logging :as log])
  (:import [java.lang Integer]))

(def label-name "bounty")

(defn find-issue-event
  [events type owner]
  (first (filter #(and
                   (= owner (get-in % [:actor :login]))
                   (= type (:event %)))
           events)))

(defn find-commit-id
  [user repo issue-number event-types]
  (some identity (map #(->
                        (github/get-issue-events user repo issue-number)
                        (find-issue-event % user)
                        :commit_id)
                   event-types)))

(defn handle-issue-labeled
  [issue]
  (let [{repo-id       :id
         repo          :name
         {user :login} :owner} (:repository issue)
        {issue-id     :id
         issue-number :number
         issue-title  :title} (:issue issue)
        created-issue (issues/create repo-id issue-id issue-number issue-title)
        repo-owner    (:address (users/get-repo-owner repo-id))]
    (log/debug (format "Issue %s/%s/%s labeled as bounty" user repo issue-number))
    (when (= 1 created-issue)
      (issues/update-transaction-hash issue-id (eth/deploy-contract repo-owner)))))

(defn handle-issue-closed
  [{{{user :login} :owner repo :name}   :repository
    {issue-id :id issue-number :number} :issue}]
  (future
    (when-let [commit-id (find-commit-id user repo issue-number ["referenced" "closed"])]
      (log/debug (format "Issue %s/%s/%s closed with commit %s" user repo issue-number commit-id))
      (issues/close commit-id issue-id))))

(def keywords
  [#"(?i)close\s+#(\d+)"
   #"(?i)closes\s+#(\d+)"
   #"(?i)closed\s+#(\d+)"
   #"(?i)fix\s+#(\d+)"
   #"(?i)fixes\s+#(\d+)"
   #"(?i)fixed\s+#(\d+)"
   #"(?i)resolve\s+#(\d+)"
   #"(?i)resolves\s+#(\d+)"
   #"(?i)resolved\s+#(\d+)"])

(defn extract-issue-number
  [pr-body]
  (mapcat #(keep
            (fn [s]
              (try (let [issue-number (Integer/parseInt (second s))]
                     (when (pos? issue-number)
                       issue-number))
                   (catch NumberFormatException _)))
            (re-seq % pr-body)) keywords))

(defn has-bounty-label?
  [issue]
  (let [labels (:labels issue)]
    (some #(= label-name (:name %)) labels)))

(defn validate-issue-number
  "Checks if an issue has a bounty label attached and returns its number"
  [user repo issue-number]
  (when-let [issue (github/get-issue user repo issue-number)]
    (when (has-bounty-label? issue)
      issue-number)))

(defn handle-pull-request-closed
  [{{{owner :login} :owner
     repo           :name
     repo-id        :id}    :repository
    {{user-id :id
      login   :login
      name    :name} :user
     id              :id
     pr-number       :number
     pr-body         :body} :pull_request}]
  (future
    (let [commit-id    (find-commit-id owner repo pr-number ["merged"])
          issue-number (->>
                         (extract-issue-number pr-body)
                         (first)
                         (validate-issue-number owner repo))
          m            {:commit_id commit-id :issue_number issue-number}]
      (when (or commit-id issue-number)
        (log/debug (format "Pull request %s/%s/%s closed with reference to %s"
                     login repo pr-number
                     (if commit-id (str "commit-id " commit-id)
                                   (str "issue-number " issue-number))))
        (pull-requests/create (merge m {:repo_id   repo-id
                                        :pr_id     id
                                        :pr_number pr-number
                                        :user_id   user-id}))
        (users/create-user user-id login name nil nil)))))

(defn labeled-as-bounty?
  [action issue]
  (and
    (= "labeled" action)
    (= label-name (get-in issue [:label :name]))))

(defn handle-issue
  [issue]
  (when-let [action (:action issue)]
    (when (labeled-as-bounty? action issue)
      (handle-issue-labeled issue))
    (when (and
            (= "closed" action)
            (has-bounty-label? (:issue issue)))
      (handle-issue-closed issue)))
  (ok (str issue)))

(defn handle-pull-request
  [pull-request]
  (when (= "closed" (:action pull-request))
    (handle-pull-request-closed pull-request))
  (ok (str pull-request)))

(defroutes webhook-routes
  (POST "/webhook" {:keys [params headers]}
    (case (get headers "x-github-event")
      "issues" (handle-issue params)
      "pull_request" (handle-pull-request params)
      (ok))))
