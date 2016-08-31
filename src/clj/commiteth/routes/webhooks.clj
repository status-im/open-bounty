(ns commiteth.routes.webhooks
  (:require [compojure.core :refer [defroutes POST]]
            [commiteth.github.core :as github]
            [commiteth.db.pull-requests :as pull-requests]
            [commiteth.db.issues :as issues]
            [commiteth.db.users :as users]
            [ring.util.http-response :refer [ok]]
            [clojure.string :refer [join]])
  (:import [java.util UUID]))

(def label-name "bounty")

(defn find-issue-event
  [events type]
  (first (filter #(= type (:event %)) events)))

(defn find-commit-id
  [user repo issue-number event-type]
  (->
    (github/get-issue-events user repo issue-number)
    (find-issue-event event-type)
    (:commit_id)))

(defn handle-issue-closed
  [{{{user :login} :owner repo :name}   :repository
    {issue-id :id issue-number :number} :issue}]
  (future
    (when-let [commit-id (find-commit-id user repo issue-number "referenced")]
      (issues/close commit-id issue-id))))

(defn handle-pull-request-closed
  [{{{owner :login} :owner
     repo           :name
     repo-id        :id}      :repository
    {{user-id :id
      login   :login
      name    :name} :user
     id              :id
     pr-number       :number} :pull_request}]
  (future
    (when-let [commit-id (find-commit-id owner repo pr-number "merged")]
      (pull-requests/create {:repo_id   repo-id
                             :pr_id     id
                             :pr_number pr-number
                             :user_id   user-id
                             :commit_id commit-id})
      (users/create-user user-id login name nil nil))))

(defn labeled-as-bounty?
  [action issue]
  (and
    (= "labeled" action)
    (= label-name (get-in issue [:label :name]))))

(defn has-bounty-label?
  [issue]
  (let [labels (get-in issue [:issue :labels])]
    (some #(= label-name (:name %)) labels)))

(defn gen-address []
  (UUID/randomUUID))

(defn handle-issue
  [issue]
  (when-let [action (:action issue)]
    (when (labeled-as-bounty? action issue)
      (let [repository    (:repository issue)
            {repo-id              :id
             {owner-login :login} :owner
             repo-name            :name} repository
            issue         (:issue issue)
            {issue-id     :id
             issue-number :number
             issue-title  :title} issue
            issue-address (gen-address)]
        (github/post-comment owner-login repo-name issue-number issue-address)
        (issues/create repo-id issue-id issue-number issue-title issue-address)))
    (when (and
            (= "closed" action)
            (has-bounty-label? issue))
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
