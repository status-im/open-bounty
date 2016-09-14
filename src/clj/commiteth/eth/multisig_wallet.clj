(ns commiteth.eth.multisig-wallet
  (:require [commiteth.eth.core :as eth]
            [clojure.tools.logging :as log]))

(defn is-owner?
  [contract address]
  (eth/call contract "0x0ed5bc12" address))

(defn get-owner
  [contract index]
  (eth/call contract "0xc41a360a" index))

(defn add-owner
  [contract owner]
  (log/debug "multiowned.addOwner(contract, owner)" contract owner)
  (eth/execute (eth/eth-account) contract "0x7065cb48" owner))

(defn execute
  [contract to value]
  (log/debug "multisig.execute(contract, to, value)" contract to value)
  (eth/execute (eth/eth-account) contract "0xb61d27f6" to value))

(defn confirm
  [contract hash]
  (log/debug "multisig.confirm(contract, hash)")
  (eth/execute (eth/eth-account) contract "0x797af627" hash))
