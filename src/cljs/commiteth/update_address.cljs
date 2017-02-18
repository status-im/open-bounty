(ns commiteth.update-address
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [input dropdown]]
            [reagent.core :as r]
            [reagent.crypt :as crypt]))


(defn update-address-page []
  (let [user (rf/subscribe [:user])
        web3 (.-web3 js/window)
        web3-accounts (into [] (when-not (nil? web3) (-> web3
                                                         .-eth
                                                         .-accounts
                                                         js->clj)))
        address (r/atom @(rf/subscribe [:get-in [:user :address]]))]
    (fn []
      (println "web3-accounts" web3-accounts)
      [:div.ui.container.grid
       [:div.ui.form.eight.wide.column
        [:h3 "Update address"]
        [:p "Placeholder text for explaining what an Ethereum address is."]
        [:div.field
         (if-not (empty? web3-accounts)
           [dropdown "Select address"
            address
            (into []
                  (for [acc web3-accounts]
                    acc))]
           [:div.ui.input
            [input address {:placeholder  "0x0000000000000000000000000000000000000000"
                            :auto-complete "off"
                            :max-length 42}]])]
        [:button.ui.button {:on-click
                            #(rf/dispatch [:save-user-address
                                           (:id @user)
                                           @address])}
         "Update"]]])))
