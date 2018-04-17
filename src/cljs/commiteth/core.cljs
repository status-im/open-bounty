(ns commiteth.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [commiteth.ajax :refer [load-interceptors!]]
            [commiteth.routes]
            [commiteth.handlers]
            [commiteth.subscriptions]
            [commiteth.activity :refer [activity-page]]
            [commiteth.bounties :refer [bounties-page]]
            [commiteth.repos :refer [repos-page]]
            [commiteth.manage-payouts :refer [manage-payouts-page]]
            [commiteth.update-address :refer [update-address-page]]
            [commiteth.usage-metrics :refer [usage-metrics-page]]
            [commiteth.common :refer [input]]
            [commiteth.config :as config]
            [commiteth.svg :as svg]
            [clojure.set :refer [rename-keys]]
            [re-frisk.core :refer [enable-re-frisk!]])
  (:import goog.History))

(defonce version js/commitethVersion)

(defn flash-message-pane []
  (let [flash-message (rf/subscribe [:flash-message])]
    (fn []
      (when-let [[type message] @flash-message]
        (do
          (println "flash-message-pane" type message)
          [:div.ui.active.modal
           [:div.flash-message {:class (name type)}
            [:i.close.icon {:on-click #(rf/dispatch [:clear-flash-message])}]
            [:p message]]])))))

(defn user-dropdown [user items mobile?]
  (let [menu (if @(rf/subscribe [:user-dropdown-open?])
               [:div.ui.menu.transition.visible {:tab-index -1}]
               [:div.ui.menu.transition.hidden])
        avatar-url (:avatar_url user)]
    [:div.ui.inline.dropdown
     {:on-click #(rf/dispatch [:user-dropdown-open])}
     [:div.header-dropdown-container
      [:div.item.header-avatar
       [:img.ui.mini.circular.image.user-dropdown-image {:src avatar-url}]]
      (when-not mobile?
        [:div.header-username (:login user)])
      [:div.item
       [svg/dropdown-icon]]
      (into menu
            (for [[target caption props] items]
              ^{:key target} [:div.item
                              [:a
                               (merge props
                                      (if (keyword? target)
                                        {:on-click #(commiteth.routes/nav! target)}
                                        {:href target}))
                               caption]]))]]))


(defn user-component
  [user mobile?]
  (let [user (rf/subscribe [:user])]
    (fn []
      (if @user
        [:div.ui.container.user-component
         [user-dropdown
          @user
          [[:settings "My Payment Details" {}]
           ["/logout" "Sign Out" {:class "logout-link"}]]
          mobile?]]
        [:a.ui.button.small.login-button {:href js/authorizeUrl} (str "LOG IN"
                                                                      (when-not mobile? " \u2192"))]))))

(defn tabs [mobile?]
  (let [user (rf/subscribe [:user])
        owner-bounties (rf/subscribe [:owner-bounties])
        route (rf/subscribe [:route])]
    (fn tabs-render []
      (let [route-id (:route-id @route)
            tabs [[:bounties (str (when-not @user "Open ") "Bounties")]
                  [:activity "Activity"]
                  (when (seq @owner-bounties)
                    [:dashboard "Dashboard"])
                  (when (:status-team-member? @user)
                    [:usage-metrics "Usage metrics"])]]
        (into [:div.ui.attached.tabular.menu.tiny]
              (for [[page caption] (remove nil? tabs)]
                (let [props {:class (str "ui item"
                                         (if (= :dashboard page)
                                           (when (contains? #{:dashboard :dashboard/to-confirm :dashboard/to-merge} route-id)
                                             " active")
                                           (when (= route-id page) " active")))
                             :on-click #(commiteth.routes/nav! page)}]
                  ^{:key page} [:div props caption])))))))


(defn header-logo []
  [:div.status-header-logo
   [:div.ui.circular.image.status-logo
    [svg/status-logo]]
   [:div.logo-header "Status"]
   [:div.logo-subheader "Open Bounty"]])

(defn page-header []
  (let [user (rf/subscribe [:user])
        flash-message (rf/subscribe [:flash-message])]
    (fn []
      [:div.vertical.segment.commiteth-header
       [:div.ui.grid.container.computer.tablet.only
        [:div.four.wide.column
         [header-logo]]
        [:div.eight.wide.column.middle.aligned.computer.tablet.only.computer-tabs-container
         [tabs false]]
        [:div.four.wide.column.right.aligned.computer.tablet.only
         [user-component @user false]]]
       [:div.ui.grid.container.mobile.only
        [:div.row
         [:div.eight.wide.column.left.aligned
          [header-logo]]
         [:div.eight.wide.column.right.aligned
          [user-component @user true]]]
        [:div.row.mobile-tab-container
         [tabs true]]]
       (when @flash-message
         [flash-message-pane])])))


(defn top-hunters []
  (let [top-hunters (rf/subscribe [:top-hunters])]
    (fn []
      (if (empty? @top-hunters)
        [:div.view-no-data-container
         [:p "No recent activity yet"]]
        (into [:div.ui.items]
              (map-indexed (fn [idx hunter]
                             [:div.item
                              [:div.ui.mini.circular.image
                               [:img {:src (:avatar-url hunter)}]]
                              [:div.leader-ordinal (str (inc idx) ".")]
                              [:div.header.leader-name (:display-name hunter)]
                              [:div.leader-amount (str "$" (:total-usd hunter))]])
                           @top-hunters))))))

(defn footer []
  (let [social-links [["icon-fb" "Facebook" "https://www.facebook.com/ethstatus"]
                      ["icon-tw" "Twitter" "https://twitter.com/ethstatus"]
                      ["icon-rt" "Riot" "https://chat.status.im/#/register"]
                      ["icon-gh" "Github" "https://github.com/status-im"]
                      ["icon-rd" "Reddit" "https://www.reddit.com/r/statusim/"]
                      #_["icon-yt" "YouTube" "https://www.youtube.com/channel/UCFzdJTUdzqyX4e9dOW7UpPQ/"]]]
    [:div.commiteth-footer
     [:div.commiteth-footer-inner
      [:div.commiteth-footer-logo-container
       [:div.commiteth-footer-logo-container-inner
        [:div.commiteth-footer-logo (svg/status-logo-footer)]
        [:div.commiteth-footer-status-addr
         "Status Research & Development GmbH"
         [:br]
         "Baarerstrasse 10"
         [:br]
         "Zug, Switzerland"]]]
      [:div.commiteth-footer-table
       [:div.commiteth-footer-table__column
        [:div.commiteth-footer-header "Social networks"]
        [:ul.commiteth-footer-list
         (for [[svg caption url] social-links]
           ^{:key (random-uuid)}
           [:li.commiteth-footer-link
            [:a {:href url}
             [:div.commiteth-footer-icon
              {:style {:background-image (str "url(/img/" svg ".svg)")}}]
             [:span.commiteth-footer-link-label caption]]])]]
       [:div.commiteth-footer-table__column
        [:div.commiteth-footer-header "Community"]
        [:ul.commiteth-footer-list
         [:li.commiteth-footer-link
          [:a {:href "https://wiki.status.im/"}
           "Wiki"]]
         [:li.commiteth-footer-link
          [:a {:href "https://status.im/privacy-policy.html"}
           "Privacy policy"]]
         [:li.commiteth-footer-link
          [:a {:href "https://status.im/jobs.html"}
           "Jobs"]]]]
       [:div.commiteth-footer-table__column
        [:div.commiteth-footer-header "Language"]
        [:ul.commiteth-footer-list
         [:li.commiteth-footer-link
          [:select.commiteth-language-switcher {:name "lang"}
           [:option {:value "en"} "English"]]]]]]]
     (when-not (= "unknown" version)
       [:div.version-footer "version " [:a {:href (str "https://github.com/status-im/commiteth/commit/" version)} version]])]))

(defn page []
  (let [route (rf/subscribe [:route])
        show-top-hunters? #(contains? #{:bounties :activity} (:route-id @route))]
    (fn []
      [:div.ui.pusher
       [page-header]
       [:div.ui.vertical.segment
        [:div.ui.container
         [:div.ui.grid.stackable
          [:div {:class (str (if (show-top-hunters?) "eleven" "sixteen")
                             " wide computer sixteen wide tablet column")}
           [:div.ui.container
            (case (:route-id @route)
              :activity       [activity-page]
              :bounties       [bounties-page]
              :repos          [repos-page]
              (:dashboard
               :dashboard/to-confirm
               :dashboard/to-merge) [manage-payouts-page]
              :settings       [update-address-page]
              :usage-metrics  [usage-metrics-page])]]
          (when (show-top-hunters?)
            [:div.five.wide.column.computer.only
             [:div.ui.container.top-hunters
              [:h3.top-hunters-header "Top 5 hunters"]
              [:div.top-hunters-subheader "All time"]
              [top-hunters]]])]]]
       [footer]])))

(defn mount-components []
  (r/render [#'page] (.getElementById js/document "app")))

(defonce active-user (r/atom nil))


;; TODO: update-tokens is unnecessarily getting dispatched on every timer tick
(defn load-user []
  (if-let [login js/user]
    (if (= login @active-user)
      (do
        (println "refresh with active user, updating tokens")
        (rf/dispatch [:update-tokens js/token js/adminToken]))
      (do
        (println "active user changed, loading user data")
        (reset! active-user login)
        (rf/dispatch [:set-active-user
                      {:login login
                       :id (js/parseInt js/userId)}
                      js/token
                      js/adminToken])))
    (reset! active-user nil)))

(defn load-data [initial-load?]
  (doall (map rf/dispatch
        [[:load-open-bounties initial-load?]
         [:load-activity-feed initial-load?]
         [:load-top-hunters initial-load?]]))
  (load-user))

(defonce timer-id (r/atom nil))

(defn on-js-load []
  (when-not (nil? @timer-id)
    (js/clearInterval @timer-id))
  (reset! timer-id (js/setInterval #(load-data false) 60000))
  (mount-components))

(defn init! []
  (commiteth.routes/setup-nav!)
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch [:initialize-web3])
  (when config/debug?
    (enable-re-frisk!))
  (load-interceptors!)
  (load-data true)
  (.addEventListener js/window "click" #(rf/dispatch [:clear-flash-message]))
  (on-js-load))
