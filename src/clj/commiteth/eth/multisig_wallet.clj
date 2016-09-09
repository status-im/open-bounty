(ns commiteth.eth.multisig-wallet
  (:require [commiteth.eth.core :as eth]))

(defn is-owner?
  [contract address]
  (eth/call contract "0x0ed5bc12" address))

(defn get-owner
  [contract index]
  (eth/call contract "0xc41a360a" index))

(defn add-owner
  [contract owner]
  (eth/execute (eth/eth-account) contract "0x7065cb48" owner))
