(ns commiteth.profile.page
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [input]]))

(defn save-address
  [login address]
  (fn [_]
    (rf/dispatch [:save-user-address login address])))

(defn address-settings []
  (let [user    (rf/subscribe [:user])
        login   (:login @user)
        address (rf/subscribe [:address])]
    (fn []
      [:div.form-group
       [:label "Address"]
       [input {:placeholder "Address"
               :value-path  [:address]}]
       [:button.btn.btn-primary.btn-lg
        {:on-click (save-address login @address)}
        "Save"]])))

(defn profile-page []
  (fn []
    [:div.profile-settings
     [:h1 "Profile"]
     [address-settings]]))
