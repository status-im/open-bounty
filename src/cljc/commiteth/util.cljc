(ns commiteth.util)

(defn parse-float [x]
  #?(:cljs (js/parseFloat x)
     :clj  (Float/parseFloat x)))
