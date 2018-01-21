(ns commiteth.subscriptions
  (:require [re-frame.core :refer [reg-sub]]
            [commiteth.db :as db]
            [commiteth.ui-model :as ui-model]))

(reg-sub
  :db
  (fn [db _] db))

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
  :user
  (fn [db _]
    (:user db)))

(reg-sub
  :repos-loading?
  (fn [db _]
    (:repos-loading? db)))

(reg-sub
  :repos
  (fn [db _]
    (:repos db)))

(reg-sub
  :flash-message
  (fn [db _]
    (:flash-message db)))

(reg-sub
  :open-bounties
  (fn [db _]
    (:open-bounties db)))

(reg-sub
  :owner-bounties
  (fn [db _]
    (:owner-bounties db)))

(reg-sub
  :pagination
  (fn [db [_ table]]
    (get-in db [:pagination table])))

(reg-sub
  :top-hunters
  (fn [db _]
    (:top-hunters db)))

(reg-sub
  :activity-feed
  (fn [db _]
    (:activity-feed db)))

(reg-sub
  :gh-admin-token
  (fn [db _]
    (let [login (get-in db [:user :login])]
      (get-in db [:tokens login :gh-admin-token]))))

(reg-sub
  :get-in
  (fn [db [_ path]]
    (get-in db path)))

(reg-sub
  :usage-metrics
  (fn [db _]
    (:usage-metrics db)))

(reg-sub
  :metrics-loading?
  (fn [db _]
    (:metrics-loading? db)))

(reg-sub
  :user-dropdown-open?
  (fn [db _]
    (:user-dropdown-open? db)))

(reg-sub
  ::open-bounties-sorting-type
  (fn [db _]
    (::db/open-bounties-sorting-type db)))

(reg-sub
  ::open-bounties-filters
  (fn [db _]
    (::db/open-bounties-filters db)))

(reg-sub
  ::open-bounties-owners
  :<- [:open-bounties]
  (fn [open-bounties _]
    (->> open-bounties
         (map :repo-owner)
         set)))

(reg-sub
  ::open-bounties-currencies
  :<- [:open-bounties]
  (fn [open-bounties _]
    (let [token-ids (->> open-bounties
                         (map :tokens)
                         (map keys)
                         (filter identity)
                         set)]
      (into #{"ETH"} token-ids))))

(reg-sub
  ::filtered-and-sorted-open-bounties
  :<- [:open-bounties]
  :<- [::open-bounties-sorting-type]
  (fn [[open-bounties sorting-type] _]
    (println "RAW" open-bounties)
    (cond->> open-bounties
             sorting-type (ui-model/sort-bounties-by-sorting-type sorting-type)
             )))
