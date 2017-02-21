(ns commiteth.db.comment-images
  (:require [commiteth.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]))

(defn save-image!
  [issue-id png-data]
  (jdbc/with-db-connection [con-db *db*]
    (db/save-issue-comment-image! con-db
                                  {:issue_id issue-id
                                   :png_data png-data})))

(defn get-image-data
  [issue-id]
  (jdbc/with-db-connection [con-db *db*]
    (db/get-issue-comment-image con-db {:issue_id issue-id})))
