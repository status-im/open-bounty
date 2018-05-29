(ns commiteth.test.db.core
  (:require [commiteth.db.core :refer [*db*] :as db]
            [luminus-migrations.core :as migrations]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [commiteth.config :refer [env]]
            [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'commiteth.config/env
      #'commiteth.db.core/*db*)
    (migrations/migrate ["migrate"]
                        {:database-url (env :jdbc-database-url)})
    (f)))

(deftest test-users
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (is (not (nil? (db/create-user!
                    t-conn
                    {:id      1
                     :login   "torvalds"
                     :name    "Linus Torvalds"
                     :avatar_url ""
                     :email   nil
                     :address "address"
                     :created nil}))))
    (is (= {:id      1
            :login   "torvalds"
            :name    "Linus Torvalds"
            :email   nil
            :avatar-url ""
            :address "address"
            :created nil
            :welcome-email-sent 0
            :is-hidden-in-hunters false}
          (db/get-user t-conn {:id 1})))))
