(ns commiteth.eth.multisig-wallet
  (:require [commiteth.eth.core :as eth]
            [commiteth.config :refer [env]]
            [commiteth.eth.web3j
             :refer [create-web3j creds]]
            [clojure.tools.logging :as log]
            [commiteth.eth.token-data :as token-data])
  (:import [org.web3j
            abi.datatypes.Address]
           [commiteth.eth.contracts MultiSigTokenWallet]))

(def method-ids
  (into {}
        (map (fn [[k signature]]
               [k (eth/sig->method-id signature)])
             {:submit-transaction "submitTransaction(address,uint256,bytes)"
              :withdraw-everything "withdrawEverything(address)"
              :token-balances "tokenBalances(address)"
              :get-token-list "getTokenList()"
              :create "create(address[],uint256)"
              :watch "watch(address)"
              :balance-of "balanceOf(address)"})))

(def topics
  {:factory-create (eth/event-sig->topic-id "Create(address,address)")
   :submission (eth/event-sig->topic-id "Submission(uint256)")})

(defn factory-contract-addr []
  (env :contract-factory-addr "0x47F56FD26EEeCda4FdF5DB5843De1fe75D2A64A6"))

(defn create-new
  [owner1 owner2 required]
  (eth/execute (eth/eth-account)
               (factory-contract-addr)
               (:create method-ids)
               0x40
               0x2
               required
               owner1
               owner2))

(defn deploy-multisig
  [owner]
  (create-new (eth/eth-account) owner 2))


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

(defn find-event-in-tx [tx-receipt topic-id]
  (let [logs (:logs tx-receipt)
        correct-topic? (fn [topic]
                         (= topic topic-id))
        has-correct-event? #(some correct-topic?
                                  (:topics %))
        event     (first (filter has-correct-event? logs))]
    (:data event)))


(defn find-confirmation-hash
  [tx-receipt]
  (let [confirmation-data (find-event-in-tx tx-receipt (:submission topics))]
    (when confirmation-data
      (subs confirmation-data 2 66))))


(defn find-created-multisig-address
  [tx-receipt]
  (let [factory-data (find-event-in-tx tx-receipt (:factory-create topics))]
    (when factory-data
      (str "0x" (subs factory-data 26)))))


(defn send-all ;; TODO: not tested
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


(defn get-token-address [token]
  (let [token-details (token-data/token-info token)]
    (assert token-details)
    (:address token-details)))

(defn watch-token
  [bounty-addr token]
  (log/debug "multisig.watch-token(contract, token)" bounty-addr token)
  (let [token-address (get-token-address token)]
    (assert token-address)
    (eth/execute (eth/eth-account)
                 bounty-addr
                 (:watch method-ids)
                 token-address)))


(defn load-bounty-contract [addr]
  (MultiSigTokenWallet/load addr
    (create-web3j)
    (creds)
    (eth/gas-price)
    (BigInteger/valueOf 500000)))

(defn convert-token-value
  "Convert given value to decimal using given token's base"
  [value token]
  (let [token-details (token-data/token-info token)
        token-base (:base token-details)]
    (assert (> token-base 0))
    (-> value
        (/ (Math/pow 10 token-base)))))


(defn token-balance-in-bounty
  "Query (internal) ERC20 token balance from bounty contract for given token TLA"
  [bounty-addr token]
  (let [bounty-contract (load-bounty-contract bounty-addr)
        token-address (get-token-address token)
        token-addr-web3j (Address. token-address)]
    (-> bounty-contract
        (.tokenBalances token-addr-web3j)
        .get
        .getValue
        (convert-token-value token))))

(defn token-balance
  "Query balance of given ERC20 token TLA for given address from ERC20 contract"
  [bounty-addr token]
  (let [token-address (get-token-address token)]

    (-> (eth/call token-address
                  (:balance-of method-ids)
                  bounty-addr)
        eth/hex->big-integer
        (convert-token-value token))))


(defn token-balances
  "Get a given bounty contract's token balances. Assumes contract's internal balances have been updated"
  [bounty-addr]
  (let [bounty-contract (load-bounty-contract bounty-addr)
        token-addresses (-> bounty-contract
                            (.getTokenList)
                            .get)]
    (if token-addresses
      (let [addrs (map str
                       (.getValue token-addresses))]
        (into {}
              (map (fn [addr] (let [tla (first (token-data/token-info-by-addr addr))]
                               (assert tla)
                               [tla (token-balance bounty-addr tla)])) addrs)))
      {})))
