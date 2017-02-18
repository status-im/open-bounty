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
            [commiteth.activity :refer [activity-page]]
            [commiteth.repos :refer [repos-page]]
            [commiteth.bounties :refer [bounties-page]]
            [commiteth.update-address :refer [update-address-page]]
            [commiteth.manage :refer [manage-page]]
            [commiteth.issues :refer [issues-page]]
            [commiteth.common :refer [input]]
            [commiteth.config :as config]
            [commiteth.svg :as svg]
            [clojure.set :refer [rename-keys]]
            [re-frisk.core :refer [enable-re-frisk!]])
  (:import goog.History))

#_(defn error-pane
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

#_(defn save-address
  [user-id address]
  (fn [_]
    (rf/dispatch [:save-user-address user-id address])))

#_(defn address-settings []
  (let [user    (rf/subscribe [:user])
        user-id (:id @user)
        address (rf/subscribe [:get-in [:user :address]])]
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
                  :value-path   [:user :address]})]
         [svg/octicon-broadcast]]]])))


(defn user-dropdown [user items]
  (let [dropdown-open? (r/atom false)]
    (fn []
      (let [menu (if @dropdown-open?
                   [:div.ui.menu.transition.visible]
                   [:div.ui.menu])]
        [:div.ui.browse.item.dropdown
         {:on-click #(swap! dropdown-open? not)}
         (:login user)
         [:span.dropdown.icon]
         (into menu
               (for [[target caption] items]
                 ^{:key target} [:div.item
                                    [:a
                                     (if (keyword? target)
                                       {:on-click #(rf/dispatch [target])}
                                       {:href target})
                                     caption]]))]))))


(defn user-component [user]
  (if user
    (let [login (:login user)]
      [:div.ui.text.menu.user-component
       [:div.item
        [:img.ui.mini.circular.image {:src (:avatar_url user)}]]
       [user-dropdown user [[:update-address "Update address"]
                            ["/logout" "Sign out"]]]])
    [:a.ui.button.small {:href js/authorizeUrl} "Sign in"]))

(defn tabs []
  (let [user (rf/subscribe [:user])
        current-page (rf/subscribe [:page])]
    (fn []
      (let [tabs (apply conj [[:activity "Activity"]]
                        (when @user
                          [[:repos "Repositories"]
                           [:bounties "Bounties"]]))]
        (into [:div.ui.attached.tabular.menu.tiny]
              (for [[page caption] tabs]
                (let [props {:class (str "ui item"
                                         (when (= @current-page page) " active"))
                             :on-click #(rf/dispatch [:set-active-page page])}]
                  ^{:key page} [:div props caption])))))))


(defn page-header []
  (let [user (rf/subscribe [:user])]
    (fn []
      [:div.vertical.segment.commiteth-header
       [:div.ui.grid.container
        [:div.twelve.wide.column
         [:div.ui.image
          [:img.left.aligned {:src "/img/logo.svg"}]]]
        [:div.four.wide.column
         [user-component @user]]
        (when-not @user
          [:div.ui.text.content
           [:div.ui.divider.hidden]
           [:h2.ui.header "Commit ETH"]
           [:h2.ui.subheader "Earn ETH by committing to open source projects"]
           [:div.ui.divider.hidden]])
         [tabs]]])))

(def pages
  {:activity #'activity-page
   :repos #'repos-page
   :bounties #'bounties-page
   :update-address #'update-address-page})

(defn page []
  (fn []
    [:div.ui.pusher
     [page-header]
;;     [error-pane]
     [:div.ui.vertical.segment
      [:div.page-content
       [(pages @(rf/subscribe [:page]))]]]]))

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :activity]))

(secretary/defroute "/manage" []
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

(defn load-user []
  (if-let [login js/user]
    (when-not (= login @active-user)
      (println "active user changed, loading user data")
      (reset! active-user login)
      (rf/dispatch [:set-active-user
                    {:login login
                     :id (js/parseInt js/userId)
                     :token js/token}]))
    (reset! active-user nil)))

(defn load-issues []
  (rf/dispatch [:load-bounties]))

(defn load-data []
  (load-issues)
  (load-user))

(defonce timer-id (r/atom nil))

(defn on-js-load []
  (when-not (nil? @timer-id)
    (js/clearInterval @timer-id))
  (reset! timer-id (js/setInterval load-data 60000))
  (mount-components))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (when config/debug?
    (enable-re-frisk!))
  (load-interceptors!)
  (hook-browser-navigation!)
  (load-data)
  (on-js-load))
