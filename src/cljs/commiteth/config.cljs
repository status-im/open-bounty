(ns commiteth.config)

(def debug?
  ^boolean js/goog.DEBUG)

(defn on-testnet? []
  (not (or (empty? js/onTestnet)
           (= "false" js/onTestnet))))
