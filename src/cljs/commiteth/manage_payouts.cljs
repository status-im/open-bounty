(ns commiteth.manage-payouts
  (:require [re-frame.core :as rf]
            [commiteth.routes :as routes]
            [commiteth.common :as common :refer [human-time]]))



(defn pr-url [{owner :repo_owner
               pr-number :pr_number
               repo :repo_name}]
  (str "https://github.com/" owner "/" repo "/pull/" pr-number))

(defn balance-badge
  [tla balance]
  (let [color (fn balance-badge-color [tla]
                (get {"ETH" "#57a7ed"} tla "#4360df"))
        tla (if (keyword? tla)
              (subs (str tla) 1)
              tla)]
    [:div.ph2.pv1.relative
     {:style {:color (color tla)}}
     [:div.absolute.top-0.left-0.right-0.bottom-0.o-10.br2
      {:style {:background-color (color tla)}}]
     [:span.pg-med (str tla " " balance)]]))

(defn bounty-card [{owner        :repo-owner
                    repo-name    :repo-name
                    issue-title  :issue-title
                    issue-number :issue-number
                    updated      :updated
                    tokens       :tokens
                    balance-eth  :balance-eth
                    value-usd    :value-usd
                    :as bounty}]

  [:div
   [:a {:href (common/issue-url owner repo-name issue-number)}
    [:span.db.f4.muted-blue.hover-black issue-title]
    [:div.mt2
     [:span.f5.gray.pg-book (str owner "/" repo-name " #" issue-number) " — " (common/human-time updated)]]
    [:div.cf.mt2
     [:div.fl.mr2
      [balance-badge "ETH" balance-eth]]
     (for [[tla balance] tokens]
       ^{:key tla}
       [:div.fl.mr2
        [balance-badge tla balance]])
     [:div.fl.mr2.pv1
      [:span.usd-value-label "Value "]
      [:span.usd-balance-label (str "$" value-usd)]]]

    #_[:code (pr-str bounty)]


     #_(when (> claim-count 0)
       [:span.open-claims-label (str claim-count " open claim"
                                     (when (> claim-count 1) "s"))])]])

(defn claim-card [bounty claim]
  #_(prn claim)
  (let [{pr-state :pr_state
         user-name :user_name
         user-login :user_login
         avatar-url :user_avatar_url
         issue-id :issue_id
         issue-title :issue_title} claim
        merged? (= 1 (:pr_state claim))
        paid? (not-empty (:payout_hash claim))
        winner-login (:winner_login bounty)
        bot-confirm-unmined? (empty? (:confirm_hash bounty))
        confirming? (:confirming? bounty)
        updated (:updated bounty)]
    [:div.pa2
     [:div.dt
      {:class (when (and paid? (not (= user-login winner-login)))
                "o-50")}
      [:div.dtc.v-top
       [:img.br-100.w3 {:src avatar-url}]]
      [:div.dtc.v-top.pl3
       [:div
        [:span.f4.muted-blue (or user-name user-login) " · "
         (if paid?
           (if (= user-login winner-login)
             [:span "Received payout"]
             [:span "No payout"])
           (if merged? "Merged" "Open"))]
        ;; [:span.f5 (human-time updated)]
        [:div "Submitted a claim via "
         [:a {:href (pr-url claim)}
          (str (:repo_owner claim) "/" (:repo_name claim) " PR #" (:pr_number claim))]]
        (when (and merged? (not paid?))
          [:button.mt2.f5.outline-0.bg-sob-blue.white.pv2.ph3.pg-med.br2.bn
           (merge (if (and merged? (not paid?))
                    {}
                    {:disabled true})
                  {:on-click #(rf/dispatch [:confirm-payout claim])}
                  (when (and (or confirming? bot-confirm-unmined?)
                             merged?)
                    {:class "busy loading" :disabled true}))
           (if paid?
             "Signed off"
             "Confirm")])]]]]))


(defn claim-list [bounties]
  ;; TODO: exclude bounties with no claims
  (if (empty? bounties)
    [:div.ui.text "No items"]
    (into [:div]
          (for [bounty bounties]
            ^{:key (:issue_id bounty)}
            [:div.mb2.br3
             [:div.pa3.bg-white.bb.b--light-gray.br3.br--top
              [bounty-card bounty]]
             [:div.pa3.bg-white.br3.br--bottom
              (if (seq (:claims bounty))
                (for [claim  (:claims bounty)]
                  ^{:key (:pr_id claim)}
                  [claim-card bounty claim])
                [:div.f4.muted-blue "No claims yet."])]]))))

(defn bounty-stats [{:keys [paid unpaid]}]
  [:div.cf
   [:div.fl-ns.w-50-ns.tc.pv4
    [:div.ttu.tracked "Open"]
    [:div.f2.pa2 (common/usd-string (:combined-usd-value unpaid))]
    [:div (:count unpaid) " bounties"]]

   [:div.fl-ns.w-50-ns.tc.pv4
    [:div.ttu.tracked "Paid"]
    [:div.f2.pa2 (common/usd-string (:combined-usd-value paid))]
    [:div (:count paid) " bounties"]]])

(def state-mapping
  {:opened :open
   :funded :funded
   :claimed :claimed
   :multiple-claims :claimed
   :merged :merged
   :pending-contributor-address :pending-contributor-address
   :pending-maintainer-confirmation :pending-maintainer-confirmation
   :paid :paid})

(defn manage-payouts-page []
  (let [owner-bounties (rf/subscribe [:owner-bounties])
        bounty-stats-data (rf/subscribe [:owner-bounties-stats])
        owner-bounties-loading? (rf/subscribe [:get-in [:owner-bounties-loading?]])]
    (fn []
      (if @owner-bounties-loading?
        [:container
         [:div.ui.active.inverted.dimmer
          [:div.ui.text.loader "Loading"]]]
        (let [bounties (vals @owner-bounties)
              grouped  (group-by (comp state-mapping :state) bounties)]
          [:div.center.mw7
           (when (nil? (common/web3))
             [:div.ui.warning.message
              [:i.warning.icon]
              "To sign off claims, please view Status Open Bounty in Status, Mist or Metamask"])
           [bounty-stats @bounty-stats-data]
           [:div.cf
            [:button.pa2.tl.bn.bg-white.muted-blue
             {:on-click #(routes/nav! :issuer-dashboard/paid)}
             [:span.f4 "Paid"] [:br]
             (count (get grouped :paid)) " bounties"]]
           (for [[k v] grouped]
             [:div
              {:key (name k)}
              [:h3 (name k) " — " (count v)]
              [claim-list (take 10 v)]])
           #_[:h3 "New claims"]
           #_[claim-list (filter (complement :paid?) bounties)]
           #_[:h3 "Old claims"]
           #_[claim-list (filter :paid? bounties)]])))))
