(ns commiteth.common
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(defn input [{:keys [value-path]}]
  (let [init-val @(rf/subscribe [:get-in value-path])
        val      (reagent/atom init-val)
        save     #(let [v (-> @val str clojure.string/trim)]
                   (when (seq v)
                     (rf/dispatch [:assoc-in value-path v])))]
    (fn [props]
      [:input.form-control
       (merge props {:type       "text"
                     :value      @val
                     :auto-focus true
                     :on-blur    save
                     :on-change  #(reset! val (-> % .-target .-value))})])))
