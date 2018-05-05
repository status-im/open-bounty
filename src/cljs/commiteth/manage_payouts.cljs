(ns commiteth.manage-payouts
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.string :as gstring]
            [commiteth.util :as util]
            [commiteth.routes :as routes]
            [commiteth.model.bounty :as bnt]
            [commiteth.ui.balances :as ui-balances]
            [commiteth.config :as config]
            [commiteth.common :as common :refer [human-time]]))

(defn pr-url [{owner :repo_owner
               pr-number :pr_number
               repo :repo_name}]
  (str "https://github.com/" owner "/" repo "/pull/" pr-number))

(defn etherscan-tx-url [tx-id]
   (str "https://"
          (when (config/on-testnet?) "ropsten.")
          "etherscan.io/tx/" tx-id))

(def primary-button-button :button.f7.ttu.tracked.outline-0.bg-sob-blue.white.pv3.ph4.pg-med.br3.bn.pointer.shadow-7)
(def primary-button-link :a.dib.tc.f7.ttu.tracked.bg-sob-blue.white.pv2.ph3.pg-med.br2.pointer.hover-white.shadow-7)


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
      [:span.pg-med.fw5.db.f4.dark-gray.hover-black
       (gstring/truncate issue-title 110)]
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
      [primary-button-button
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
        [:div
         [ui-balances/token-balances (bnt/crypto-balances bounty) :badge]
         [:div.dib.mr2.pv1
          [ui-balances/usd-value-label (:value-usd bounty)]]]]
       [:div.dtc.v-mid
        [confirm-button bounty claim]]]]]))

(defn view-pr-button [claim]
  [primary-button-link
   {:href (pr-url claim)
    :target "_blank"}
   "View Pull Request"])

(defn claim-card [bounty claim {:keys [render-view-claim-button?] :as opts}]
  (let [{user-name :user_name
         user-login :user_login
         avatar-url :user_avatar_url} claim
        winner-login (:winner_login bounty)]
    [:div.pv2
     [:div.flex
      {:class (when (and (bnt/paid? claim) (not (= user-login winner-login)))
                "o-50")}
      [:div.w3.flex-none.pr3.pl1.nl1
       [:img.br-100.w-100.bg-white {:src avatar-url}]]
      [:div.flex-auto
       [:div
        [:span.f5.dark-gray.pg-med.fw5
         (or user-name user-login) " "
         [:span.f6.o-60 (when user-name (str "@" user-login "") )]
         (if (bnt/paid? claim)
           (if (= user-login winner-login)
             [:span "Received payout"]
             [:span "No payout"]))]
        [:div.f6.gray "Submitted a claim via "
         [:a.gray {:href (pr-url claim)}
          (str (:repo_owner claim) "/" (:repo_name claim) " PR #" (:pr_number claim))]]
        ;; We render the button twice for difference screen sizes, first button is for small screens:
        ;; 1) db + dn-ns: `display: block` + `display: none` for not-small screens
        ;; 2) dn + db-ns: `display: none` + `display: block` for not-small screens
        (when render-view-claim-button?
          [:div.mt2.db.dn-ns
           (view-pr-button claim)])]]
      (when render-view-claim-button?
        [:div.dn.db-ns
         [:div.w-100
          (view-pr-button claim)]])]]))

(defn to-confirm-list [bounties]
  (if (empty? bounties)
    [:div.mb3.br3.shadow-6.bg-white.tc.pa5
     [:h3.pg-book "Nothing to confirm"]
     [:p "Here you will see the merged claims awaiting payment confirmation"]]
    (into [:div]
          ;; FIXME we remove all bounties that Andy 'won' as this basically
          ;; has been our method for revocations. This needs to be cleaned up ASAP.
          ;; https://github.com/status-im/open-bounty/issues/284
          (for [bounty (filter #(not= "andytudhope" (:winner_login %)) bounties)
                ;; Identifying the winning claim like this is a bit
                ;; imprecise if there have been two PRs for the same
                ;; bounty by the same contributor
                ;; Since the resulting payout is the same we can probably
                ;; ignore this edge case for a first version
                :let [winning-claim (->> (:claims bounty)
                                         (filter #(and (bnt/merged? %)
                                                       (= (:user_login %)
                                                          (:winner_login bounty))))
                                         util/assert-first)]]
            ^{:key (:issue-id bounty)}
            [:div.mb3.br3.shadow-6.bg-white
             [:div.ph4.pt4
              [bounty-card bounty]]
             [:div.ph4.pv3
              [claim-card bounty winning-claim]]
             [:div.ph4.pv3.bg-sob-tint.br3.br--bottom
              [confirm-row bounty winning-claim]]]))))

