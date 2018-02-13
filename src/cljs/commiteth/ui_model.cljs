(ns commiteth.ui-model
  (:require [clojure.set :as set]
            [cljs-time.core :as t]
            [cljs-time.coerce :as t-coerce]
            [cljs-time.format :as t-format]))

;;;; bounty sorting types

(def bounty-sorting-types-def
  {::bounty-sorting-type|most-recent   {::bounty-sorting-type.name               "Most recent"
                                        ::bounty-sorting-type.sort-key-fn        (fn [bounty]
                                                                                   (:updated bounty))
                                        ::bounty-sorting-type.sort-comparator-fn (comp - compare)}
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

(defn bounty-filter-type-date-option->name [option]
  (bounty-filter-type-date-options-def option))

(def bounty-filter-type-date-pre-predicate-value-processor
  "It converts an option of the filter type date to a cljs-time interval in which
  that option is valid, so that you can check `cljs-time.core.within?` against that
  interval and know if a `cljs-time` date is valid for that filter type date option."
  (fn [filter-value]
    (let [filter-from (condp = filter-value
                        ::bounty-filter-type-date-option|last-week (t/minus (t/now) (t/weeks 1))
                        ::bounty-filter-type-date-option|last-month (t/minus (t/now) (t/months 1))
                        ::bounty-filter-type-date-option|last-3-months (t/minus (t/now) (t/months 3)))]
      (t/interval filter-from (t/now)))))
(def bounty-filter-type-date-predicate
  (fn [filter-value-interval bounty]
    (when-let [date-inst (:updated bounty)]
      (let [date (-> date-inst inst-ms t-coerce/from-long)]
        (t/within? filter-value-interval date)))))

(def bounty-filter-type-claims-options-def {::bounty-filter-type-claims-option|no-claims "Not claimed yet"})

(def bounty-filter-type-claims-options (keys bounty-filter-type-claims-options-def))

(defn bounty-filter-type-claims-option->name [option]
  (bounty-filter-type-claims-options-def option))

(def bounty-filter-types-def
  {::bounty-filter-type|value
   {::bounty-filter-type.name      "Value"
    ::bounty-filter-type.category  ::bounty-filter-type-category|range
    ::bounty-filter-type.min-val   0
    ::bounty-filter-type.max-val   10000
    ::bounty-filter.type.header    "$0 - $10000+"
    ::bounty-filter-type.predicate (fn [filter-value bounty]
                                     (let [min-val (first filter-value)
                                           max-val (second filter-value)]
                                       (<= min-val (:value-usd bounty) max-val)))}

   ::bounty-filter-type|currency
   {::bounty-filter-type.name                          "Currency"
    ::bounty-filter-type.category                      ::bounty-filter-type-category|multiple-dynamic-options
    ::bounty-filter-type.re-frame-subs-key-for-options :commiteth.subscriptions/open-bounties-currencies
    ::bounty-filter-type.predicate                     (fn [filter-value bounty]
                                                         (or (and (some #{"ETH"} filter-value)
                                                                  (< 0 (:balance-eth bounty)))
                                                             (not-empty (set/intersection
                                                                          (->> filter-value (remove #{"ETH"}) set)
                                                                          (-> bounty :tokens keys set)))))}

   ::bounty-filter-type|date
   {::bounty-filter-type.name                          "Date"
    ::bounty-filter-type.category                      ::bounty-filter-type-category|single-static-option
    ::bounty-filter-type.options                       bounty-filter-type-date-options-def
    ::bounty-filter-type.pre-predicate-value-processor bounty-filter-type-date-pre-predicate-value-processor
    ::bounty-filter-type.predicate                     bounty-filter-type-date-predicate}

   ::bounty-filter-type|owner
   {::bounty-filter-type.name                          "Owner"
    ::bounty-filter-type.category                      ::bounty-filter-type-category|multiple-dynamic-options
    ::bounty-filter-type.re-frame-subs-key-for-options :commiteth.subscriptions/open-bounties-owners-sorted
    ::bounty-filter-type.predicate                     (fn [filter-value bounty]
                                                         (->> filter-value
                                                              (some #{(:repo-owner bounty)})
                                                              boolean))}

   ::bounty-filter-type|claims
   {::bounty-filter-type.name      "Claims"
    ::bounty-filter-type.category  ::bounty-filter-type-category|single-static-option
    ::bounty-filter-type.options   bounty-filter-type-claims-options-def
    ::bounty-filter-type.predicate (fn [filter-value bounty]
                                     (condp = filter-value
                                       ::bounty-filter-type-claims-option|no-claims
                                       (= 0 (:claim-count bounty))))}})

(def bounty-filter-types (keys bounty-filter-types-def))

(defn bounty-filter-type->name [filter-type]
  (-> bounty-filter-types-def (get filter-type) ::bounty-filter-type.name))

(defn bounty-filter-value->short-text [filter-type filter-value]
  (cond
    (= filter-type ::bounty-filter-type|date)
    (bounty-filter-type-date-option->name filter-value)

    (#{::bounty-filter-type|owner
       ::bounty-filter-type|currency} filter-type)
    (str (bounty-filter-type->name filter-type) " (" (count filter-value) ")")

    (= filter-type ::bounty-filter-type|value)
    (str "$" (first filter-value) "-$" (second filter-value))

    (= filter-type ::bounty-filter-type|claims)
    (bounty-filter-type-claims-option->name filter-value)

    :else
    (str filter-type " with val " filter-value)))

(defn- bounty-filter-values-by-type->predicates [filters-by-type]
  "It receives a map with filter types as keys and filter values as values and
  returns a lazy seq of predicates, one for each pair of filter type and value.
  Those predicate can receive a bounty and tell whether that bounty passes
  the filter type with that filter value. It removes filter types with a `nil`
  filter value."
  (->> filters-by-type
       ; used `nil?` because a valid filter value can be `false`
       (remove #(nil? (val %)))
       (map (fn [[filter-type filter-value]]
              (let [filter-type-def    (bounty-filter-types-def filter-type)
                    pred               (::bounty-filter-type.predicate filter-type-def)
                    pre-pred-processor (::bounty-filter-type.pre-predicate-value-processor filter-type-def)
                    filter-value       (cond-> filter-value
                                               pre-pred-processor pre-pred-processor)]
                (partial pred filter-value))))))

(defn filter-bounties [filters-by-type bounties]
  (let [filter-preds (bounty-filter-values-by-type->predicates filters-by-type)
        filters-pred (fn [bounty]
                       (every? #(% bounty) filter-preds))]
    (cond->> bounties
             (not-empty filter-preds) (filter filters-pred))))
