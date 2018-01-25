(ns commiteth.ui-model
  (:require [clojure.set :as set]
            [cljs-time.core :as t]
            [cljs-time.coerce :as t-coerce]
            [cljs-time.format :as t-format]))

;;;; bounty sorting types

(def bounty-sorting-types-def
  {::bounty-sorting-type|most-recent   {::bounty-sorting-type.name               "Most recent"
                                        ::bounty-sorting-type.sort-key-fn        (fn [bounty]
                                                                                   (:created-at bounty))
                                        ::bounty-sorting-type.sort-comparator-fn compare}
   ::bounty-sorting-type|lowest-value  {::bounty-sorting-type.name               "Lowest value"
                                        ::bounty-sorting-type.sort-key-fn        (fn [bounty]
                                                                                   (js/parseFloat (:value-usd bounty)))
                                        ::bounty-sorting-type.sort-comparator-fn compare}
   ::bounty-sorting-type|highest-value {::bounty-sorting-type.name               "Highest value"
                                        ::bounty-sorting-type.sort-key-fn        (fn [bounty]
                                                                                   (js/parseFloat (:value-usd bounty)))
                                        ::bounty-sorting-type.sort-comparator-fn (comp - compare)}
   ::bounty-sorting-type|owner         {::bounty-sorting-type.name               "Owner"
                                        ::bounty-sorting-type.sort-key-fn        (fn [bounty]
                                                                                   (:repo-owner bounty))
                                        ::bounty-sorting-type.sort-comparator-fn compare}})

(defn bounty-sorting-type->name [sorting-type]
  (-> bounty-sorting-types-def (get sorting-type) ::bounty-sorting-type.name))

(defn sort-bounties-by-sorting-type [sorting-type bounties]
  (let [keyfn      (-> bounty-sorting-types-def
                       sorting-type
                       ::bounty-sorting-type.sort-key-fn)
        comparator (-> bounty-sorting-types-def
                       sorting-type
                       ::bounty-sorting-type.sort-comparator-fn)]
    (sort-by keyfn comparator bounties)))

;;;; bounty filter types

(def bounty-filter-type-date-options-def {::bounty-filter-type-date-option|last-week     "Last week"
                                          ::bounty-filter-type-date-option|last-month    "Last month"
                                          ::bounty-filter-type-date-option|last-3-months "Last 3 months"})

(def bounty-filter-type-date-options (keys bounty-filter-type-date-options-def))

(def bounty-filter-types-def
  {::bounty-filter-type|value    {::bounty-filter-type.name      "Value"
                                  ::bounty-filter-type.category  ::bounty-filter-type-category|range
                                  ::bounty-filter-type.min-val   0
                                  ::bounty-filter-type.max-val   1000
                                  ::bounty-filter-type.predicate (fn [filter-value bounty]
                                                                   (let [min-val (first filter-value)
                                                                         max-val (second filter-value)]
                                                                     (<= min-val (:value-usd bounty) max-val)))}
   ::bounty-filter-type|currency {::bounty-filter-type.name                                  "Currency"
                                  ::bounty-filter-type.category                              ::bounty-filter-type-category|multiple-dynamic-options
                                  ::bounty-filter-type.re-frame-subscription-key-for-options :commiteth.subscriptions/open-bounties-currencies
                                  ::bounty-filter-type.predicate                             (fn [filter-value bounty]
                                                                                               (and (or (not-any? #{"ETH"} filter-value)
                                                                                                        (< 0 (:balance-eth bounty)))
                                                                                                    (set/subset? (->> filter-value (remove #{"ETH"}) set)
                                                                                                                 (-> bounty :tokens keys set))))}
   ::bounty-filter-type|date     {::bounty-filter-type.name      "Date"
                                  ::bounty-filter-type.category  ::bounty-filter-type-category|single-static-option
                                  ::bounty-filter-type.options   bounty-filter-type-date-options-def
                                  ::bounty-filter-type.predicate (fn [filter-value bounty]
                                                                   (when-let [created-at-inst (:created-at bounty)]
                                                                     (let [created-at-date (-> created-at-inst inst-ms t-coerce/from-long)
                                                                           filter-from     (condp = filter-value
                                                                                             ::bounty-filter-type-date-option|last-week (t/minus (t/now) (t/weeks 1))
                                                                                             ::bounty-filter-type-date-option|last-month (t/minus (t/now) (t/months 1))
                                                                                             ::bounty-filter-type-date-option|last-3-months (t/minus (t/now) (t/months 3)))
                                                                           interval        (t/interval filter-from (t/now))]
                                                                       (t/within? interval created-at-date))))}
   ::bounty-filter-type|owner    {::bounty-filter-type.name                                  "Owner"
                                  ::bounty-filter-type.category                              ::bounty-filter-type-category|multiple-dynamic-options
                                  ::bounty-filter-type.re-frame-subscription-key-for-options :commiteth.subscriptions/open-bounties-owners
                                  ::bounty-filter-type.predicate                             (fn [filter-value bounty]
                                                                                               (->> filter-value
                                                                                                    (some #{(:repo-owner bounty)})
                                                                                                    boolean))}})

(def bounty-filter-types (keys bounty-filter-types-def))

(defn bounty-filter-type->name [filter-type]
  (-> bounty-filter-types-def (get filter-type) ::bounty-filter-type.name))

(defn bounty-filter-type-date-option->name [option]
  (bounty-filter-type-date-options-def option))

(defn bounty-filter-value->short-text [filter-type filter-value]
  (cond
    (= filter-type ::bounty-filter-type|date)
    (bounty-filter-type-date-option->name filter-value)

    (#{::bounty-filter-type|owner
       ::bounty-filter-type|currency} filter-type)
    (str (bounty-filter-type->name filter-type) " (" (count filter-value) ")")

    (= filter-type ::bounty-filter-type|value)
    (str "$" (first filter-value) "-$" (second filter-value))

    :else
    (str filter-type " with val " filter-value)))

(defn filter-bounties [filters-by-type bounties]
  (let [filter-preds (->> filters-by-type
                          (remove #(nil? (val %)))
                          (map (fn [[filter-type filter-value]]
                                 (let [pred (-> bounty-filter-types-def (get filter-type) ::bounty-filter-type.predicate)]
                                   (partial pred filter-value)))))
        filters-pred (fn [bounty]
                       (every? #(% bounty) filter-preds))]
    (->> bounties
         (filter filters-pred))))
