(ns commiteth.bounties
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [moment-timestamp
                                      issue-url]]))


(defn bounty-item [bounty]
  (println bounty)
  (let [{avatar-url :repo_owner_avatar_url
         owner :repo_owner
         repo-name :repo_name
         issue-title :issue_title
         issue-number :issue_number
         updated :updated
         balance :balance} bounty
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
       issue-link]
      [:div.footer-row
       [:div.balance-badge (str "ETH " balance )]
       [:div.time (moment-timestamp updated)]]]]))

(defn bounties-list [open-bounties]
  [:div.ui.container
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
