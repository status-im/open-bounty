(ns commiteth.ui.balances)

(defn tla-color
  [tla]
  {:pre [(string? tla)]}
  (get {"ETH" "#57a7ed"} tla "#4360df"))

(defn balance-badge
  [tla balance]
  {:pre [(keyword? tla)]}
  (let [tla   (name tla)]
    [:div.dib.ph2.pv1.relative
     {:style {:color (tla-color tla)}}
     [:div.absolute.top-0.left-0.right-0.bottom-0.o-10.br2
      {:style {:background-color (tla-color tla)}}]
     [:span.pg-med (str tla " " balance)]]))

(defn balance-label
  [tla balance]
  {:pre [(keyword? tla)]}
  (let [tla   (name tla)]
    [:span.pg-med.fw5
     {:style {:color (tla-color tla)}}
     (str tla " " balance)]))

(defn usd-value-label [value-usd]
  [:span
   [:span.gray "Value "]
   [:span.dark-gray (str "$" value-usd)]])

(defn token-balances
  "Render ETH and token balances using the specified `style` (:label or :badge).

  Non-positive balances will not be rendered. ETH will always be rendered first."
  [crypto-balances style]
  [:span ; TODO consider non DOM el react wrapping
   (for [[tla balance] (-> (dissoc crypto-balances :ETH)
                           (seq)
                           (conj [:ETH (:ETH crypto-balances)]))
         :when (pos? balance)]
     ^{:key tla}
     [:div.dib.mr2
      (case style
        :label [balance-label tla balance]
        :badge [balance-badge tla balance]
        [balance-badge tla balance])])])
