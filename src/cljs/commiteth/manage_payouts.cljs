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
   [:a {:href (bnt/issue-url bounty)}
    [:div.cf
     [:div.fl.w-80
      [:span.db.f4.muted-blue.hover-black issue-title]
      #_[:div.mt2
       [:span.f5.gray.pg-book (str owner "/" repo-name " #" issue-number)]]]
     [:div.fl.w-20.tr
      [:span.f6.gray.pg-book
       {:on-click #(do (.preventDefault %) (prn (dissoc bounty :claims)))}
       (common/human-time updated)]]]]])

(defn confirm-button [bounty claim]
  (let [paid?   (bnt/paid? claim)
        merged? (bnt/merged? claim)]
    (when (and merged? (not paid?))
      [:button.f7.ttu.tracked.outline-0.bg-sob-blue.white.pv3.ph4.pg-med.br3.bn.pointer
       (merge {:on-click #(rf/dispatch [:confirm-payout claim])}
              (if (and merged? (not paid?) (:payout_address bounty))
                {}
                {:disabled true})
              (when (and (or (bnt/confirming? bounty)
                             (bnt/bot-confirm-unmined? bounty))
                         merged?)
                {:class "busy loading" :disabled true}))
       (if paid?
         "Signed off"
         "Confirm Payment")])))

(defn confirm-row [bounty claim]
  (let [payout-address-available? (:payout_address bounty)]
    [:div
     (when-not payout-address-available?
       [:div.bg-sob-blue-o-20.pv2.ph3.br3.mb3.f6
        [:p [:span.pg-med (or (:user_name claim) (:user_login claim))
             "’s payment address is pending."] " You will be able to confirm the payment once the address is provided."]])
     [:div.cf
      [:div.dt.fr
       (when-not payout-address-available?
         {:style {:-webkit-filter "grayscale(1)"
                  :pointer-events "none"}})
       [:div.dtc.v-mid.pr3.f6
        [bounty-balance bounty]]
       [:div.dtc.v-mid
        [confirm-button bounty claim]]]]]))

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
       [:img.br-100.w2 {:src avatar-url}]]
      [:div.pl3.flex-auto
       [:div
        [:span.f5.muted-blue
         (or user-name user-login) " "
         [:span.f6.o-60 (when user-name (str "@" user-login "") )]
         (if (bnt/paid? claim)
           (if (= user-login winner-login)
             [:span "Received payout"]
             [:span "No payout"]))]
        [:div.muted-blue "Submitted a claim via "
         [:a {:href (pr-url claim)}
          (str (:repo_owner claim) "/" (:repo_name claim) " PR #" (:pr_number claim))]]]]
      (when render-view-claim-button?
        [:div.dtc.v-mid
         [:div.w-100
          [:a.dib.tc.f7.ttu.tracked.bg-sob-blue.white.pv2.ph3.pg-med.br2.pointer.hover-white
           {:href (pr-url claim)}
           "View Pull Request"]]])]]))

(defn to-confirm-list [bounties]
  (if (empty? bounties)
    [:div.mb3.br3.shadow-6.bg-white.tc.pa5
     [:h3.pg-book "Nothing to confirm"]
     [:p "Here you will see the merged claims awaiting payment confirmation"]]
    (into [:div]
          (for [bounty bounties
                ;; Identifying the winning claim like this is a bit
                ;; imprecise if there have been two PRs for the same
                ;; bounty by the same contributor
                ;; Since the resulting payout is the same we can probably
                ;; ignore this edge case for a first version
                :let [winning-claim (->> (:claims bounty)
                                         (filter #(= (:user_login %)
                                                     (:winner_login bounty)))
                                         util/assert-first)]]
            ^{:key (:issue_id bounty)}
            [:div.mb3.br3.shadow-6.bg-white
             [:div.pa3
              [bounty-card bounty]]
             [:div.pa3
              [claim-card bounty winning-claim]]
             [:div.pa3.bg-sob-tint.br3.br--bottom
              [confirm-row bounty winning-claim]]]))))

