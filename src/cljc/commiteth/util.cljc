(ns commiteth.util)

(defn parse-float [x]
  #?(:cljs (js/parseFloat x)
     :clj  (Float/parseFloat x)))

(defn assert-first [xs]
  (assert (first xs) "assert-first failure")
  (first xs))
