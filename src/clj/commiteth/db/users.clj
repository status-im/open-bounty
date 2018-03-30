(ns commiteth.db.users
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log])
  (:import [java.util Date]))

(defn create-user
  [user-id login name email avatar-url]
  (jdbc/with-db-connection [con-db *db*]
    (db/create-user! con-db
      {:id      user-id
       :login   login
       :name    name
       :email   email
       :avatar_url avatar-url
       :address nil
       :created (new Date)})))

(defn get-user
  [user-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/get-user con-db {:id user-id})))

(defn exists?
  [user-id]
  (jdbc/with-db-connection [con-db *db*]
    (some? (db/get-user con-db {:id user-id}))))

(defn get-repo-owner
  "Gets repository owner by given repository id"
  [repo-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/get-repo-owner {:repo_id repo-id})))

(defn update-user
  [user-id body]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-user! con-db {:id user-id
                             :address (:address body)
                             :opts (dissoc body :address)})))
