(ns commiteth.db.issues
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]))

(defn create
  "Creates issue"
  [repo-id issue-id issue-number issue-title address]
  (jdbc/with-db-connection [con-db *db*]
    (db/create-issue! con-db {:repo_id      repo-id
                              :issue_id     issue-id
                              :issue_number issue-number
                              :title        issue-title
                              :address      address})))

(defn close
  "Updates issue with commit_id"
  [issue-id commit-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/close-issue! con-db {:issue_id issue-id :commit_id commit-id})))
