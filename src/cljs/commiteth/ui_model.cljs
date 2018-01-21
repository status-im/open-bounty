(ns commiteth.ui-model)

;;;; bounty sorting types

(def bounty-sorting-types-def
  {::bounty-sorting-type|most-recent   {::bounty-sorting-type.name               "Most recent"
                                        ::bounty-sorting-type.sort-key-fn        (fn [bounty]
                                                                                   (:created-at bounty))
                                        ::bounty-sorting-type.sort-comparator-fn compare}
   ::bounty-sorting-type|lowest-value  {::bounty-sorting-type.name               "Lowest value"
                                        ::bounty-sorting-type.sort-key-fn        (fn [bounty]
                                                                                   (:value-usd bounty))
                                        ::bounty-sorting-type.sort-comparator-fn compare}
   ::bounty-sorting-type|highest-value {::bounty-sorting-type.name               "Highest value"
                                        ::bounty-sorting-type.sort-key-fn        (fn [bounty]
                                                                                   (:value-usd bounty))
                                        ::bounty-sorting-type.sort-comparator-fn (comp - compare)}
   ::bounty-sorting-type|owner         {::bounty-sorting-type.name               "Owner"
                                        ::bounty-sorting-type.sort-key-fn        (fn [bounty]
                                                                                   (:repo-owner bounty))
                                        ::bounty-sorting-type.sort-comparator-fn compare}})

(defn bounty-sorting-type->name [sorting-type]
  (-> bounty-sorting-types-def (get sorting-type) ::bounty-sorting-type.name))

(defn sort-bounties-by-sorting-type [sorting-type bounties]
  (let [keyfn (-> bounty-sorting-types-def
                  sorting-type
                  ::bounty-sorting-type.sort-key-fn)
        comparator (-> bounty-sorting-types-def
                       sorting-type
                       ::bounty-sorting-type.sort-comparator-fn)]
    (sort-by keyfn comparator bounties)))

;;;; bounty filter types

(def bounty-filter-types-def {::bounty-filter-type|value    "Value"
                              ::bounty-filter-type|currency "Currency"
                              ::bounty-filter-type|date     "Date"
                              ::bounty-filter-type|owner    "Owner"})

(def bounty-filter-types (keys bounty-filter-types-def))

(defn bounty-filter-type->name [filter-type]
  (bounty-filter-types-def filter-type))

(def bounty-filter-type-date-options-def {::bounty-filter-type-date-option|last-week     "Last week"
                                          ::bounty-filter-type-date-option|last-month    "Last month"
                                          ::bounty-filter-type-date-option|last-3-months "Last 3 months"})

(def bounty-filter-type-date-options (keys bounty-filter-type-date-options-def))

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
