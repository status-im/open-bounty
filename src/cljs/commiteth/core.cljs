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
            [commiteth.profile.page :refer [profile-page]]
            [commiteth.home.page :refer [home-page]])
  (:import goog.History))

(defn nav-link [uri title page collapsed?]
  (let [selected-page (rf/subscribe [:page])]
    (fn []
      [:li.nav-item
       {:class (when (= page @selected-page) "active")}
       [:a.nav-link
        {:href     uri
         :on-click #(reset! collapsed? true)} title]])))

(defn login-link [collapsed?]
  (let [user (rf/subscribe [:user])]
    (fn []
      (if-let [login (:login @user)]
        [:li.pull-right.p
         [:span.profile-link "Logged in as "
          [:a {:href "/#/profile" :on-click #(reset! collapsed? true)} login]]
         [:a.btn.btn-primary.btn-sm {:href "/logout"} "Logout"]]
        [:li.pull-right
         [:a.btn.btn-social.btn-github
          {:href js/authorizeUrl}
          [:i.fa.fa-github]
          "Sign in with GitHub"]]))))

(defn navbar []
  (r/with-let [collapsed? (r/atom true)]
    (fn []
      [:nav.navbar.navbar-light.bg-faded
       [:button.navbar-toggler.hidden-sm-up
        {:on-click #(swap! collapsed? not)} "☰"]
       [:div.collapse.navbar-toggleable-xs
        (when-not @collapsed? {:class "in"})
        [:ul.nav.navbar-nav
         [nav-link "#/" "Home" :home collapsed?]
         [login-link collapsed?]]]])))

(def pages
  {:home    #'home-page
   :profile #'profile-page})

(defn page []
  (fn []
    [:div.app
     [navbar]
     [:div.container [(pages @(rf/subscribe [:page]))]]]))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))
(secretary/defroute "/profile" []
  (rf/dispatch [:set-active-page :profile]))

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

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (load-interceptors!)
  (hook-browser-navigation!)
  (load-user)
  (mount-components))
