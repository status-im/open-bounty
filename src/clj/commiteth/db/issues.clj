(ns commiteth.db.issues
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [commiteth.util.util :refer [to-db-map]]
            [clojure.set :refer [rename-keys]]
            [clojure.tools.logging :as log]))

(defn create
  "Creates issue"
  [repo-id issue-id issue-number issue-title]
  (jdbc/with-db-connection [con-db *db*]
    (db/create-issue! con-db {:repo_id      repo-id
                              :issue_id     issue-id
                              :issue_number issue-number
                              :title        issue-title})))

(defn update-commit-sha
  "Updates issue with commit-sha"
  [issue-id commit-sha]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-commit-sha con-db {:issue_id issue-id
                                  :commit_sha commit-sha})))

(defn get-issue-titles
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/get-issue-titles con-db {})))

(defn get-issues-count
  [repo-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/get-issues-count con-db {:repo_id repo-id})))

(defn update-issue-title
  [issue-id title]
  (log/info "issue %s: Updating changed title \"%s\"" issue-id title)
  (jdbc/with-db-connection [con-db *db*]
    (db/update-issue-title con-db {:issue_id issue-id
                                   :title title})))


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

(defn update-comment-id
  "Updates issue with comment id"
  [issue-id comment-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-comment-id con-db {:issue_id   issue-id
                                  :comment_id comment-id})))

(defn list-pending-deployments
  "Retrieves pending transaction ids"
  []
  (jdbc/with-db-connection [con-db *db*]
    (db/list-pending-deployments con-db)))

(defn update-balances
  [contract-address balance-eth token-balances usd-value]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-balances con-db (to-db-map contract-address
                                                balance-eth
                                                token-balances
                                                usd-value))))

(defn update-open-status
  [issue-id is-open]
  (jdbc/with-db-connection [con-db *db*]
    (db/update-issue-open con-db {:issue_id issue-id
                                  :is_open is-open})))

(defn is-bounty-issue?
  [issue-id]
  (let [res (jdbc/with-db-connection [con-db *db*]
              (db/issue-exists con-db {:issue_id issue-id}))]
    (-> res
        first
        :exists
        boolean)))

(defn get-issue
  [repo-id issue-number]
  (jdbc/with-db-connection [con-db *db*]
    (db/get-issue con-db {:repo_id repo-id
                          :issue_number issue-number})))
