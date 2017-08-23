(ns commiteth.util.util)


(defn eth-decimal->str [n]
  (format "%.4f" n))

(defn usd-decimal->str [n]
  (format "%.2f" n))
