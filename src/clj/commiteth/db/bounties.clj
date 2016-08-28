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
    (db/issues-list con-db {:owner_id owner-id})))
