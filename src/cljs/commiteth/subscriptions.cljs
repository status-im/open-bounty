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
  :repos
  (fn [db _]
    (:repos db)))

(reg-sub
  :get-in
  (fn [db [_ path]]
    (get-in db path)))

(def user-address-path [:user-profile :user :address])
