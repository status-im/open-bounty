(ns commiteth.common
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]))

(defn input [{:keys [value-path] :as props}]
  (let [init-val @(rf/subscribe [:get-in value-path])
        props-clean (dissoc props :value-path)
        val      (reagent/atom init-val)
        save     #(let [v (-> @val str clojure.string/trim)]
                    (when (seq v)
                      (rf/dispatch [:assoc-in value-path v])))]
    (fn []
      [:input.form-control
       (merge props-clean {:type      "text"
                           :value     @val
                           :on-blur   save
                           :on-change #(reset! val (-> % .-target .-value))})])))

(defn checkbox [{:keys [value-path on-change]}]
  (let [init-val @(rf/subscribe [:get-in value-path])
        val      (reagent/atom init-val)]
    (fn [props]
      (let [props-clean (dissoc props :value-path)]
        [:input.form-control
         (merge {:type     "checkbox"
                 :checked  @val
                 :on-change #(let [new-val (not @val)]
                               (on-change)
                               (rf/dispatch [:assoc-in value-path
                                             (reset! val new-val)]))}
                props-clean)]))))
