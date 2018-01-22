(ns commiteth.bounties
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [commiteth.common :refer [moment-timestamp
                                      display-data-page
                                      scroll-div
                                      items-per-page
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
         value-usd :value-usd
         claim-count :claim-count} bounty
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
       [:span.usd-value-label "Value "] [:span.usd-balance-label (str "$" value-usd)]
       (when (> claim-count 0)
         [:span.open-claims-label (str claim-count " open claim"
                                       (when (> claim-count 1) "s"))]) ]]
     [:div.open-bounty-item-icon
      [:div.ui.tiny.circular.image
       [:img {:src avatar-url}]]]]))

(defn bounties-list [{:keys [items item-count page-number total-count] 
                      :as bounty-page-data}]
  (if (empty? items)
    [:div.view-no-data-container
     [:p "No recent activity yet"]]
    [:div
     (let [left (inc (* (dec page-number) items-per-page))
           right (dec (+ left item-count))]
       [:div.item-counts-label
        [:span (str "Showing " left "-" right " of " total-count)]])
     (display-data-page bounty-page-data bounty-item)]))

(defn bounties-page []
  (let [bounty-page-data (rf/subscribe [:open-bounties-page])
        open-bounties-loading? (rf/subscribe [:get-in [:open-bounties-loading?]])
        container-element (atom nil)] 
    (fn []
      (if @open-bounties-loading?
        [:div.view-loading-container
         [:div.ui.active.inverted.dimmer
          [:div.ui.text.loader.view-loading-label "Loading"]]]
        [:div.ui.container.open-bounties-container
         {:ref #(reset! container-element %1)} 
         [scroll-div container-element]
         [:div.open-bounties-header "Bounties"]
         [bounties-list @bounty-page-data]]))
    ))
