(ns commiteth.update-address
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [input dropdown]]
            [reagent.core :as r]
            [reagent.crypt :as crypt]
            [cljs-web3.eth :as web3-eth]))

(defn update-address-page-contents []
  (let [db (rf/subscribe [:db])
        updating-user (rf/subscribe [:get-in [:updating-user]])
        address (r/atom @(rf/subscribe [:get-in [:user :address]]))
        hidden (r/atom @(rf/subscribe [:get-in [:user :is_hidden_in_hunters]]))]

    (fn []
      (let [web3 (:web3 @db)
            web3-accounts (when web3
                            (web3-eth/accounts web3))]
        [:div.ui.container.grid
         [:div.ui.form.sixteen.wide.column
          [:h3 "Update address"]
          [:p "Insert your Ethereum address in hex format."]
          [:div.field
           (if-not (empty? web3-accounts)
             [dropdown {:class "address-input"} "Select address"
              address
              (vec
               (for [acc web3-accounts]
                 acc))]
             [:div.ui.input.address-input
              [input address {:placeholder  "0x0000000000000000000000000000000000000000"
                              :auto-complete "off"
                              :auto-correct "off"
                              :spell-check "false"
                              :max-length 42}]])]

          [:h3 "Settings"]

          [:div
           [:input
            {:type :checkbox
             :disabled @updating-user
             :id :input-hidden
             :checked @hidden
             :on-change
             (fn [e]
               (let [value (-> e .-target .-checked)]
                 (reset! hidden value)))}]

           [:label {:for :input-hidden} "Disguise myself from the top hunters and activity lists."]]

          [:button
           (merge {:on-click
                   #(rf/dispatch [:save-user-fields {:address @address
                                                     :is_hidden_in_hunters @hidden}])
                   :class (str "ui button small update-address-button"
                               (when @updating-user
                                 " busy loading"))})
           "UPDATE"]]]))))

(defn update-address-page []
  (let [loaded? @(rf/subscribe [:user-profile-loaded?])]
    (if loaded?
      [update-address-page-contents]
      [:div.view-loading-container
       [:div.ui.active.inverted.dimmer
        [:div.ui.text.loader.view-loading-label "Loading"]]])))
