(ns commiteth.eth.multisig-wallet
  (:require [commiteth.eth.core :as eth]
            [clojure.tools.logging :as log]))

(def confirmation-topic "0xe1c52dc63b719ade82e8bea94cc41a0d5d28e4aaf536adb5e9cccc9ff8c1aeda")

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
      (subs confirmation-data 66))))

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
