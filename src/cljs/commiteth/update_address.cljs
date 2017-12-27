(ns commiteth.update-address
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [input dropdown checkbox]]
            [reagent.core :as r]
            [reagent.crypt :as crypt]
            [cljs-web3.eth :as web3-eth]))

(defn update-address-page []
  (let [db (rf/subscribe [:db])
        user (rf/subscribe [:user])
        updating-address (rf/subscribe [:get-in [:updating-address]])
        address (r/atom @(rf/subscribe [:get-in [:user :address]]))
        hidden (r/atom @(rf/subscribe [:get-in [:user :is_hidden]]))]

    (add-watch hidden
     :default
     (fn [_ _ _ new-val]
       (rf/dispatch [:mark-user-hidden (:id @user) new-val])))

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
                   #(rf/dispatch [:save-user-address
                                  (:id @user)
                                  @address])
                   :class (str "ui button small update-address-button"
                               (when @updating-address
                                 " busy loading"))})
           "UPDATE"]

          [:h3 "Settings"]
          [:div
           [checkbox hidden {:id :input-hidden}]
           [:label {:for :input-hidden} "Disguise myself from the top hunters and activity lists."]]]]))))
