(ns commiteth.db.bounties
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]))



(defn list-owner-bounties
  [owner]
  (jdbc/with-db-connection [con-db *db*]
    (db/owner-bounties-list con-db {:owner_id owner})))

(defn bounty-claims
  [issue-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/bounty-claims con-db {
                              :issue_id issue-id})))

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

(defn confirmed-payouts-list
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/confirmed-payouts-list con-db)))

(defn update-confirm-hash
  [issue-id confirm-hash]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-confirm-hash con-db {:issue_id issue-id :confirm_hash confirm-hash})))

(defn update-execute-hash
  [issue-id execute-hash]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-execute-hash con-db {:issue_id issue-id :execute_hash execute-hash})))

(defn update-payout-hash
  [issue-id payout-hash]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-payout-hash con-db {:issue_id issue-id :payout_hash payout-hash})))

(defn update-payout-receipt
  [issue-id payout-receipt]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-payout-receipt con-db {:issue_id issue-id :payout_receipt payout-receipt})))

(defn get-bounty
  [owner repo issue-number]
  (jdbc/with-db-connection [con-db *db*]
    (db/get-bounty con-db {:owner owner :repo repo :issue_number issue-number})))


(defn open-bounty-contracts
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/open-bounty-contracts con-db)))

(defn top-hunters
  []
    (jdbc/with-db-connection [con-db *db*]
      (db/top-hunters con-db)))

(defn bounty-activity
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/bounties-activity con-db)))
