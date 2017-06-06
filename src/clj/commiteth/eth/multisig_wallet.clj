(ns commiteth.eth.multisig-wallet
  (:require [commiteth.eth.core :as eth]
            [clojure.tools.logging :as log]))

(def confirmation-topic "0x1733cbb53659d713b79580f79f3f9ff215f78a7c7aa45890f3b89fc5cddfbf32")

(defn get-owner
  [contract index]
  (eth/call contract "0xc41a360a" index))

(defn execute
  [contract to value]
  (log/debug "multisig.execute(contract, to, value)" contract to value)
  (eth/execute (eth/eth-account)
               contract
               "0xb61d27f6"
               to
               value
               "0x60"
               "0x0"
               "0x0"))

(defn find-confirmation-hash
  [receipt]
  (let [logs                   (:logs receipt)
        has-confirmation-event #(some (fn [topic] (= topic
                                                    confirmation-topic))
                                      (:topics %))
        confirmation-event     (first (filter has-confirmation-event logs))
        confirmation-data      (:data confirmation-event)]
    (when confirmation-data
      (subs confirmation-data 2 66))))

  (defn erc20-transfer 
    [contract token to amount]
    (log/debug "multisig.erc20-transfer(contract, token, to, amount)" contract token to amount)
    (eth/execute (eth/eth-account)
                contract
                "0xb61d27f6"
                token
                0
                "0x60"
                "0x44"
                (eth/format-call-params "0xa9059cbb" to amount)))
                