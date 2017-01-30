(ns commiteth.handlers
  (:require [commiteth.db :as db]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-fx]]
            [ajax.core :refer [GET POST]]
            [cuerdas.core :as str]))

(reg-fx
  :http
  (fn [{:keys [method url on-success params]}]
    (method url
      {:headers {"Accept" "application/transit+json"}
       :handler on-success
       :params  params})))

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :assoc-in
  (fn [db [_ path value]]
    (assoc-in db path value)))

(reg-event-db
  :set-error
  (fn [db [_ text]]
    (assoc db :error text)))

(reg-event-db
  :clear-error
  (fn [db _]
    (dissoc db :error)))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
  :set-page
  (fn [db [_ table page]]
    (assoc-in db [:pagination table :page] page)))

(reg-event-db
  :init-pagination
  (fn [db [_ bounties]]
    (let [{page-size :page-size} (:pagination-props db)]
      (assoc-in db [:pagination :all-bounties]
        {:page  0
         :pages (Math/ceil (/ (count bounties) page-size))}))))

(reg-event-fx
  :set-active-user
  (fn [{:keys [db]} [_ user]]
    {:db         (assoc db :user user)
     :dispatch-n [[:load-user-profile]
                  [:load-user-repos]
                  [:load-enabled-repos]
                  [:load-owner-bounties]]}))

(reg-event-fx
  :load-bounties
  (fn [{:keys [db]} [_]]
    {:db   db
     :http {:method     GET
            :url        "/api/bounties"
            :on-success #(dispatch [:set-bounties %])}}))

(reg-event-fx
  :save-payout-hash
  (fn [{:keys [db]} [_ issue-id payout-hash]]
    {:db   db
     :http {:method     POST
            :url        (str/format "/api/bounty/%s/payout" issue-id)
            :on-success #(println %)
            :params     {:payout-hash payout-hash}}}))

(reg-event-fx
  :set-bounties
  (fn [{:keys [db]} [_ bounties]]
    {:db       (assoc db :all-bounties bounties)
     :dispatch [:init-pagination bounties]}))

(reg-event-fx
  :load-owner-bounties
  (fn [{:keys [db]} [_]]
    {:db   db
     :http {:method     GET
            :url        "/api/user/bounties"
            :on-success #(dispatch [:set-owner-bounties %])}}))

(reg-event-db
  :set-owner-bounties
  (fn [db [_ issues]]
    (assoc db :owner-bounties issues)))

(reg-event-fx
  :load-user-profile
  (fn [{:keys [db]} [_]]
    {:db   db
     :http {:method     GET
            :url        "/api/user"
            :on-success #(dispatch [:set-user-profile %])}}))

(reg-event-db
  :set-user-profile
  (fn [db [_ user-profile]]
    (assoc db :user-profile user-profile)))

(reg-event-db
  :set-user-repos
  (fn [db [_ repos]]
    (-> db
        (assoc :repos repos)
        (assoc :repos-loading? false))))

(reg-event-fx
  :load-user-repos
  (fn [{:keys [db]} [_]]
    {:db   (assoc db :repos-loading? true)
     :http {:method     GET
            :url        "/api/user/repositories"
            :on-success #(dispatch [:set-user-repos (:repositories %)])}}))

(reg-event-db
  :set-enabled-repos
  (fn [db [_ repos]]
    (assoc db :enabled-repos (zipmap repos (repeat true)))))

(reg-event-fx
  :load-enabled-repos
  (fn [{:keys [db]} [_]]
    {:db   db
     :http {:method     GET
            :url        "/api/repositories"
            :on-success #(dispatch [:set-enabled-repos %])}}))

(reg-event-fx
  :toggle-repo
  (fn [{:keys [db]} [_ repo]]
    (println "toggle-repo" repo)
    {:db   db
     :http {:method     POST
            :url        "/api/repository/toggle"
            :on-success #(println %)
            :params     (select-keys repo [:id :login :full_name :name])}}))

(reg-event-fx
  :save-user-address
  (fn [{:keys [db]} [_ user-id address]]
    {:db   db
     :http {:method     POST
            :url        "/api/user/address"
            :on-success #(println %)
            :params     {:user-id user-id :address address}}}))
