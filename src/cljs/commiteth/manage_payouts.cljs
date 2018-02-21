(ns commiteth.manage-payouts
  (:require [re-frame.core :as rf]
            [commiteth.common :as common :refer [human-time]]))



(defn pr-url [{owner :repo_owner
               pr-number :pr_number
               repo :repo_name}]
  (str "https://github.com/" owner "/" repo "/pull/" pr-number))

(defn claim-card [bounty claim]
  (let [{pr-state :pr_state
         user-name :user_name
         avatar-url :user_avatar_url
         issue-id :issue_id
         issue-title :issue_title} claim
        merged? (= 1 (:pr_state claim))
        paid? (not-empty (:payout_hash claim))
        winner-login (:winner_login bounty)
        bot-confirm-unmined? (empty? (:confirm_hash bounty))
        confirming? (:confirming? bounty)
        updated (:updated bounty)]
    [:div.activity-item
     [:div.ui.grid.container
      [:div.left-column
       [:div.ui.circular.image
        [:img {:src avatar-url}]]]
      [:div.content
       [:div.header user-name]
       [:div.description "Submitted a claim for " [:a {:href (pr-url claim)}
                                                   issue-title]]
       [:div.description (if paid?
                           (str "(paid to " winner-login ")")
                           (str "(" (if merged? "merged" "open") ")"))]
       [:div.time (human-time updated)]
       [:button.ui.button
        (merge (if (and merged? (not paid?))
                 {}
                 {:disabled true})
               {:on-click #(rf/dispatch [:confirm-payout claim])}
               (when (and (or confirming? bot-confirm-unmined?)
                          merged?)
                 {:class "busy loading" :disabled true}))
        (if paid?
          "Signed off"
          "Confirm")]]]]))


(defn claim-list [bounties]
  ;; TODO: exclude bounties with no claims
  (if (empty? bounties)
    [:div.ui.text "No items"]
    (into [:div.activity-item-container]
          (for [bounty bounties
                claim (filter #(not (= 2 (:pr_state %))) ;; exclude closed
                       (:claims bounty))]
            [claim-card bounty claim]))))

(defn bounty-stats [{:keys [paid unpaid]}]
  [:div.cf.pv4
   [:div.fl.w-50.tc
    [:div.ttu.tracked "Open"]
    [:div.f2.pa2 (common/usd-string (:combined-usd-value unpaid))]
    [:div (:count unpaid) " bounties"]]

   [:div.fl.w-50.tc
    [:div.ttu.tracked "Paid"]
    [:div.f2.pa2 (common/usd-string (:combined-usd-value paid))]
    [:div (:count paid) " bounties"]]])

(defn manage-payouts-page []
  (let [owner-bounties (rf/subscribe [:owner-bounties])
        bounty-stats-data (rf/subscribe [:owner-bounties-stats])
        owner-bounties-loading? (rf/subscribe [:get-in [:owner-bounties-loading?]])]
    (fn []
      (if @owner-bounties-loading?
        [:container
         [:div.ui.active.inverted.dimmer
          [:div.ui.text.loader "Loading"]]]
        (let [web3 (.-web3 js/window)
              bounties (vals @owner-bounties)]
          [:div.ui.container
           (when (nil? web3)
             [:div.ui.warning.message
              [:i.warning.icon]
              "To sign off claims, please view Status Open Bounty in Status, Mist or Metamask"])
           [bounty-stats @bounty-stats-data]
           [:h3 "New claims"]
           [claim-list (filter (complement :paid?) bounties)]
           [:h3 "Old claims"]
           [claim-list (filter :paid? bounties)]])))))
