(ns commiteth.db.repositories
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]
            [clojure.string :as str]))

(defn create
  "Creates repository or returns existing one."
  [repo]
  (jdbc/with-db-connection [con-db *db*]
    (or
     (db/create-repository! con-db (-> repo
                                       (rename-keys {:id :repo_id
                                                     :name :repo
                                                     :owner-avatar-url :owner_avatar_url})
                                       (merge {:state 0})))
     (db/get-repo {:repo (:name repo)
                   :owner (:owner repo)}))))

(defn get-enabled
  "Lists enabled repository id's for a given user-id"
  [user-id]
  (mapcat
   vals
    (jdbc/with-db-connection [con-db *db*]
      (db/get-enabled-repositories con-db {:user_id user-id}))))

(defn update-repo-name
  [repo-id repo-name]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-repo-name con-db {:repo_id repo-id
                                 :repo_name repo-name})))

(defn update-repo-state
  [repo-id repo-state]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-repo-state con-db {:repo_id repo-id
                                 :repo_state repo-state})))
(defn get-repo
  "Get a repo from DB given it's full name (owner/repo-name)"
  [full-name]
  (let [[owner repo-name] (str/split full-name #"/")]
    (jdbc/with-db-connection [con-db *db*]
      (db/get-repo {:owner owner
                    :repo repo-name}))))
