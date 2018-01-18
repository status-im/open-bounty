(ns commiteth.bounties
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [commiteth.common :refer [moment-timestamp
                                      issue-url]]
            [commiteth.handlers :as handlers]
            [commiteth.db :as db]))


(defn bounty-item [bounty]
  (let [{avatar-url   :repo_owner_avatar_url
         owner        :repo-owner
         repo-name    :repo-name
         issue-title  :issue-title
         issue-number :issue-number
         updated      :updated
         tokens       :tokens
         balance-eth  :balance-eth
         value-usd    :value-usd
         claim-count  :claim-count} bounty
        full-repo  (str owner "/" repo-name)
        repo-url   (str "https://github.com/" full-repo)
        repo-link  [:a {:href repo-url} full-repo]
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
                                       (when (> claim-count 1) "s"))])]]
     [:div.open-bounty-item-icon
      [:div.ui.tiny.circular.image
       [:img {:src avatar-url}]]]]))

(defn bounties-filter [name]
  (let [open? (r/atom false)]
    (fn [name]
      [:div.open-bounties-filter-element-container
       {:tab-index 0
        :on-blur   #(reset! open? false)}
       [:div.open-bounties-filter-element
        {:on-click #(swap! open? not)
         :class    (when @open? "open-bounties-filter-element-active")}
        name]
       (when @open?
         [:div.open-bounties-filter-element-tooltip
          "TOOLTIP"])])))

(defn bounties-filters []
  [:div.open-bounties-filter
   [bounties-filter "Value"]
   [bounties-filter "Currency"]
   [bounties-filter "Date"]
   [bounties-filter "Owner"]])

(defn bounties-sort []
  (let [open? (r/atom false)]
    (fn []
      (let [current-sorting (rf/subscribe [::db/bounty-sorting-type])]
        [:div.open-bounties-sort
        {:tab-index 0
         :on-blur   #(reset! open? false)}
        [:div.open-bounties-sort-element
         {:on-click #(swap! open? not)}
         (db/bounty-sorting-types @current-sorting)
         [:div.icon-forward-white-box
          [:img
           {:src "icon-forward-white.svg"}]]]
        (when @open?
          [:div.open-bounties-sort-element-tooltip
           (for [[sorting-type sorting-name] db/bounty-sorting-types]
             [:div.open-bounties-sort-type
              {:on-click #(do
                            (reset! open? false)
                            (rf/dispatch [::handlers/set-bounty-sorting-type sorting-type]))}
              sorting-name])])]))))

(defn bounties-list [open-bounties]
  [:div.ui.container.open-bounties-container
   [:div.open-bounties-header "Bounties"]
   [:div.open-bounties-filter-and-sort
    [bounties-filters]
    [bounties-sort]]
   (if (empty? open-bounties)
     [:div.view-no-data-container
      [:p "No recent activity yet"]]
     (into [:div.ui.items]
           (for [bounty open-bounties]
             [bounty-item bounty])))])


(defn bounties-page []
  (let [open-bounties          (rf/subscribe [:open-bounties])
        open-bounties-loading? (rf/subscribe [:get-in [:open-bounties-loading?]])]
    (fn []
      (if @open-bounties-loading?
        [:div.view-loading-container
         [:div.ui.active.inverted.dimmer
          [:div.ui.text.loader.view-loading-label "Loading"]]]
        [bounties-list @open-bounties]))))
