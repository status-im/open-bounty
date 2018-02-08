(ns commiteth.subscriptions
  (:require [re-frame.core :refer [reg-sub]]
            [commiteth.common :refer [items-per-page]]))

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
  :user-profile-loaded?
  (fn [db _]
    (:user-profile-loaded? db)))

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
    (vec (:open-bounties db))))

(reg-sub
  :page-number
  (fn [db _]
    (:page-number db)))

(reg-sub
  :open-bounties-page
  :<- [:open-bounties]
  :<- [:page-number]
  (fn [[open-bounties page-number] _]
    (let [total-count (count open-bounties)
          start (* (dec page-number) items-per-page)
          end (min total-count (+ items-per-page start))
          items (subvec open-bounties start end)]
      {:items items
       :item-count (count items)
       :total-count total-count
       :page-number page-number
       :page-count (Math/ceil (/ total-count items-per-page))})))


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
    (vec (:activity-feed db))))

(reg-sub
  :activities-page
  :<- [:activity-feed]
  :<- [:page-number]
  (fn [[activities page-number] _]
    (let [total-count (count activities)
          start (* (dec page-number) items-per-page)
          end (min total-count (+ items-per-page start))
          items (subvec activities start end)]
      {:items items
       :item-count (count items)
       :total-count total-count
       :page-number page-number
       :page-count (Math/ceil (/ total-count items-per-page))})))


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
