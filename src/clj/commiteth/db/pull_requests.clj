(ns commiteth.db.pull-requests
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]))

(defn create
  "Creates pull-request"
  [pull-request]
  (jdbc/with-db-connection [con-db *db*]
    (db/create-pull-request! con-db pull-request)))
