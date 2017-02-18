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
                                                     :name :repo})
                                       (merge {:state 0})))
     (db/get-repo {:repo (:name repo)
                   :login (:login repo)}))))

(defn get-enabled
  "Lists enabled repositories ids for a given login"
  [user-id]
  (->>
    (jdbc/with-db-connection [con-db *db*]
      (db/get-enabled-repositories con-db {:user_id user-id}))
    (mapcat vals)))

(defn update-repo
  [repo-id updates]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-repo-generic con-db {:repo_id repo-id
                                    :updates updates})))


(defn get-repo
  "Get a repo from DB given it's full name (owner/repo-name)"
  [full-name]
  (let [[login repo-name] (str/split full-name #"/")]
    (jdbc/with-db-connection [con-db *db*]
      (db/get-repo {:login login :repo repo-name}))))
