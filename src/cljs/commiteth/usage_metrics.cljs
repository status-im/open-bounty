(ns commiteth.usage-metrics
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [cljs-time.core :as time]
            [cljs-time.coerce :as time-coerce]
            [cljsjs.chartjs]))


(defn usage-metrics-chart
  [data]
  (let [day-labels [nil "Mo" "Tu" "We" "Th" "Fr" "Sa" "Su"]
        context (.getContext (.getElementById js/document "chart-canvas") "2d")
        chart-data {:type "bar"
                    :stacked true
                    :options {:scales {:yAxes [{:ticks {:beginAtZero true}}]}}
                    :data {:labels (mapv #(get day-labels
                                               (-> (:day %)
                                                   time-coerce/from-date
                                                   time/day-of-week))
                                         data)
                           :datasets [{:data (mapv :registered_users data)
                                       :label "Registered users"
                                       :backgroundColor "#90EE90"}
                                      {:data (mapv :users_with_address data)
                                       :label "Users with address"
                                       :backgroundColor "#F08080"}]}}]

    (js/Chart. context (clj->js chart-data))))

(defn chartjs-component
  []
  (let [usage-metrics (rf/subscribe [:usage-metrics])]
    (fn []
      (r/create-class
       {:component-did-mount #(usage-metrics-chart @usage-metrics)
        :display-name        "chartjs-component"
        :reagent-render      (fn []
                               [:canvas {:id "chart-canvas"
                                         :width "700"
                                         :height "380"}])}))))


(defn usage-metrics-page []
  (rf/dispatch [:load-usage-metrics])
  (let [metrics-loading? (rf/subscribe [:metrics-loading?])]
    (fn []
      (if @metrics-loading?
        [:div
         [:div.ui.active.inverted.dimmer
          [:div.ui.text.loader "Loading"]]]
        [:div
         [:h2 "Usage metrics for the past 30 days"]
         [chartjs-component]]))))
