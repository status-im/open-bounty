(ns commiteth.activity
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [commiteth.common :refer [moment-timestamp
                                      issue-url]]))


(defn item-description [{:keys [display-name
                                value-usd
                                issue-title
                                item-type
                                repo-owner
                                repo-name
                                issue-number
                                user-has-address] :as item}]
  (let [issue-link [:a
                    {:href (issue-url repo-owner repo-name issue-number)}
                    issue-title]]
    (case item-type
      "new-bounty" [:div "New bounty opened for issue " issue-link]
      "claim-payout" [:div "Received USD " value-usd
                      " for " issue-link]
      "open-claim" [:div "Submitted a claim for " issue-link]
      "balance-update" [:div issue-link " bounty increased to USD " value-usd]
      "claim-pending" [:div "Won USD " value-usd " for " issue-link
                       (if user-has-address
                         " (payout pending maintainer confirmation)"
                         " (payout pending user to update ETH address)")]
      "")))


(defn activity-item [{:keys [avatar-url
                             display-name
                             updated
                             value-usd
                             balance-eth
                             issue-title
                             item-type
                             tokens] :as item}]
  [:div.item.activity-item
   [:div.ui.mini.circular.image
    [:img {:src avatar-url}]]
   [:div.content
    [:div.header.display-name display-name]
    [:div.description
     [item-description item]]
    [:div.footer-row
     (when-not (= item-type "new-bounty")
       [:div
        [:div.balance-badge "ETH " balance-eth]
        (for [[tla balance] tokens]
          ^{:key (random-uuid)}
          [:div.balance-badge.token
           (str (subs (str tla) 1) " " balance)])])
     [:div.time (moment-timestamp updated)]]]])



(defn activity-list [activity-items]
  [:div.ui.container.activity-container
   (if (empty? activity-items)
     [:div.view-no-data-container
      [:p "No recent activity yet"]]
     (into [:div.ui.items]
           (for [item activity-items]
             ^{:key item} [activity-item item])))]  )

(defn activity-page []
  (let [activity-items (rf/subscribe [:activity-feed])
        activity-feed-loading? (rf/subscribe [:get-in [:activity-feed-loading?]])]
    (fn []
      (if @activity-feed-loading?
        [:div.view-loading-container
         [:div.ui.active.inverted.dimmer
          [:div.ui.text.loader.view-loading-label "Loading"]]]
        [activity-list @activity-items]))))
