(ns commiteth.update-address
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [input dropdown]]
            [reagent.core :as r]
            [reagent.crypt :as crypt]
            [clojure.string :as str]
            [cljs-web3.eth :as web3-eth]))


(defn update-address-page []
  (let [db (rf/subscribe [:db])
        user (rf/subscribe [:user])
        updating-address (rf/subscribe [:get-in [:updating-address]])
        address (r/atom (some-> @(rf/subscribe [:get-in [:user :address]]) 
                                str/lower-case))]
    (fn []
      (let [web3 (:web3 @db)
            web3-accounts (when web3
                            (web3-eth/accounts web3))]
        (println "web3-accounts" web3-accounts)
        [:div.ui.container.grid
         [:div.ui.form.sixteen.wide.column
          [:h3 "Update address"]
          [:p "Insert your Ethereum address in hex format."]
          [:div.field
           (if-not (empty? web3-accounts)
             ; Add value of address if it's missing from items list.
             ; If address is empty, add title 
             (let [accounts (map str/lower-case web3-accounts)
                   addr @address
                   title "Select address"
                   items (cond->> accounts
                           (and addr
                                (not (contains? (set accounts) addr)))
                           (into [addr])
                           (not addr)
                           (into [title]))]
               [dropdown {:class "address-input"} 
                title
                address
                items])
             [:div.ui.input.address-input
              [input address {:placeholder "0x0000000000000000000000000000000000000000"
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
           "UPDATE"]]]))))
