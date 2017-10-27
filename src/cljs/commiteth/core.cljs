(ns commiteth.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [commiteth.ajax :refer [load-interceptors!]]
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

(def user-dropdown-open? (r/atom false))

(defn user-dropdown [user items]
  (let [menu (if @user-dropdown-open?
                  [:div.ui.menu.transition.visible]
                  [:div.ui.menu.transition.hidden])
        avatar-url (:avatar_url user)]
    [:div.ui.left.item.dropdown
     {:on-click #(swap! user-dropdown-open? not)}
     [:div.item
      [:img.ui.mini.circular.image {:src avatar-url}]]
     [:div.item
      (:login user)]
     [:div.item
      [svg/dropdown-icon]]
     (into menu
           (for [[target caption props] items]
             ^{:key target} [:div.item
                             [:a
                              (merge props
                                     (if (keyword? target)
                                       {:on-click #(rf/dispatch [target])}
                                       {:href target}))
                              caption]]))]))


(defn user-component [user]
  (let [user (rf/subscribe [:user])]
    (fn []
      (if @user
        [:div.ui.text.menu.user-component
         [:div.item
          [user-dropdown @user [[:update-address "My Payment Details" {}]
                                ["/logout" "Sign Out" {:class "logout-link"}]]]]]
        [:a.ui.button.small.login-button {:href js/authorizeUrl} "LOG IN \u2192"]))))

(defn tabs []
  (let [user (rf/subscribe [:user])
        current-page (rf/subscribe [:page])]
    (fn []
      (let [tabs (apply conj [[:bounties (str (when-not @user "Open ") "Bounties")]
                              [:activity "Activity"]]
                        (when @user
                          [[:repos "Repositories"]
                           [:manage-payouts "Manage Payouts"]
                           (when (:status-team-member? @user)
                             [:usage-metrics "Usage metrics"])]))]
        (into [:div.ui.attached.tabular.menu.tiny.commiteth-tabs]
              (for [[page caption] tabs]
                (let [props {:class (str "ui item"
                                         (when (= @current-page page) " active"))
                             :on-click #(rf/dispatch [:set-active-page page])}]
                  ^{:key page} [:div props caption])))))))


(defn page-header []
  (let [user (rf/subscribe [:user])
        flash-message (rf/subscribe [:flash-message])]
    (fn []
      [:div.vertical.segment.commiteth-header
       [:div.ui.grid.container
        [:div.four.wide.column
         [:div.ui.grid
          [:div.ui.four.wide.column
           [:div.ui.circular.image.status-logo
            [svg/status-logo]]]
          [:div.ui.twelve.wide.column.left.aligned
           [:div.logo-header "Status"]
           [:div.logo-subheader "Open Bounty"]]]]
        [:div.eight.wide.column
         [tabs]]
        [:div.four.wide.column
         [user-component @user]]
        (when @flash-message
          [flash-message-pane])]])))


(def pages
  {:activity #'activity-page
   :bounties #'bounties-page
   :repos #'repos-page
   :manage-payouts #'manage-payouts-page
   :update-address #'update-address-page
   :usage-metrics #'usage-metrics-page})


(defn top-hunters []
  (let [top-hunters (rf/subscribe [:top-hunters])]
    (fn []
      (if (empty? @top-hunters)
        [:div.ui.text "No data"]
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
  [:div.commiteth-footer "Built by " [:a {:href "https://status.im"} "Status"]
   (when-not (= "unknown" version)
          [:div.version-footer "version " [:a {:href (str "https://github.com/status-im/commiteth/commit/" version)} version]])])

(defn page []
  (let [current-page (rf/subscribe [:page])
        show-top-hunters? #(contains? #{:bounties :activity} @current-page)]
    (fn []
      [:div.ui.pusher
       [page-header]
       [:div.ui.vertical.segment
        [:div.ui.container
         [:div.ui.grid.stackable
          [:div {:class (str (if (show-top-hunters?) "ten" "sixteen")
                             " wide computer sixteen wide tablet column")}
           [:div.ui.container
            [(pages @current-page)]]]
          (when (show-top-hunters?)
            [:div.six.wide.column.computer.only
             [:div.ui.container.top-hunters
              [:h3.top-hunters-header "Top hunters"]
              [:div.top-hunters-subheader "All time"]
              [top-hunters]]])]
         [footer]]]])))

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :bounties]))

(secretary/defroute "/repos" []
  (if js/user
    (rf/dispatch [:set-active-page :repos])
    (secretary/dispatch! "/")))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

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

(defn load-data []
  (doall (map rf/dispatch
        [[:load-open-bounties]
         [:load-activity-feed]
         [:load-top-hunters]]))
  (load-user))

(defonce timer-id (r/atom nil))

(defn on-js-load []
  (when-not (nil? @timer-id)
    (js/clearInterval @timer-id))
  (reset! timer-id (js/setInterval load-data 60000))
  (mount-components))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch [:initialize-web3])
  (when config/debug?
    (enable-re-frisk!))
  (load-interceptors!)
  (hook-browser-navigation!)
  (load-data)
  (on-js-load))
