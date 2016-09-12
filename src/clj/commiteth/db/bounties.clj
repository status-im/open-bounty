(ns commiteth.db.bounties
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]))


(defn list-fixed-issues
  [owner-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/bounties-list con-db {:owner_id owner-id})))

(defn list-not-fixed-issues
  [owner-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/owner-issues-list con-db {:owner_id owner-id})))

(defn pending-bounties-list
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/pending-bounties-list con-db)))

(defn update-confirm-hash
  [issue-id confirm-hash]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-confirm-hash con-db {:issue_id issue-id :confirm_hash confirm-hash})))

(defn get-bounty-address
  [user repo issue-number]
  (jdbc/with-db-connection [con-db *db*]
    (db/get-bounty-address con-db {:login user :repo repo :issue_number issue-number})))

(defn list-wallets
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/wallets-list con-db)))
