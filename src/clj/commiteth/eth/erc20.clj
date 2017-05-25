(ns commiteth.eth.erc20
  (:require [commiteth.eth.core :as eth]
            [clojure.tools.logging :as log]))

(defn balance-of
  [token of]
  (log/debug "erc20.balance-of(token, of)" token of)
  (eth/call token
            "0x70a08231"
            of))
            