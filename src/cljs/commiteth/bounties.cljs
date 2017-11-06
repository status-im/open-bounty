(ns commiteth.bounties
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [moment-timestamp
                                      issue-url]]))


(defn bounty-item [bounty]
  (let [{avatar-url :repo_owner_avatar_url
         owner :repo-owner
         repo-name :repo-name
         issue-title :issue-title
         issue-number :issue-number
         updated :updated
         tokens :tokens
         balance-eth :balance-eth
         value-usd :value-usd} bounty
        full-repo (str owner "/" repo-name)
        repo-url (str "https://github.com/" full-repo)
        repo-link [:a {:href repo-url} full-repo]
        issue-link [:a
                    {:href (issue-url owner repo-name issue-number)}
                    issue-title]]
    [:div.open-bounty-item
     [:div.open-bounty-item-content
      [:div.header issue-link]
      [:div.bounty-item-row
       [:div.time (moment-timestamp updated)]
       [:span.bounty-repo-label repo-link]]

      [:div.footer-row
       [:div.balance-badge "ETH " balance-eth]
       (for [[tla balance] tokens]
         ^{:key (random-uuid)}
         [:div.balance-badge.token
          (str (subs (str tla) 1) " " balance)])
       [:span.usd-value-label "Value "] [:span.usd-balance-label (str "$" value-usd)]]]
     [:div.open-bounty-item-icon
      [:div.ui.tiny.circular.image
       [:img {:src avatar-url}]]]]))

(defn bounties-list [open-bounties]
  [:div.ui.container.open-bounties-container
   [:div.open-bounties-header "Bounties"]
   (if (empty? open-bounties)
     [:div.view-no-data-container
      [:p "No recent activity yet"]]
     (into [:div.ui.items]
           (for [bounty open-bounties]
             [bounty-item bounty])))])


(defn bounties-page []
  (let [open-bounties (rf/subscribe [:open-bounties])
        open-bounties-loading? (rf/subscribe [:get-in [:open-bounties-loading?]])]
    (fn []
      (if @open-bounties-loading?
        [:div.view-loading-container
         [:div.ui.active.inverted.dimmer
          [:div.ui.text.loader.view-loading-label "Loading"]]]
        [bounties-list @open-bounties]))))
