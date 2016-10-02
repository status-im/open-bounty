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
            [clojure.set :refer [rename-keys]])
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
         [:svg.octicon.octicon-broadcast
          {:aria-hidden "true",
           :height      "16",
           :version     "1.1",
           :viewBox     "0 0 16 16",
           :width       "16"}
          [:path
           {:d "M9 9H8c.55 0 1-.45 1-1V7c0-.55-.45-1-1-1H7c-.55
            0-1 .45-1 1v1c0 .55.45 1 1 1H6c-.55 0-1 .45-1
            1v2h1v3c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-3h1v-2c0-.55-.45-1-1-1zM7
            7h1v1H7V7zm2 4H8v4H7v-4H6v-1h3v1zm2.09-3.5c0-1.98-1.61-3.59-3.59-3.59A3.593
            3.593 0 0 0 4 8.31v1.98c-.61-.77-1-1.73-1-2.8 0-2.48 2.02-4.5
            4.5-4.5S12 5.01 12 7.49c0 1.06-.39 2.03-1 2.8V8.31c.06-.27.09-.53.09-.81zm3.91
            0c0 2.88-1.63 5.38-4 6.63v-1.05a6.553 6.553 0 0 0 3.09-5.58A6.59 6.59 0 0 0
            7.5.91 6.59 6.59 0 0 0 .91 7.5c0 2.36 1.23 4.42 3.09 5.58v1.05A7.497
            7.497 0 0 1 7.5 0C11.64 0 15 3.36 15 7.5z"}]]]]])))

(defn header []
  (let [page (rf/subscribe [:page])
        user (rf/subscribe [:user])]
    (fn []
      [:header.main-header
       [:div.container
        [:div.flex-table.mt-4.mb-4
         [:a
          {:href "/"}
          [:img {:src "img/logo.png", :alt "commiteth", :width "100"}]]
         [:div.flex-table-item.flex-table-item-primary
          [:a {:href "/"}]
          [:h1.main-title.lh-condensed "commiteth"]
          [:span.main-link
           "earn ETH for commits"]]
         [:div.flex-table-item.flex-table-item-primary
          [login-link]]]
        [:div.tabnav
         (when @user [(address-settings)])
         [:nav.header-nav.tabnav-tabs
          [:a.tabnav-tab
           {:href  "#"
            :class (when (= :issues @page) "selected")}
           [:svg.octicon.octicon-repo
            {:aria-hidden "true",
             :height      "16",
             :version     "1.1",
             :viewBox     "0 0 12 16",
             :width       "12"}
            [:path
             {:d
              "M4 9H3V8h1v1zm0-3H3v1h1V6zm0-2H3v1h1V4zm0-2H3v1h1V2zm8-1v12c0 .55-.45 1-1 1H6v2l-1.5-1.5L3 16v-2H1c-.55 0-1-.45-1-1V1c0-.55.45-1 1-1h10c.55 0 1 .45 1 1zm-1 10H1v2h2v-1h3v1h5v-2zm0-10H2v9h9V1z"}]]
           "Open Bounties"]
          (when @user [:a.tabnav-tab
                       {:href  "#/manage"
                        :class (when (= :manage @page) "selected")}
                       [:svg.octicon.octicon-organization
                        {:aria-hidden "true",
                         :height      "16",
                         :version     "1.1",
                         :viewBox     "0 0 16 16",
                         :width       "16"}
                        [:path
                         {:d
                          "M16 12.999c0 .439-.45 1-1 1H7.995c-.539 0-.994-.447-.995-.999H1c-.54 0-1-.561-1-1 0-2.634 3-4 3-4s.229-.409 0-1c-.841-.621-1.058-.59-1-3 .058-2.419 1.367-3 2.5-3s2.442.58 2.5 3c.058 2.41-.159 2.379-1 3-.229.59 0 1 0 1s1.549.711 2.42 2.088C9.196 9.369 10 8.999 10 8.999s.229-.409 0-1c-.841-.62-1.058-.59-1-3 .058-2.419 1.367-3 2.5-3s2.437.581 2.495 3c.059 2.41-.158 2.38-1 3-.229.59 0 1 0 1s3.005 1.366 3.005 4"}]]
                       "Manage Transactions"])]]]])))

(def pages
  {:issues #'issues-page
   :manage #'manage-page})

(defn page []
  (fn []
    [:div.app
     [:nav.main-navbar [:div.container]]
     [header]
     [(pages @(rf/subscribe [:page]))]]))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :issues]))
(secretary/defroute "/manage" []
  (rf/dispatch [:set-active-page :manage]))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
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
  (load-interceptors!)
  (hook-browser-navigation!)
  (load-data)
  (mount-components))
