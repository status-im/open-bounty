(ns commiteth.util.util)


(defn eth-decimal->str [n]
  (format "%.6f" n))

(defn usd-decimal->str [n]
  (format "%.2f" n))
