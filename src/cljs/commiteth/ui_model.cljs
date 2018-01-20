(ns commiteth.ui-model)

;;;; bounty sorting types

(def bounty-sorting-types-def {::bounty-sorting-type|most-recent   "Most recent"
                               ::bounty-sorting-type|lowest-value  "Lowest value"
                               ::bounty-sorting-type|highest-value "Highest value"
                               ::bounty-sorting-type|owner         "Owner"})

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
