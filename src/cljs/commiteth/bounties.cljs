(ns commiteth.bounties
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [moment-timestamp
                                      issue-url]]))


(defn bounty-item [bounty]
  (let [{avatar-url :avatar-url
         owner :repo-owner
         repo-name :repo-name
         issue-title :issue-title
         issue-number :issue-number
         updated :updated
         tokens :tokens
         balance-eth :balance-eth
         value-usd :value-usd} bounty
        full-repo (str owner "/" repo-name)
        issue-link [:a
                    {:href (issue-url owner repo-name issue-number)}
                    issue-title]]
    [:div.item.activity-item
     [:div.ui.mini.circular.image
      [:img {:src avatar-url}]]
     [:div.content
      [:div.header.display-name full-repo]
      [:div.description
       issue-link
       (str " (USD " value-usd ")")]
      [:div.footer-row
       (for [[tla balance] (merge tokens {:ETH balance-eth})]
         ^{:key (random-uuid)}
         [:div.balance-badge
          (str (subs (str tla) 1) " " balance)])
       [:div.time (moment-timestamp updated)]]]]))

(defn bounties-list [open-bounties]
  [:div.ui.container.activity-container
   (if (empty? open-bounties)
     [:div.ui.text "No data"]
     (into [:div.ui.items]
           (for [bounty open-bounties]
             [bounty-item bounty])))])


(defn bounties-page []
  (let [open-bounties (rf/subscribe [:open-bounties])
        open-bounties-loading? (rf/subscribe [:get-in [:open-bounties-loading?]])]
    (fn []
      (if @open-bounties-loading?
        [:container
         [:div.ui.active.inverted.dimmer
          [:div.ui.text.loader "Loading"]]]
        [bounties-list @open-bounties]))))
