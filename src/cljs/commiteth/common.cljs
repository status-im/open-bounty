(ns commiteth.common
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [clojure.string :as str]
            [goog.object :as gobj]
            [goog.date.relative]
            [goog.i18n.DateTimePatterns :as DateTimePatterns])
  (:import (goog.i18n DateTimeFormat)))

(defn web3
  "Return the web3 instance injected via Mist or Metamask"
  []
  (new (goog.object/getValueByKeys js/window "web3" "constructor")
     (current-provider
       (goog.object/get js/window "web3"))))

(defn input [val-ratom props]
  (fn []
    [:input
     (merge props {:type      "text"
                   :value     @val-ratom
                   :on-change #(reset! val-ratom (-> % .-target .-value))})]))

(defn dropdown [props title val-ratom items]
  "If val-ratom is set, preselect it in the dropdown.
   Otherwise, prepend title as a disabled option."
  (fn []
    [:select.ui.basic.selection.dropdown
     (merge props {:on-change
                   #(reset! val-ratom (-> % .-target .-value))
                   :default-value (or @val-ratom title)})
     (for [item items]
       ^{:key item} [:option {:value item
                              :disabled (= item title)} 
                     item])]))

(def ^:private long-ago-fmt
  (DateTimeFormat. DateTimePatterns/MONTH_DAY_FULL))

(defn human-time [date]
  "Shows a given date in a human-friendly way. For dates less than
   two weeks ago this means a relative '3 hours ago' kind of thing.
   For dates longer ago we return 'January 01'."
  (let [ms       (.getTime date)
        relative (goog.date.relative/format ms)]
    ;; Dates older than 2 weeks will not be shown as relative
    ;; https://github.com/google/closure-library/blob/99d7fa323f4c9e35ce7a97ea3cb08fc1d97d9e92/closure/goog/date/relative.js#L206
    (if-not (empty? relative)
      relative
      (goog.date.relative/formatDay ms #(.format long-ago-fmt %)))))

(defn issue-url [owner repo number]
  (str "https://github.com/" owner "/" repo "/issues/" number))

(defn pull-request-url [owner repo number]
  (str "https://github.com/" owner "/" repo "/pull/" number))

(def items-per-page 15)

(defn draw-page-numbers [page-number page-count container-element]
  "Draw page numbers for the pagination component.
  Inserts ellipsis when list is too long, by default
  max 6 items are allowed"
  (let [draw-page-num-fn (fn [current? i]
                           ^{:key i}
                           [:div.rectangle-rounded
                            (if current?
                              {:class "page-num-active"}
                              {:class "grayed-out-page-num"
                               :on-click #(do 
                                            (rf/dispatch [:set-page-number i])
                                            (when @container-element
                                              (.scrollIntoView @container-element)))})
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
                         container-element]
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
                                (rf/dispatch [:set-page-number
                                              (if forward?
                                                (inc page-number)
                                                (dec page-number))])
                                (when @container-element
                                  (.scrollIntoView @container-element))))
        draw-rect (fn [direction]
                    (let [forward? (= direction :forward)
                          gray-out? (or (and forward? (= page-number page-count))
                                        (and (not forward?) (= page-number 1)))]
                      [:div.rectangle-rounded 
                       (cond-> {:on-click (on-direction-click forward?)}
                         gray-out? (assoc :class "grayed-out-direction"))
                       [:img.icon-forward-gray 
                        (cond-> {:src "icon-forward-gray.svg"}
                          forward? (assoc :class "flip-horizontal"))]]))]
    (cond (<= total-count items-per-page) 
          [draw-items]
          :else 
          [:div
           [draw-items] 
           [:div.page-nav-container.ph4.pb4
            [:div.page-direction-container
             [draw-rect :backward]
             [draw-rect :forward]]
            [:div.page-nav-text [:span (str "Page " page-number " of " page-count)]]
            [draw-page-numbers page-number page-count container-element]]])))

(defn usd-string
  "Turn a given float into a USD currency string based on the browsers locale setting.

  A more complex and customizable approach can be found in goog.i18n.NumberFormat:
  https://google.github.io/closure-library/api/goog.i18n.NumberFormat.html"
  [usd-float]
  (.toLocaleString usd-float js/navigator.language #js {:style "currency" :currency "USD"}))
