(ns commiteth.db.bounties
  (:require [commiteth.db.core :refer [*db*] :as db]
            [commiteth.util.util :refer [to-db-map]]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]))



(defn pending-contracts
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/pending-contracts con-db)))

(defn owner-bounties
  [owner-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/owner-bounties con-db {:owner_id owner-id})))

(defn open-bounties
  []
    (jdbc/with-db-connection [con-db *db*]
      (db/open-bounties con-db)))

(defn bounty-claims
  [issue-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/bounty-claims con-db {:issue_id issue-id})))

(defn pending-bounties
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/pending-bounties con-db)))

(defn pending-payouts
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/pending-payouts con-db)))

(defn confirmed-payouts
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/confirmed-payouts con-db)))

(defn update-confirm-hash
  [issue-id confirm-hash]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-confirm-hash con-db (to-db-map issue-id confirm-hash))))

(defn update-execute-hash-and-winner-login
  [issue-id execute-hash winner-login]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-execute-hash-and-winner-login con-db 
                                             (to-db-map issue-id execute-hash winner-login)
                                             )))

(defn update-watch-hash
  [issue-id watch-hash]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-watch-hash con-db (to-db-map issue-id watch-hash))))

(defn pending-watch-calls
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/pending-watch-calls con-db)))

(defn update-payout-hash
  [issue-id payout-hash]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-payout-hash con-db (to-db-map issue-id payout-hash))))

(defn reset-payout-hash
  [issue-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/reset-payout-hash con-db {:issue_id issue-id})))

(defn update-payout-receipt
  [issue-id payout-receipt]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-payout-receipt con-db (to-db-map issue-id payout-receipt))))

(defn get-bounty
  [owner repo issue-number]
  (jdbc/with-db-connection [con-db *db*]
    (db/get-bounty con-db (to-db-map owner repo issue-number))))

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