(defn to-merge-list [bounties]
  (if (empty? bounties)
    [:div.mb3.br3.shadow-6.bg-white.tc.pa5
     [:h3.pg-book "Nothing to merge"]
     [:p "Here you will see the claims waiting to be merged"]]
    (into [:div]
          (for [bounty bounties
                :let [claims (filter bnt/open? (:claims bounty))]]
            ^{:key (:issue-id bounty)}
            [:div.mb3.shadow-6
             [:div.pa4.nb2.bg-white.br3.br--top
              [bounty-card bounty]]
             [:div.ph4.pv3.bg-sob-tint.br3.br--bottom
              [:span.f6.gray (if (second claims)
                       (str "Current Claims (" (count claims) ")")
                       "Current Claim")]
              (for [[idx claim] (zipmap (range) claims)]
                ^{:key (:pr_id claim)}
                [:div
                 {:class (when (> idx 0) "bt b--light-gray pt2")}
                 [claim-card bounty claim {:render-view-claim-button? true}]])]]))))

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
  (let [usd-stat (fn usd-stat [usd-amount]
                   [:div.dt
                    [:span.dtc.v-mid.pr1 "$"]
                    [:span.dtc.pg-med.fw5.mb2.dark-gray
                     {:class (if (< 100000000 usd-amount) "f3" "f2")}
                     (.toLocaleString usd-amount)]])]
    [:div.br3.bg-white.shadow-6.pa4.dark-gray
     [:span.db.mb3.f6 "Open for " [:span.dark-gray (:count unpaid) " bounties"]]
     (usd-stat (:combined-usd-value unpaid))
     [:div.f6.mt3
      [ui-balances/token-balances (:crypto unpaid) :label]]
     [:div.bb.b--near-white.mv3]
     [:span.db.mb3.f6 "Paid for " (:count paid) " solved bounties"]
     (usd-stat (:combined-usd-value paid))
     [:div.f6.mt3
      [ui-balances/token-balances (:crypto paid) :label]]]))

(def state-mapping
  {:opened :open
   :funded :funded
   :claimed :claimed
   :multiple-claims :claimed
   :merged :merged
   :pending-contributor-address :pending-contributor-address
   :pending-maintainer-confirmation :pending-maintainer-confirmation
   :paid :paid})

(defn bounty-title-link [bounty {:keys [show-date? max-length]}]
  [:a.lh-title {:href (common/issue-url (:repo-owner bounty) (:repo-name bounty) (:issue-number bounty))}
   [:div.w-100.overflow-hidden
    [:span.db.f5.pg-med.dark-gray.hover-black
     (cond-> (:issue-title bounty)
       max-length (gstring/truncate max-length))]
    (when show-date?
      [:span.db.mt1.f6.gray.pg-book
       (common/human-time (:updated bounty))])]])

(defn square-card
  "A mostly generic component that renders a square with a section
  pinned to the top and a section pinned to the bottom."
  [top bottom]
  [:div.aspect-ratio-l.aspect-ratio--1x1-l
   [:div.bg-sob-tint.br3.shadow-6.pa3.aspect-ratio--object-l.flex-l.flex-column-l
    [:div.flex-auto top]
    [:div bottom]]])

(defn small-card-balances [bounty]
  [:div.f6.fl.w-80
   [ui-balances/token-balances (bnt/crypto-balances bounty) :label]
   [:div
    [ui-balances/usd-value-label (:value-usd bounty)]]])

