(ns commiteth.db.usage-metrics
    (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(defn usage-metrics-by-day
  ([] (usage-metrics-by-day 30))
  ([limit-days]
   (jdbc/with-db-connection [con-db *db*]
     (db/usage-metrics-by-day {:limit_days limit-days}))))
