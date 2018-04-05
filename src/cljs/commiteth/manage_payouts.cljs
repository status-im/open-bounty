(ns commiteth.manage-payouts
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [commiteth.util :as util]
            [commiteth.routes :as routes]
            [commiteth.model.bounty :as bnt]
            [commiteth.common :as common :refer [human-time]]))

(defn pr-url [{owner :repo_owner
               pr-number :pr_number
               repo :repo_name}]
  (str "https://github.com/" owner "/" repo "/pull/" pr-number))

(defn balance-badge
  [tla balance]
  {:pre [(keyword? tla)]}
  (let [color (fn balance-badge-color [tla]
                (get {"ETH" "#57a7ed"} tla "#4360df"))
        tla   (name tla)]
    [:div.dib.ph2.pv1.relative
     {:style {:color (color tla)}}
     [:div.absolute.top-0.left-0.right-0.bottom-0.o-10.br2
      {:style {:background-color (color tla)}}]
     [:span.pg-med (str tla " " balance)]]))

(defn usd-value-label [value-usd]
  [:span
   [:span.usd-value-label "Value "]
   [:span.usd-balance-label (str "$" value-usd)]])

(defn token-balances [crypto-balances]
  [:span ; TODO consider non DOM el react wrapping
   (for [[tla balance] crypto-balances]
     ^{:key tla}
     [:div.dib.mr2
      [balance-badge tla balance]])])

(defn bounty-balance [{:keys [value-usd] :as bounty}]
  [:div
   [token-balances (bnt/crypto-balances bounty)]
   [:div.dib.mr2.pv1
    [usd-value-label value-usd]]])

