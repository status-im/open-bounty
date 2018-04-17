(ns commiteth.subscriptions
  (:require [re-frame.core :refer [reg-sub]]
            [commiteth.db :as db]
            [commiteth.ui-model :as ui-model]
            [commiteth.common :refer [items-per-page]]
            [clojure.string :as string]))

(reg-sub
  :db
  (fn [db _] db))

(reg-sub
 :route
 (fn [db _]
   (:route db)))

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
  :<- [::filtered-and-sorted-open-bounties]
  :<- [:page-number]
  :<- [:activity-feed]
  (fn [[open-bounties page-number activity-feed] _]
    (let [total-count (count open-bounties)
          start       (* (dec page-number) items-per-page)
          end         (min total-count (+ items-per-page start))
          items       (->> (subvec open-bounties start end)
                           (map (fn [bounty]
                                  (let [matching-claims (filter
                                                         (fn [claim]
                                                           (= (:issue-id claim)
                                                              (:issue-id bounty)))
                                                         activity-feed)]
                                    (assoc bounty :claims matching-claims)))))]
      {:items       items
       :item-count  (count items)
       :total-count total-count
       :page-number page-number
       :page-count  (Math/ceil (/ total-count items-per-page))})))

(reg-sub
  :owner-bounties
  (fn [db _]
    (->> (for [[id bounty] (:owner-bounties db)]
           ;; TODO(martinklepsch) we might want to consider using a
           ;; special prefix or namespace for derived properties that
           ;; are added to domain records like this
           ;; e.g. `derived/paid?`
           [id (assoc bounty :paid? (boolean (:payout_hash bounty)))])
         (into {}))))

(reg-sub
 :owner-bounties-stats
 :<- [:owner-bounties]
 (fn [owner-bounties _]
   (let [sum-dollars (fn sum-dollars [bounties]
                       (reduce + (map #(js/parseFloat (:value-usd %)) bounties)))
         {:keys [paid unpaid]} (group-by #(if (:paid? %) :paid :unpaid)
                                         (vals owner-bounties))]
     {:paid {:count (count paid)
             :combined-usd-value (sum-dollars paid)}
      :unpaid {:count (count unpaid)
               :combined-usd-value (sum-dollars unpaid)}})))

(reg-sub
 :dashboard/seen-banners
 (fn [db _] (:dashboard/seen-banners db)))

(reg-sub
 :dashboard/banner-msg
 :<- [:user]
 :<- [:dashboard/seen-banners]
 (fn [[user seen-banners] _]
   (cond
     (not (contains? seen-banners "bounty-issuer-salute"))
     {:name (or (some-> (:name user) (string/split  #"\s") first)
                (:login user))
      :banner-id "bounty-issuer-salute"}

     #_(not (contains? seen-banners "new-dashboard-info"))
     #_{:banner-id "new-dashboard-info"})))

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

(reg-sub
  ::open-bounty-claims
  (fn [db _]
    (::db/open-bounty-claims db)))

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
  ::open-bounties-owners-sorted
  :<- [::open-bounties-owners]
  (fn [owners _]
    (sort-by string/lower-case owners)))

(reg-sub
  ::open-bounties-currencies
  :<- [:open-bounties]
  (fn [open-bounties _]
    (let [token-ids (->> open-bounties
                         (map :tokens)
                         (mapcat keys)
                         (filter identity)
                         set)]
      (into #{:ETH} token-ids))))

(reg-sub
  ::filtered-and-sorted-open-bounties
  :<- [:open-bounties]
  :<- [::open-bounties-filters]
  :<- [::open-bounties-sorting-type]
  (fn [[open-bounties filters sorting-type] _]
    (cond->> open-bounties
             true (ui-model/filter-bounties filters)
             sorting-type (ui-model/sort-bounties-by-sorting-type sorting-type)
             true vec)))
