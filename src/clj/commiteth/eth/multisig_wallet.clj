(ns commiteth.eth.multisig-wallet
  (:require [commiteth.eth.core :as eth]
            [clojure.tools.logging :as log]))

(defonce method-ids
  {:submit-transaction (eth/sig->method-id "submitTransaction(address,uint256,bytes)")
   :withdraw-everything (eth/sig->method-id "withdrawEverything(address)")})

(defonce factory "0xbcBc5b8cE5c76Ed477433636926f76897401f838")

(defonce factory-topic "0x96b5b9b8a7193304150caccf9b80d150675fa3d6af57761d8d8ef1d6f9a1a909")

(defonce confirmation-topic "0xc0ba8fe4b176c1714197d43b9cc6bcf797a4a7461c5fe8d0ef6e184ae7601e51")

(defn create-new 
  [owner1 owner2 required]
  (eth/execute (eth/eth-account)
               factory 
               "0xf8f73808"
               0x40
               0x2
               required
               owner1
               owner2))

(defn execute
  [contract to value]
  (log/debug "multisig.execute(contract, to, value)" contract to value)
  (eth/execute (eth/eth-account)
               contract
               (:submit-transaction method-ids)
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

(defn find-factory-hash
  [receipt]
  (let [logs                   (:logs receipt)
        has-factory-event #(some (fn [topic] (= topic
                                                    factory-topic))
                                      (:topics %))
        factory-event     (first (filter has-factory-event logs))
        factory-data      (:data factory-event)]
    (when factory-data
      (subs factory-data 2 66))))
      
      
(defn send-all
  [contract to]
  (log/debug "multisig.send-all(contract, to)" contract to)
  (let [params (eth/format-call-params
                (:withdraw-everything method-ids)
                to)]
    (eth/execute (eth/eth-account)
                 contract
                 (:submit-transaction method-ids)
                 contract
                 0
                 "0x60"
                 "0x24"
                 params)))


  (defn watch-token 
    [contract token]
    (log/debug "multisig.watch-token(contract, token)" contract token)
    (eth/execute (eth/eth-account)
    contract
    "0xf375c07a"
    token
    0))
  
  (defn token-balance
    [contract token]
    (eth/call contract "0x523fba7f" token))  

  (defn tokens-list
    [contract]
    (eth/call contract "0x273cbaa0"))  
  
