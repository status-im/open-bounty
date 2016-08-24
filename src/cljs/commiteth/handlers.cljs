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
    (dispatch [:load-user-profile])
    (dispatch [:load-user-repos])
    (dispatch [:load-enabled-repos])
    (assoc db :user user)))

(reg-event-db
  :load-user-profile
  (fn [db [_]]
    (GET "/api/user"
      {:headers {"Accept" "application/transit+json"}
       :handler #(dispatch [:set-user-profile %])})
    db))

(reg-event-db
  :set-user-profile
  (fn [db [_ user-profile]]
    (assoc db :user-profile user-profile)))

(reg-event-db
  :set-user-repos
  (fn [db [_ repos]]
    (assoc db :repos repos)))

(reg-event-db
  :load-user-repos
  (fn [db [_]]
    (GET "/api/user/repositories"
      {:headers {"Accept" "application/transit+json"}
       :handler #(dispatch [:set-user-repos (:repositories %)])})
    db))

(reg-event-db
  :set-enabled-repos
  (fn [db [_ repos]]
    (assoc db :enabled-repos (zipmap repos (repeat true)))))

(reg-event-db
  :load-enabled-repos
  (fn [db [_]]
    (GET "/api/repositories"
      {:headers {"Accept" "application/transit+json"}
       :handler #(dispatch [:set-enabled-repos %])})
    db))

(reg-event-db
  :toggle-repo
  (fn [db [_ repo]]
    (POST "/api/repository/toggle"
      {:headers {"Accept" "application/transit+json"}
       :params  (select-keys repo [:id :login :name])
       :handler #(println %)})
    db))

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