(defn bounty-card [{owner        :repo-owner
                    repo-name    :repo-name
                    issue-title  :issue-title
                    issue-number :issue-number
                    updated      :updated
                    tokens       :tokens
                    balance-eth  :balance-eth
                    value-usd    :value-usd
                    :as bounty}
                   {:keys [style] :as opts}]
  [:div
   [:a {:href (common/issue-url owner repo-name issue-number)}
    [:div.cf
     [:div.fl.w-80
      [:span.db.f4.muted-blue.hover-black issue-title]
      [:div.mt2
       [:span.f5.gray.pg-book (str owner "/" repo-name " #" issue-number)]]]
     [:div.fl.w-20.tr
      [:span.f5.gray.pg-book
       {:on-click #(do (.preventDefault %) (prn (dissoc bounty :claims)))}
       (common/human-time updated)]]]]])

(defn confirm-button [bounty claim]
  (let [paid?   (bnt/paid? claim)
        merged? (bnt/merged? claim)]
    (when (and merged? (not paid?))
      [:button.f5.outline-0.bg-sob-blue.white.pv2.ph3.pg-med.br2.bn.pointer
       (merge (if (and merged? (not paid?))
                {}
                {:disabled true})
              {:on-click #(rf/dispatch [:confirm-payout claim])}
              (when (and (or (bnt/confirming? bounty)
                             (bnt/bot-confirm-unmined? bounty))
                         merged?)
                {:class "busy loading" :disabled true}))
       (if paid?
         "Signed off"
         "Confirm")])))

(defn confirm-row [bounty claim]
  [:div.cf
   [:div.dt.fr
    [:div.dtc.v-mid.pr3
     [bounty-balance bounty]]
    [:div.dtc.v-mid
     [confirm-button bounty claim]]]])

(defn claim-card [bounty claim {:keys [render-view-claim-button?] :as opts}]
  (let [{user-name :user_name
         user-login :user_login
         avatar-url :user_avatar_url} claim
        winner-login (:winner_login bounty)]
    [:div.pa2
     [:div.flex.items-center
      {:class (when (and (bnt/paid? claim) (not (= user-login winner-login)))
                "o-50")}
      [:div
       [:img.br-100.w3 {:src avatar-url}]]
      [:div.pl3.flex-auto
       [:div
        [:span.f4.muted-blue
         (or user-name user-login) " "
         [:span.f5.o-60 (when user-name (str "@" user-login "") )]
         (if (bnt/paid? claim)
           (if (= user-login winner-login)
             [:span "Received payout"]
             [:span "No payout"]))]
        [:div "Submitted a claim via "
         [:a {:href (pr-url claim)}
          (str (:repo_owner claim) "/" (:repo_name claim) " PR #" (:pr_number claim))]]]]
      (when render-view-claim-button?
        [:div.dtc.v-mid
         [:div.w-100
          [:a.f5.outline-0.bg-sob-blue.white.pv2.ph3.pg-med.br2.bn.pointer.hover-white
           {:href (pr-url claim)}
           "View Claim"]]])]]))

#_(defn claim-list [bounties]
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

(defn to-confirm-list [bounties]
  (if (empty? bounties)
    [:div.ui.text "No items"]
    (into [:div]
          (for [bounty bounties
                ;; Identifying the winning claim like this is a bit
                ;; imprecise if there have been two PRs for the same
                ;; bounty by the same contributor
                ;; Since the resulting payout is the same we can probably
                ;; ignore this edge case for a first version
                :let [winning-claim (->> (:claims bounty)
                                         (filter #(do (prn bounty)
                                                      (prn %)
                                                      (= (:user_login %)
                                                         (:winner_login bounty))))
                                         util/assert-first)]]
            ^{:key (:issue_id bounty)}
            [:div.mb2
             [:div.pa3.bg-white.bb.b--light-gray
              [bounty-card bounty]]
             [:div.pa3.bg-white
              [claim-card bounty winning-claim]]
             [:div.pa3.bg-near-white
              [confirm-row bounty winning-claim]]]))))

(defn to-merge-list [bounties]
  (if (empty? bounties)
    [:div.ui.text "No items"]
    (into [:div]
          (for [bounty bounties
                :let [claims (:claims bounty)]] ; TODO identify winning claim
            ^{:key (:issue_id bounty)}
            [:div.mb2
             [:div.pa3.bg-white.bb.b--light-gray
              [bounty-card bounty]
              [:div.mt3 [bounty-balance bounty]]]
             [:div.pa3.bg-white
              (for [claim (:claims bounty)]
                ^{:key (:pr_id claim)}
                [claim-card bounty claim {:render-view-claim-button? true}])]]))))

(defn bounty-stats [{:keys [paid unpaid]}]
  [:div.cf
   [:div.fl-ns.w-33-ns.tc.pv4
    #_[:div.ttu.tracked "Paid"]
    [:div.f3.pa2 (common/usd-string (:combined-usd-value paid))]
    [:div.ph4 "Invested so far"]]

   [:div.fl-ns.w-33-ns.tc.pv4
    [:div.f3.pa2 (:count paid)]
    [:div.ph4 "Bounties solved by contributors"]]

   [:div.fl-ns.w-33-ns.tc.pv4
    [:div.f3.pa2 (:count unpaid)]
    [:div.ph4 "Open bounties in total"]]])

(def state-mapping
  {:opened :open
   :funded :funded
   :claimed :claimed
   :multiple-claims :claimed
   :merged :merged
   :pending-contributor-address :pending-contributor-address
   :pending-maintainer-confirmation :pending-maintainer-confirmation
   :paid :paid})

(defn bounty-title-link [bounty]
  [:a {:href (common/issue-url (:repo-owner bounty) (:repo-name bounty) (:issue-number bounty))}
   [:div.w-100.overflow-hidden
    [:span.db.f5.pg-med.muted-blue.hover-black (:issue-title bounty)]
    [:span.f6.gray.pg-book
     {:on-click #(do (.preventDefault %) (prn (dissoc bounty :claims)))}
     (common/human-time (:updated bounty))]]])

(defn unclaimed-bounty [bounty]
  [:div.w-third-ns.fl.pa2
   [:div.bg-white.br2.br--top.pa2.h4
    [bounty-title-link bounty]]
   [:div.bg-white.pa2.f7.br2.br--bottom
    [bounty-balance bounty]]])

(defn paid-bounty [bounty]
  [:div.w-third-ns.fl.pa2
   [:div.bg-white.br2.br--top.pa2.h4
    [bounty-title-link bounty]]
   [:div.bg-white.ph2.f6
    "Paid out to @" (:winner_login bounty)]
   [:div.bg-white.pa2.f7.br2.br--bottom
    [bounty-balance bounty]]])

(defn expandable-bounty-list [bounty-component bounties]
  (let [expanded? (r/atom false)]
    (fn expandable-bounty-list-render [bounty-component bounties]
      [:div
       [:div.cf.nl2.nr2
        (for [bounty (cond->> bounties
                       (not @expanded?) (take 3))]
          [bounty-component bounty])]
       [:div.tr
        [:span.f5.sob-blue.pointer
         {:role "button"
          :on-click #(reset! expanded? (not @expanded?))}
         (if @expanded?
           "Collapse ↑"
           "See all ↓")]]])))

(defn manage-payouts-page []
  (let [page          (rf/subscribe [:page]) ; TODO fix this to subscribe to route subscription
        owner-bounties (rf/subscribe [:owner-bounties])
        bounty-stats-data (rf/subscribe [:owner-bounties-stats])
        owner-bounties-loading? (rf/subscribe [:get-in [:owner-bounties-loading?]])]
    (fn []
      (if @owner-bounties-loading?
        [:div.pa5
         [:div.ui.active.inverted.dimmer.bg-none
          [:div.ui.text.loader "Loading"]]]
        (let [bounties (vals @owner-bounties)
              grouped  (group-by (comp state-mapping :state) bounties)]
          [:div.center.mw7
           (when (nil? (common/web3))
             [:div.ui.warning.message
              [:i.warning.icon]
              "To sign off claims, please view Status Open Bounty in Status, Mist or Metamask"])
           [bounty-stats @bounty-stats-data]
           [:div.cf.ba.b--white.mb2
            [:div.f4.fl.w-50-ns.pa4.tc
             {:role "button"
              :class (if (= @page :issuer-dashboard/to-confirm) "bg-white" "bg-near-white pointer")
              :on-click #(routes/nav! :issuer-dashboard/to-confirm)}
             "To confirm payment (" (count (get grouped :pending-maintainer-confirmation)) ")"]
            [:div.f4.fl.w-50-ns.pa4.tc.pointer.bl.b--white
             {:role "button"
              :class (if (= @page :issuer-dashboard/to-merge) "bg-white" "bg-near-white pointer")
              :on-click #(routes/nav! :issuer-dashboard/to-merge)}
             "To merge (" (count (get grouped :claimed)) ")"]]
           [:div
            (case @page
              :issuer-dashboard/to-confirm (to-confirm-list (get grouped :pending-maintainer-confirmation))
              :issuer-dashboard/to-merge (to-merge-list (get grouped :claimed))
              (cond
                (seq (get grouped :pending-maintainer-confirmation))
                (routes/nav! :issuer-dashboard/to-confirm)

                (seq (get grouped :claimed))
                (routes/nav! :issuer-dashboard/to-merge)

                :else (routes/nav! :issuer-dashboard/to-confirm)))]
           [:div.mt4
            [:h4.f3.sob-muted-blue "Bounties not claimed yet (" (count (get grouped :funded)) ")"]
            [expandable-bounty-list
             unclaimed-bounty
             (reverse (sort-by :updated (get grouped :funded)))]]

           [:div.mt4
            [:h4.f3.sob-muted-blue "Paid out bounties (" (count (get grouped :paid)) ")"]
            [expandable-bounty-list paid-bounty (get grouped :paid)]]


           #_[:div.mt4
            [:h4.f3 "Revoked bounties (" (count (get grouped :paid)) ")"]
            [expandable-bounty-list unclaimed-bounty (get grouped :paid)]]

           [:div.mb5]
           #_[:div.cf
            [:button.pa2.tl.bn.bg-white.muted-blue
             {:on-click #(routes/nav! :issuer-dashboard/paid)}
             [:span.f4 "Paid"] [:br]
             (count (get grouped :paid)) " bounties"]]
           #_(for [[k v] grouped]
             [:div
              {:key (name k)}
              [:h3 (name k) " — " (count v)]
              [claim-list (take 10 v)]])
           #_[:h3 "New claims"]
           #_[claim-list (filter (complement :paid?) bounties)]
           #_[:h3 "Old claims"]
           #_[claim-list (filter :paid? bounties)]])))))
