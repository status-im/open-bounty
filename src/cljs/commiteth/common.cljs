(ns commiteth.common
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [cljsjs.moment]))

(defn input [val-ratom props]
  (fn []
    [:input
     (merge props {:type      "text"
                   :value     @val-ratom
                   :on-change #(reset! val-ratom (-> % .-target .-value))})]))

(defn checkbox
  "Common checkbox widget. Takes a boolean value wrapped into an atom
  and updates its state when clicking in it. An additional `opt` map
  is to override its attributes (class, id, etc)."
  [val-atom & [opt]]
  [:input
   (merge
    {:type :checkbox
     :checked @val-atom
     :on-change
     (fn [e]
       (let [value (-> e .-target .-checked)]
         (reset! val-atom value)))}
    opt)])

(defn dropdown [props title val-ratom items]
  (fn []
    (if (= 1 (count items))
      (reset! val-ratom (first items)))
    [:select.ui.basic.selection.dropdown
     (merge props {:on-change
                   #(reset! val-ratom (-> % .-target .-value))})
     (doall (for [item items]
              ^{:key item} [:option
                            {:value item}
                            item]))]))

(defn moment-timestamp [time]
  (let [now (.now js/Date.)
        js-time (clj->js time)]
    (.to (js/moment.utc) js-time)))

(defn issue-url [owner repo number]
  (str "https://github.com/" owner "/" repo "/issues/" number))
