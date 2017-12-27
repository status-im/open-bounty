(ns commiteth.db.core
  (:require
   [cheshire.core :refer [generate-string parse-string]]
   [clojure.java.jdbc :as jdbc]
   [conman.core :as conman]
   [commiteth.config :refer [env]]
   [mount.core :refer [defstate] :as mount]
   [migratus.core :as migratus]
   [mpg.core :as mpg]
   [clojure.string :as str])
  (:import org.postgresql.util.PGobject
           java.sql.Array
           clojure.lang.IPersistentMap
           clojure.lang.IPersistentVector
           [java.sql
            BatchUpdateException
            Date
            Timestamp
            PreparedStatement]))

(mpg/patch)

(defn db-start []
  (let [db (env :jdbc-database-url)
        migratus-config {:store :database
                         :migration-dir "migrations/"
                         :migration-table-name "schema_migrations"
                         :db db}]
    (migratus/migrate migratus-config)
    (conman/connect! {:jdbc-url db})
    db))

(defstate ^:dynamic *db*
  :start (db-start)
  :stop (conman/disconnect! *db*))

(defn start! []
  (mount/start #'*db*))

(defn stop! []
  (mount/stop #'*db*))

(conman/bind-connection *db* "sql/queries.sql")

(defn to-date [^java.sql.Date sql-date]
  (-> sql-date (.getTime) (java.util.Date.)))

(extend-protocol jdbc/IResultSetReadColumn
  Date
  (result-set-read-column [v _ _] (to-date v))

  Timestamp
  (result-set-read-column [v _ _] (to-date v))

  Array
  (result-set-read-column [v _ _] (vec (.getArray v)))

  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (let [type  (.getType pgobj)
          value (.getValue pgobj)]
      (case type
        "json" (parse-string value true)
        "jsonb" (parse-string value true)
        "citext" (str value)
        value))))

(extend-type java.util.Date
  jdbc/ISQLParameter
  (set-parameter [v ^PreparedStatement stmt ^long idx]
    (.setTimestamp stmt idx (Timestamp. (.getTime v)))))

(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (generate-string value))))

(extend-type clojure.lang.IPersistentVector
  jdbc/ISQLParameter
  (set-parameter [v ^java.sql.PreparedStatement stmt ^long idx]
    (let [conn      (.getConnection stmt)
          meta      (.getParameterMetaData stmt)
          type-name (.getParameterTypeName meta idx)]
      (if-let [elem-type (when (= (first type-name) \_) (str/join (rest type-name)))]
        (.setObject stmt idx (.createArrayOf conn elem-type (to-array v)))
        (.setObject stmt idx (to-pg-json v))))))

(extend-protocol jdbc/ISQLValue
  IPersistentMap
  (sql-value [value] (to-pg-json value))
  IPersistentVector
  (sql-value [value] (to-pg-json value)))

(defmacro with-trx [& body]
  "Performs a set of queries in transaction."
  `(conman/with-transaction [*db*]
     ~@body))

(defn update! [& args]
  (apply jdbc/update! *db* args))
