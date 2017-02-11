(ns commiteth.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [commiteth.ajax :refer [load-interceptors!]]
            [commiteth.handlers]
            [commiteth.subscriptions]
            [commiteth.manage :refer [manage-page]]
            [commiteth.issues :refer [issues-page]]
            [commiteth.common :refer [input checkbox]]
            [commiteth.subscriptions :refer [user-address-path]]
            [commiteth.config :as config]
            [commiteth.svg :as svg]
            [clojure.set :refer [rename-keys]]
            [re-frisk.core :refer [enable-re-frisk!]])
  (:import goog.History))

(defn login-link []
  (let [user (rf/subscribe [:user])]
    (fn []
      (if-let [login (:login @user)]
        [:div.tabnav-actions
         [:span.profile-link "Signed in as "
          [:a {:href (str "https://github.com/" login)} login] " "]
         [:a.btn.tabnav-button {:href "/logout"} "Sign out"]]
        [:div.tabnav-actions.logged-out
         [:a.btn.tabnav-button {:href js/authorizeUrl} "Sign in"]]))))

(defn error-pane
  []
  (let [error (rf/subscribe [:error])]
    (fn []
      (when @error
        [:div.container
         {:style    {:background-color "#faeaea"
                     :padding          "10px"
                     :color            "red"}
          :on-click #(rf/dispatch [:clear-error])}
         (str @error)]))))

(defn save-address
  [user-id address]
  (fn [_]
    (rf/dispatch [:save-user-address user-id address])))

(defn address-settings []
  (let [user    (rf/subscribe [:user])
        user-id (:id @user)
        address (rf/subscribe [:get-in user-address-path])]
    (fn []
      [:div.tabnav-actions.float-right
       [:div.tabnav-actions.logged-in
        [:button.btn.tabnav-button
         {:type     "submit", :aria-haspopup "true"
          :on-click (save-address user-id @address)}
         "Update"]
        [:div.auto-search-group
         [(input {:placeholder  "0x0000000000000000000000000000000000000000",
                  :autoComplete "off",
                  :size         55
                  :type         "text"
                  :value-path   user-address-path})]
         [svg/octicon-broadcast]]]])))

(defn header []
  (let [page (rf/subscribe [:page])
        user (rf/subscribe [:user])]
    (fn []
      [:header.main-header
       [:div.container
        [:div.flex-table.mt-4.mb-4
         [:header.logo]
         [:a {:href "/"}
          [:img {:src "/img/logo.svg"}

           ;;[:img {:src "img/logo.png", :alt "commiteth", :width "100"}]
           ]]
         [:div.flex-table-item.flex-table-item-primary
          [:a {:href "/"}]
          [:h1.main-title.lh-condensed "commiteth"]
          [:span.main-link
           "Earn ETH by committing to open source projects"]]
         [:div.flex-table-item.flex-table-item-primary
          [login-link]]]
        [:div.tabnav
         (when @user [(address-settings)])
         [:nav.header-nav.tabnav-tabs
          [:a.tabnav-tab
           {:href  "#"
            :class (when (= :issues @page) "selected")}
           [svg/octicon-repo]
           "Open Bounties"]
          (when @user [:a.tabnav-tab
                       {:href  "#/manage"
                        :class (when (= :manage @page) "selected")}
                       [svg/octicon-organization]
                       "Manage Transactions"])]]]])))

(def pages
  {:issues #'issues-page
   :manage #'manage-page})

(defn page []
  (fn []
    [:div.app
     [:nav.main-navbar [:div.container]]
     [header]
     [error-pane]
     [(pages @(rf/subscribe [:page]))]]))

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :issues]))

(secretary/defroute "/manage" []
  (if js/user
    (rf/dispatch [:set-active-page :manage])
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

(defn load-user []
  (when-let [login js/user]
    (rf/dispatch [:set-active-user {:login login :id js/userId :token js/token}])))

(defn load-issues []
  (rf/dispatch [:load-bounties]))

(defn load-data []
  (load-issues)
  (load-user))

(js/setInterval load-data 60000)

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (when config/debug?
    (enable-re-frisk!))
  (load-interceptors!)
  (hook-browser-navigation!)
  (load-data)
  (mount-components))
