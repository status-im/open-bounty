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

(def items-per-page 20)

(defn draw-page-numbers [page-number page-count set-page-kw]
  "Draw page numbers for the pagination component.
   Inserts ellipsis when list is too long, by default
   max 6 items are allowed"
  (let [draw-page-num-fn (fn [current? i]
                           ^{:key i}
                           [:div.rectangle-rounded
                            (cond-> {} 
                              (not current?) 
                              (assoc :class "grayed-out"
                                     :on-click #(rf/dispatch [set-page-kw i])))
                            i])
        max-page-nums 6]
    [:div.page-nums-container 
     (cond (<= page-count max-page-nums)
           (for [i (map inc (range page-count))]
             (draw-page-num-fn (= i page-number) i))
           (<= page-number (- max-page-nums 3))
           (concat 
             (for [i (map inc (range (- max-page-nums 2)))]
               (draw-page-num-fn (= i page-number) i))
             [^{:key (dec max-page-nums)}
              [:div.page-nav-text [:span "..."]]]
             [(draw-page-num-fn false page-count)])
           (>= page-number (- page-count (- max-page-nums 4)))
           (concat 
             [(draw-page-num-fn false 1) 
              ^{:key 2}
              [:div.page-nav-text [:span "..."]]]
             (for [i (map inc (range (- page-count 4) page-count))]
               (draw-page-num-fn (= i page-number) i))
             )
           :else
           (concat 
             [(draw-page-num-fn false 1)
              ^{:key 2} [:div.page-nav-text [:span "..."]]]
             (for [i [(dec page-number) page-number (inc page-number)]]
               (draw-page-num-fn (= i page-number) i))
             [^{:key (dec page-count)} [:div.page-nav-text [:span "..."]]
              (draw-page-num-fn false page-count)]))]))

(defn display-data-page [{:keys [items 
                                 item-count 
                                 total-count
                                 page-number 
                                 page-count]} 
                         draw-item-fn
                         set-page-kw]
  "Draw data items along with pagination controls"
  (let [draw-items (fn []
                     (into [:div.ui.items]
                           (for [item items]
                             ^{:key item} [draw-item-fn item])))
        on-direction-click (fn [forward?]
                             #(when (or (and (< page-number page-count)
                                             forward?) 
                                        (and (< 1 page-number)
                                             (not forward?))) 
                                (rf/dispatch [set-page-kw 
                                              (if forward?
                                                (inc page-number)
                                                (dec page-number))])))
        draw-rect (fn [direction]
                    (let [forward? (= direction :forward)]
                      [:div.rectangle-rounded 
                       {:on-click (on-direction-click forward?)}
                       [:img.icon-forward-gray 
                        (cond-> {:src "icon-forward-gray.svg"}
                          forward? (assoc :class "flip-horizontal"))]]))]
    (cond (<= total-count items-per-page) 
          [draw-items]
          :else 
          [:div
           [draw-items] 
           [:div.page-nav-container
            [draw-rect :backward]
            [draw-rect :forward]
            [:div.page-nav-text [:span (str "Page " page-number " of " page-count)]]
            [draw-page-numbers page-number page-count set-page-kw]]])))


