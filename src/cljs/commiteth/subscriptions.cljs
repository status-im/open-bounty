(ns commiteth.subscriptions
  (:require [re-frame.core :refer [reg-sub]]))

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
  :error
  (fn [db _]
    (:error db)))

(reg-sub
  :all-bounties
  (fn [db _]
    (:all-bounties db)))

(reg-sub
  :owner-bounties
  (fn [db _]
    (:owner-bounties db)))

(reg-sub
  :pagination
  (fn [db [_ table]]
    (get-in db [:pagination table])))

(reg-sub
  :get-in
  (fn [db [_ path]]
    (get-in db path)))
