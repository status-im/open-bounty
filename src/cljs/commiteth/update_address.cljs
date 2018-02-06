(ns commiteth.update-address
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [input dropdown]]
            [reagent.core :as r]
            [reagent.crypt :as crypt]
            [cljs-web3.eth :as web3-eth]))

(defn update-address-page []
  (let [db (rf/subscribe [:db])
        user (rf/subscribe [:user])
        updating-user (rf/subscribe [:get-in [:updating-user]])
        address (r/atom @(rf/subscribe [:get-in [:user :address]]))
        hidden (rf/subscribe [:get-in [:user :is_hidden]])]

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
          [:button
           (merge {:on-click
                   #(rf/dispatch [:save-user-fields (:id @user) {:address @address}])
                   :class (str "ui button small update-address-button"
                               (when @updating-user
                                 " busy loading"))})
           "UPDATE"]

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
                 (rf/dispatch [:save-user-fields (:id @user) {:is_hidden value}])))}]

           [:label {:for :input-hidden} "Disguise myself from the top hunters and activity lists."]]]]))))
