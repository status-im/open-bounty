(ns commiteth.db.issues
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]))

(defn create
  "Creates issue"
  [repo-id issue-id issue-number issue-title]
  (jdbc/with-db-connection [con-db *db*]
    (db/create-issue! con-db {:repo_id      repo-id
                              :issue_id     issue-id
                              :issue_number issue-number
                              :title        issue-title})))

(defn close
  "Updates issue with commit_id"
  [commit-id issue-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/close-issue! con-db {:issue_id issue-id :commit_id commit-id})))

(defn update-transaction-hash
  "Updates issue with transaction-hash"
  [issue-id transaction-hash]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-transaction-hash con-db {:issue_id         issue-id
                                        :transaction_hash transaction-hash})))

(defn update-contract-address
  "Updates issue with contract-address"
  [issue-id contract-address]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-contract-address con-db {:issue_id         issue-id
                                        :contract_address contract-address})))

(defn list-pending-deployments
  "Retrieves pending transaction ids"
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/list-pending-deployments con-db)))
