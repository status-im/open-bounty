(ns commiteth.eth.erc20
  (:require [commiteth.eth.core :as eth]
            [clojure.tools.logging :as log]))

(defn balance-of
  [token-addr owner-addr]
  (let [method-id (eth/sig->method-id "balanceOf(address)")]
    (log/debug "erc20.balance-of(token-addr, owner-addr)" token-addr owner-addr)
    (eth/call token-addr
              method-id
              owner-addr)))
