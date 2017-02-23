(ns commiteth.update-address
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [input dropdown]]
            [reagent.core :as r]
            [reagent.crypt :as crypt]))


(defn update-address-page []
  (let [user (rf/subscribe [:user])
        updating-address (rf/subscribe [:get-in [:updating-address]])
        address (r/atom @(rf/subscribe [:get-in [:user :address]]))]
    (fn []
      (let [web3 (.-web3 js/window)
            web3-accounts (into [] (when-not (nil? web3) (-> web3
                                                         .-eth
                                                         .-accounts
                                                         js->clj)))]
        (println "web3-accounts" web3-accounts)
        [:div.ui.container.grid
         [:div.ui.form.sixteen.wide.column
          [:h3 "Update address"]
          [:p "Placeholder text for explaining what an Ethereum address is."]
          [:div.field
           (if-not (empty? web3-accounts)
             [dropdown {:class "address-input"} "Select address"
              address
              (into []
                    (for [acc web3-accounts]
                      acc))]
             [:div.ui.input.address-input
              [input address {:placeholder  "0x0000000000000000000000000000000000000000"
                              :auto-complete "off"
                              :auto-correct "off"
                              :spell-check "false"
                              :max-length 42}]])]
          [:button.ui.button (merge {:on-click
                                     #(rf/dispatch [:save-user-address
                                                    (:id @user)
                                                    @address])}
                                    (when @updating-address
                                      {:class "busy loading"}))
           "Update"]]]))))
