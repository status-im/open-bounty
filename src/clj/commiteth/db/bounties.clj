(ns commiteth.db.bounties
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]))


(defn list-all-bounties
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/all-bounties-list con-db)))

(defn list-owner-bounties
  [owner]
  (jdbc/with-db-connection [con-db *db*]
    (db/owner-bounties-list con-db {:owner_id owner})))

(defn list-not-fixed-issues
  [owner-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/owner-issues-list con-db {:owner_id owner-id})))

(defn pending-bounties-list
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/pending-bounties-list con-db)))

(defn pending-payouts-list
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/pending-payouts-list con-db)))

(defn update-confirm-hash
  [issue-id confirm-hash]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-confirm-hash con-db {:issue_id issue-id :confirm_hash confirm-hash})))

(defn update-execute-hash
  [issue-id execute-hash]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-execute-hash con-db {:issue_id issue-id :execute_hash execute-hash})))

(defn get-bounty-address
  [user repo issue-number]
  (jdbc/with-db-connection [con-db *db*]
    (db/get-bounty-address con-db {:login user :repo repo :issue_number issue-number})))

(defn list-wallets
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/wallets-list con-db)))
