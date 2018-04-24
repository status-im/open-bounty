(ns commiteth.util)

(defn parse-float [x]
  #?(:cljs (js/parseFloat x)
     :clj  (Float/parseFloat x)))

(defn assert-first [xs]
  (assert (first xs) "assert-first failure")
  (first xs))

(defn sum-maps
  "Take a collection of maps and sum the numeric values for all keys in those maps."
  [maps]
  (let [sum-keys (fn sum-keys [r k v]
                   (update r k (fnil + 0) v))]
    (reduce (fn [r m]
              (reduce-kv sum-keys r m))
            {}
            maps)))
