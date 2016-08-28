(ns commiteth.db.repositories
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]))

(defn toggle
  "Toggles specified repository"
  [repo-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/toggle-repository! con-db {:repo_id repo-id})))

(defn create
  "Creates repository"
  [repo]
  (jdbc/with-db-connection [con-db *db*]
    (db/create-repository! con-db (-> repo
                                    (rename-keys {:id :repo_id :name :repo})
                                    (merge {:enabled true})))))

(defn get-enabled
  "Lists enabled repositories ids for a given login"
  [user-id]
  (->>
    (jdbc/with-db-connection [con-db *db*]
      (db/get-enabled-repositories con-db {:user_id user-id}))
    (mapcat vals)))

(defn update-hook-id
  "Updates github webhook id for a given repository"
  [repo-id hook-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-hook-id con-db {:repo_id repo-id :hook_id hook-id})))
