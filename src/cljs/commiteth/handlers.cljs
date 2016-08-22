(ns commiteth.handlers
  (:require [commiteth.db :as db]
            [re-frame.core :refer [dispatch reg-event-db]]
            [ajax.core :refer [GET POST]]))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :assoc-in
  (fn [db [_ path value]]
    (assoc-in db path value)))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
  :set-active-user
  (fn [db [_ user]]
    (assoc db :user user)))

(reg-event-db
  :set-user-address
  (fn [db [_ address]]
    (assoc db :address address)))

(defn save-user-address [params]
  (POST "/api/user/address"
    {:headers {"Accept" "application/transit+json"}
     :params  params
     :handler #(println %)}))

(reg-event-db
  :save-user-address
  (fn [db [_ user address]]
    (save-user-address {:user user :address address})
    db))