(defn to-merge-list [bounties]
  (if (empty? bounties)
    [:div.mb3.br3.shadow-6.bg-white.tc.pa5
     [:h3.pg-book "Nothing to merge"]
     [:p "Here you will see the claims waiting to be merged"]]
    (into [:div]
          (for [bounty bounties
                :let [claims (:claims bounty)]]
            ^{:key (:issue_id bounty)}
            [:div.mb3.shadow-6
             [:div.pa3.bg-white.br3.br--top
              [bounty-card bounty]
              [:div.mt3.f6 [bounty-balance bounty]]]
             [:div.pa3.bg-sob-tint.br3.br--bottom
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

(defn bounty-stats-new [{:keys [paid unpaid]}]
  [:div.br3.bg-white.shadow-6.pa4
   [:span.db.f3.pg-med.mb2 (common/usd-string (:combined-usd-value paid))]
   [:span.gray "Paid for " [:span.muted-blue (:count paid) " solved bounties"]]
   [:div.bb.b--near-white.mv3]
   [:span.db.f3.pg-med.pt1.mb2 (common/usd-string (:combined-usd-value unpaid))]
   [:span.gray "Open for " [:span.muted-blue (:count unpaid) " bounties"]]])

(def state-mapping
  {:opened :open
   :funded :funded
   :claimed :claimed
   :multiple-claims :claimed
   :merged :merged
   :pending-contributor-address :pending-contributor-address
   :pending-maintainer-confirmation :pending-maintainer-confirmation
   :paid :paid})

(defn bounty-title-link [bounty show-date?]
  [:a {:href (common/issue-url (:repo-owner bounty) (:repo-name bounty) (:issue-number bounty))}
   [:div.w-100.overflow-hidden
    [:span.db.f5.pg-med.muted-blue.hover-black (:issue-title bounty)]
    (when show-date?
      [:span.db.mt1.f7.gray.pg-book
       ;;{:on-click #(do (.preventDefault %) (prn (dissoc bounty :claims)))}
       (common/human-time (:updated bounty))])]])

(defn square-card
  "A mostly generic component that renders a square with a section
  pinned to the top and a section pinned to the bottom."
  [top bottom]
  [:div.aspect-ratio-l.aspect-ratio--1x1-l
   [:div.bg-sob-tint.br3.shadow-6.pa3.aspect-ratio--object-l.flex-l.flex-column-l
    [:div.flex-auto top]
    [:div bottom]]])

(defn unclaimed-bounty [bounty]
  [:div.w-third-l.fl-l.pa2
   [square-card
    [bounty-title-link bounty true]
    [:div.f7
     [bounty-balance bounty]]]])

(defn paid-bounty [bounty]
  [:div.w-third-l.fl-l.pa2
   [square-card
    [:div
     [bounty-title-link bounty false]
     [:div.f6.mt1.gray
      "Paid out to " [:span.pg-med.muted-blue "@" (:winner_login bounty)]]]
    [:div.f7
     [bounty-balance bounty]]]])

(defn expandable-bounty-list [bounty-component bounties]
  (let [expanded? (r/atom false)]
    (fn expandable-bounty-list-render [bounty-component bounties]
      [:div
       [:div.cf.nl2.nr2
        (for [bounty (cond->> bounties
                       (not @expanded?) (take 3))]
          ^{:key (:issue_id bounty)}
          [bounty-component bounty])]
       (when (> (count bounties) 3)
         [:div.tr
          [:span.f5.sob-blue.pointer
           {:role "button"
            :on-click #(reset! expanded? (not @expanded?))}
           (if @expanded?
             "Collapse ↑"
             "See all ↓")]])])))

(defn count-pill [n]
  [:span.v-mid.ml3.ph3.pv1.bg-light-gray.br3.f7 n])

(defn salute [name]
  (let [msg-info (rf/subscribe [:dashboard/banner-msg])]
    (fn salute-render [name]
      (when @msg-info
        [:div.relative.pa3.bg-sob-blue-o-20.br3
         [:div.absolute.top-0.right-0.pa3.b.pointer
          {:role "button"
           :on-click #(rf/dispatch [:dashboard/mark-banner-as-seen (:banner-id @msg-info)])}
          "× "]
         [:div
          (case (:banner-id @msg-info)
            "bounty-issuer-salute" [:p [:span.f4.mr2.v-mid "🖖"] [:span.pg-med "We salute you " (:name @msg-info) "!"]
                                    "  Here is where you can manage your bounties. Questions or comments? "
                                    [:a.sob-blue.pg-med {:href "https://chat.status.im"} "Chat with us"]]
            "new-dashboard-info" [:p [:span.pg-med "NEW!"]
                                  "  Here is where you can manage your bounties. Questions or comments? "
                                  [:a.sob-blue.pg-med {:href "https://chat.status.im"} "Chat with us"]])]]))))

(defn manage-payouts-page []
  ;; TODO fix `page` subscription to subscribe to full route info
  ;; do this after @msuess PR with some related routing changes has
  ;; been merged
  (let [page          (rf/subscribe [:page])
        owner-bounties (rf/subscribe [:owner-bounties])
        bounty-stats-data (rf/subscribe [:owner-bounties-stats])
        owner-bounties-loading? (rf/subscribe [:get-in [:owner-bounties-loading?]])]
    (fn []
      (if @owner-bounties-loading?
        [:div.pa5
         [:div.ui.active.inverted.dimmer.bg-none
          [:div.ui.text.loader "Loading"]]]
        (let [bounties  (vals @owner-bounties)
              grouped   (group-by (comp state-mapping :state) bounties)
              unclaimed (into (get grouped :funded)
                              (get grouped :open))]
          [:div.center.mw9.pa2.pa0-l
           [:h1.f3.pg-book.mb3 "Manage bounties"]
            [:div.dn-l.db-ns
             [bounty-stats-new @bounty-stats-data]]
           [salute "Andy"]
           (when (nil? (common/web3))
             [:div.ui.warning.message
              [:i.warning.icon]
              "To sign off claims, please view Status Open Bounty in Status, Mist or Metamask"])
           (let [active-classes "muted-blue bb bw2 b--sob-blue"
                 non-active-classes "silver pointer"]
             [:div.mv4
              [:span.dib.f6.tracked.ttu.pg-med.mr4.pb2
               {:role "button"
                :class (if (= @page :issuer-dashboard/to-confirm) active-classes non-active-classes)
                :on-click #(routes/nav! :issuer-dashboard/to-confirm)}
               "To confirm payment"]
              [:span.dib.f6.tracked.ttu.pg-med.mr4.pb2
               {:role "button"
                :class (if (= @page :issuer-dashboard/to-merge) active-classes non-active-classes)
                :on-click #(routes/nav! :issuer-dashboard/to-merge)}
               "To merge"]])
           [:div.cf
            [:div.fr.w-third.pl4.mb3.dn.db-l
             [bounty-stats-new @bounty-stats-data]]
            [:div.fr-l.w-two-thirds-l
             (case @page
               :issuer-dashboard/to-confirm (to-confirm-list (get grouped :pending-maintainer-confirmation))
               :issuer-dashboard/to-merge (to-merge-list (get grouped :claimed))
               (cond
                 (seq (get grouped :pending-maintainer-confirmation))
                 (routes/nav! :issuer-dashboard/to-confirm)

                 (seq (get grouped :claimed))
                 (routes/nav! :issuer-dashboard/to-merge)

                 :else (routes/nav! :issuer-dashboard/to-confirm)))
             (let [heading :h4.f3.normal.pg-book.sob-muted-blue]
               [:div.mt5
                [:div.mt4
                 [heading "Bounties not claimed yet" (count-pill (count unclaimed))]
                 [expandable-bounty-list
                  unclaimed-bounty
                  (->> unclaimed (sort-by :updated) (reverse))]]

                [:div.mt4
                 [heading "Paid out bounties" (count-pill (count (get grouped :paid)))]
                 [expandable-bounty-list paid-bounty (get grouped :paid)]]

                [:div.mt4
                 [heading "Merged bounties" (count-pill (count (get grouped :merged)))]
                 [:p "I'm not sure why these exist. They have a :payout-address and a :confirm-hash so why are they missing the :payout-hash?"]
                 [expandable-bounty-list paid-bounty (get grouped :merged)]]

                #_[:div.mt4
                   [:h4.f3 "Revoked bounties (" (count (get grouped :paid)) ")"]
                   [expandable-bounty-list unclaimed-bounty (get grouped :paid)]]])]
            ]
           [:div.mb5]])))))
