(ns commiteth.common
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn input [val-ratom props]
  (fn []
    [:input
     (merge props {:type      "text"
                   :value     @val-ratom
                   :on-change #(reset! val-ratom (-> % .-target .-value))})]))

(defn dropdown [title val-ratom items]
  (fn []
    (if (= 1 (count items))
      (reset! val-ratom (first items)))
    [:select.ui.basic.selection.dropdown
     {:on-change #(reset! val-ratom (-> % .-target .-value))}
     (doall (for [item items]
              ^{:key item} [:option
                            {:value item}
                            item]))]))