(defn revoke-button [bounty]
  (let [{:keys [issue-id value-usd]} bounty]
   (when (pos? value-usd)
     [:div.fl.w-20
      [:button.ui.button
       {:on-click #(rf/dispatch [:revoke-bounty {:issue-id issue-id}])}
       "Revoke"]])))

(defn unclaimed-bounty [bounty]
  [:div.w-third-l.fl-l.pa2
   [square-card
    [bounty-title-link bounty {:show-date? true :max-length 60}]
    [:div [small-card-balances bounty] [revoke-button bounty]]]])

(defn paid-bounty [bounty]
  [:div.w-third-l.fl-l.pa2
   [square-card
    [:div
     [bounty-title-link bounty {:show-date? false :max-length 60}]
     [:div.f6.mt1.gray
      "Paid out to " [:span.pg-med.fw5 "@" (:winner_login bounty)]]]
    [small-card-balances bounty]]])

(defn expandable-bounty-list [bounty-component bounties]
  (let [expanded? (r/atom false)]
    (fn expandable-bounty-list-render [bounty-component bounties]
      [:div
       [:div.cf.nl2.nr2
        (for [bounty (cond->> bounties
                       (not @expanded?) (take 3))]
          ^{:key (:issue-id bounty)}
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
  [:span.v-top.ml3.ph3.pv1.bg-black-05.gray.br3.f7 n])

(defn salute [name]
  (let [msg-info (rf/subscribe [:dashboard/banner-msg])]
    (fn salute-render [name]
      (when @msg-info
        [:div.relative.pa3.pr4.bg-sob-blue-o-20.br3.nt1
         [:div.f3.dark-gray.absolute.top-0.right-0.pa3.b.pointer
          {:role "button"
           :on-click #(rf/dispatch [:dashboard/mark-banner-as-seen (:banner-id @msg-info)])}
          "× "]
         [:div
          (case (:banner-id @msg-info)
            "bounty-issuer-salute" [:p [:span.f4.mr2.v-mid "🖖"] [:span.pg-med "We salute you " (:name @msg-info) "!"]
                                    "  Here is where you can manage your bounties. Questions or comments? "
                                    [:a.sob-blue.pg-med {:href "https://chat.status.im" :target "_blank"} "Chat with us"]]
            "new-dashboard-info" [:p [:span.pg-med "NEW!"]
                                  "  Here is where you can manage your bounties. Questions or comments? "
                                  [:a.sob-blue.pg-med {:href "https://chat.status.im"} "Chat with us"]])]]))))

(defn manage-bounties-title []
  [:h1.f3.pg-med.fw5.dark-gray.mb3 "Manage bounties"])

(defn manage-bounties-nav [active-route-id]
  (let [active-classes "dark-gray bb bw2 b--sob-blue"
        non-active-classes "silver pointer"
        tab :span.dib.f6.tracked.ttu.pg-med.mr3.ml2.pb2]
    [:div.mv4.nl2
     [tab
      {:role "button"
       :class (if (= active-route-id :dashboard/to-confirm) active-classes non-active-classes)
       :on-click #(routes/nav! :dashboard/to-confirm)}
      "To confirm payment"]
     [tab
      {:role "button"
       :class (if (= active-route-id :dashboard/to-merge) active-classes non-active-classes)
       :on-click #(routes/nav! :dashboard/to-merge)}
      "To merge"]]))

(defn manage-payouts-loading []
  [:div.center.mw9.pa2.pa0-l
   [manage-bounties-title]
   [manage-bounties-nav :dashboard/to-confirm]
   [:div.w-two-thirds-l.mb6
    ;; This semantic UI loading spinner thing makes so many assumptions
    ;; severly limiting where and how it can be used.
    ;; TODO replace with React spinner library, CSS spinner or something else
    [:div.ui.segment
     [:div.ui.active.inverted.dimmer
      [:div.ui.text.loader "Loading"]]]]])

(defn manage-payouts-page []
  (let [route (rf/subscribe [:route])
        user (rf/subscribe [:user])
        owner-bounties (rf/subscribe [:owner-bounties])
        bounty-stats-data (rf/subscribe [:owner-bounties-stats])
        owner-bounties-loading? (rf/subscribe [:get-in [:owner-bounties-loading?]])]
    (fn manage-payouts-page-render []
      (cond
        (nil? @user)
        [:div.bg-white.br3.shadow-6.pa4.tc
         [:h3 "Please log in to view this page."]]

        (and
         (empty? @owner-bounties)
         (or (nil? @owner-bounties-loading?) @owner-bounties-loading?))
        [manage-payouts-loading]

        :else
        (let [route-id  (:route-id @route)
              bounties  (vals @owner-bounties)
              grouped   (group-by (comp state-mapping :state) bounties)
              unclaimed (into (get grouped :funded)
                              (get grouped :open))
              to-confirm (into (get grouped :pending-maintainer-confirmation)
                               (get grouped :pending-contributor-address))]
          [:div.center.mw9.pa2.pa0-l
           [manage-bounties-title]
           [salute "Andy"]
           [:div.dn-l.db-ns.mt4
            [bounty-stats-new @bounty-stats-data]]
           (when (nil? (common/web3))
             [:div.ui.warning.message
              [:i.warning.icon]
              "To sign off claims, please view Status Open Bounty in Status, Mist or Metamask"])
           [manage-bounties-nav route-id]
           [:div.cf
            [:div.fr.w-third.pl4.mb3.dn.db-l
             [bounty-stats-new @bounty-stats-data]]
            [:div.fr-l.w-two-thirds-l
             (case route-id
               :dashboard/to-confirm (->> to-confirm
                                          (sort-by :updated >)
                                          (to-confirm-list))
               :dashboard/to-merge (->> (get grouped :claimed)
                                        (sort-by :updated >)
                                        (to-merge-list))
               (cond
                 (seq to-confirm)
                 (routes/nav! :dashboard/to-confirm)

                 (seq (get grouped :claimed))
                 (routes/nav! :dashboard/to-merge)

                 :else (routes/nav! :dashboard/to-confirm)))
             (let [heading :h4.f4.normal.pg-book.dark-gray]
               [:div.mt5
                [:div.mt4
                 [heading "Unclaimed bounties" (count-pill (count unclaimed))]
                 [expandable-bounty-list
                  unclaimed-bounty
                  (sort-by :updated > unclaimed)]]

                [:div.mt4
                 [heading "Paid out bounties" (count-pill (count (get grouped :paid)))]
                 [expandable-bounty-list
                  paid-bounty
                  (sort-by :updated > (get grouped :paid))]]

                #_[:div.mt4
                 [heading "Merged bounties" (count-pill (count (get grouped :merged)))]
                 [:p "I'm not sure why these exist. They have a :payout-address and a :confirm-hash so why are they missing the :payout-hash?"]
                 [expandable-bounty-list paid-bounty (get grouped :merged)]]

                #_[:div.mt4
                   [:h4.f3 "Revoked bounties (" (count (get grouped :paid)) ")"]
                   [expandable-bounty-list unclaimed-bounty (get grouped :paid)]]])]
            ]
           [:div.mb5]])))))
