(ns commiteth.db.users
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc])
  (:import [java.util Date]))

(defn create-user
  [user-id login name email token]
  (jdbc/with-db-connection [con-db *db*]
    (db/create-user! con-db
      {:id      user-id
       :login   login
       :name    name
       :email   email
       :token   token
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

(defn update-user-address
  [user-id address]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-user-address! con-db {:id user-id :address address})))

(defn update-user-token
  "Updates user token and returns updated user"
  [user-id token]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-user-token! con-db {:id user-id :token token})))
