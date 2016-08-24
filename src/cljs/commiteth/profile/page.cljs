(ns commiteth.profile.page
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [input checkbox]]
            [commiteth.subscriptions :refer [user-address-path]]
            [clojure.set :refer [rename-keys]]))

(defn save-address
  [login address]
  (fn [_]
    (rf/dispatch [:save-user-address login address])))

(defn address-settings []
  (let [user    (rf/subscribe [:user])
        login   (:login @user)
        address (rf/subscribe [:get-in user-address-path])]
    (fn []
      [:div.form-group
       [:label "Address"]
       [input {:placeholder "Address"
               :value-path  user-address-path}]
       [:button.btn.btn-primary.btn-lg
        {:on-click (save-address login @address)}
        "Save"]])))

(defn repository-row [repo]
  (let [repo-id (:id repo)]
    ^{:key repo-id}
    [:li.list-group-item
     [checkbox {:value-path [:enabled-repos repo-id]
                :on-change  #(rf/dispatch [:toggle-repo repo])}]
     [:span (:name repo)]]))

(defn repos-list []
  (let [repos (rf/subscribe [:repos])]
    (fn []
      [:ul.list-group
       (map repository-row @repos)])))

(defn profile-page []
  (fn []
    [:div.profile-settings
     [:h3 "Profile"]
     [address-settings]
     [:h3 "Repositories"]
     [repos-list]]))
