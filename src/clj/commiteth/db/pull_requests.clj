(ns commiteth.db.pull-requests
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))


(defn save
  "Creates or updates a pull-request"
  [pull-request]
  (let [state (case (:state pull-request)
                :opened 0
                :merged 1
                :closed 2
                ;; allow for case of proper state already set
                (:state pull-request))]

    (log/debug "save pr" (assoc pull-request :state state))
    (jdbc/with-db-connection [con-db *db*]
      (db/save-pull-request! con-db
                             (assoc pull-request :state state)))))

(defn remove
  [pr-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/remove-pull-request! con-db
                             {:pr_id pr-id})))
