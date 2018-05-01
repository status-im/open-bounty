(ns commiteth.eth.multisig-wallet
  (:require [commiteth.eth.core :as eth]
            [commiteth.config :refer [env]]
            [clojure.tools.logging :as log]
            [taoensso.tufte :as tufte :refer (defnp p profiled profile)]
            [commiteth.eth.token-data :as token-data])
  (:import [org.web3j
            abi.datatypes.Address
            abi.datatypes.generated.Uint256]
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
              :balance-of "balanceOf(address)"
              :transfer "transfer(address,uint256)"})))

(def topics
  {:factory-create (eth/event-sig->topic-id "Create(address,address)")
   :submission (eth/event-sig->topic-id "Submission(uint256)")
   :confirmation (eth/event-sig->topic-id "Confirmation(address,uint256)")})

(defn factory-contract-addr []
  (env :contract-factory-addr))

(defn tokenreg-base-format
  ;; status tokenreg uses eg :base 18, while parity uses :base 1000000000000
  []
  (env :tokenreg-base-format :status))

(defn deploy-multisig
  "Deploy a new multisig contract to the blockchain with commiteth bot
  and given owner as owners.
  `internal-tx-id` is used to identify what issue this multisig is deployed
  for and manage nonces at a later point in time."
  [{:keys [owner internal-tx-id]}]
  {:pre [(string? owner) (string? internal-tx-id)]}
  (eth/execute {:internal-tx-id internal-tx-id
                :from      (eth/eth-account)
                :contract  (factory-contract-addr)
                :method-id (:create method-ids)
                :gas-limit (eth/integer->hex 865000)
                :params    [0x40 0x2 2 (eth/eth-account) owner]}))

(defn find-event-in-tx-receipt [tx-receipt topic-id]
  (let [logs (:logs tx-receipt)
        correct-topic? (fn [topic]
                         (= topic topic-id))
        has-correct-event? #(some correct-topic?
                                  (:topics %))
        log-entry     (first (filter has-correct-event? logs))]
    log-entry))

(defn addr-from-topic [topic]
  (assert (= 66 (count topic)))
  (str "0x" (subs topic 26)))

(defn find-confirmation-tx-id
  [tx-receipt]
  (let [confirmation-event (find-event-in-tx-receipt
                            tx-receipt
                            (:confirmation topics))]
    (log/debug "confirmation-event" confirmation-event)
    (when-let [topics (:topics confirmation-event)]
      (let [ [_ addr-raw tx-id] topics
            address (addr-from-topic addr-raw)]
        (log/info "event: Confirmation(_sender=" address ", _transactionId=" tx-id ")")
        tx-id))))


(defn find-created-multisig-address
  [tx-receipt]
  (let [factory-data (-> (find-event-in-tx-receipt
                          tx-receipt
                          (:factory-create topics))
                         :data)]
    (when factory-data
      (addr-from-topic factory-data))))


(defn send-all
  [{:keys [contract payout-address internal-tx-id]}]
  {:pre [(string? contract) (string? payout-address) (string? internal-tx-id)]}
  (log/debug "multisig/send-all " contract payout-address internal-tx-id)
  (let [params (eth/format-call-params
                (:withdraw-everything method-ids)
                payout-address)]
    (eth/execute {:internal-tx-id internal-tx-id
                  :from      (eth/eth-account)
                  :contract  contract
                  :method-id (:submit-transaction method-ids)
                  :gas-limit nil
                  :params    [contract 0 "0x60" "0x24" params]})))


(defn get-token-address [token]
  (let [token-details (token-data/token-info token)]
    (assert token-details)
    (:address token-details)))

(defn watch-token
  [bounty-addr token]
  (log/debug "multisig/watch-token" bounty-addr token)
  (let [token-address (get-token-address token)]
    (assert token-address)
    (eth/execute {:internal-tx-id (str "watch-token-" (System/currentTimeMillis) "-" bounty-addr)
                  :from      (eth/eth-account)
                  :contract  bounty-addr
                  :method-id (:watch method-ids)
                  :gas-limit nil
                  :params    [token-address]})))

(defn load-bounty-contract [addr]
  (MultiSigTokenWallet/load addr
    @eth/web3j-obj
    (eth/creds)
    (eth/gas-price)
    (BigInteger/valueOf 500000)))

(defn convert-token-value
  "Convert given value to decimal using given token's base."
  [value token]
  (let [token-details (token-data/token-info token)
        token-base (:base token-details)
        base (if (= (tokenreg-base-format) :status)
               (Math/pow 10 token-base)
               token-base)]
    (assert (> base 0))
    (double (/ value base))))


(defn token-balance-in-bounty
  "Query (internal) ERC20 token balance from bounty contract for given
  token TLA."
  [bounty-addr token]
  (p :token-balance-in-bounty
     (let [bounty-contract (load-bounty-contract bounty-addr)
        token-address (get-token-address token)
        token-addr-web3j (Address. token-address)]
    (-> bounty-contract
        (.tokenBalances token-addr-web3j)
        .get
        .getValue
        (convert-token-value token)))))

(defn token-balance
  "Query balance of given ERC20 token TLA for given address from ERC20
  contract."
  [bounty-addr token]
  (p :token-balance
     (let [token-address (get-token-address token)]
    (log/debug "token-balance" bounty-addr token token-address)
    (try
      (-> (eth/call token-address
                    (:balance-of method-ids)
                    bounty-addr)
          eth/hex->big-integer
          (convert-token-value token))
      (catch Throwable t
        (log/debug "Failed to query token balance " t)
        0)))))


(defn token-balances
  "Get a given bounty contract's token balances. Assumes contract's
  internal balances have been updated."
  [bounty-addr]
  (p :token-balances
     (let [bounty-contract (p :load-bounty-contract (load-bounty-contract bounty-addr))
        token-addresses (p :getTokenList (-> bounty-contract
                            (.getTokenList)
                            .get))]
    (if token-addresses
      (let [addrs (map str
                       (p :getValue (.getValue token-addresses)))]
        (into {}
              (map (fn [addr] (if-let [info (token-data/token-info-by-addr addr)]
                               (let [tla (first info)]
                                 [tla (token-balance bounty-addr tla)]))) addrs)))
      {}))))

(defn uint256 [x]
  (org.web3j.abi.datatypes.generated.Uint256. x))

(defn is-confirmed?
  "Returns true if given internal transaction is confirmed in bounty
  contract."
  [bounty-addr tx-id]
  (let [bounty-contract (load-bounty-contract bounty-addr)
        tx-id-numeric (read-string tx-id)
        tx-id-web3j (uint256 tx-id-numeric)
        ret (-> bounty-contract
                (.isConfirmed tx-id-web3j)
                .get)]
    (assert ret)
    (.getValue ret)))

(defn execute-tx
  "Execute internal transaction inside contract, identified by
  tx-id. Returns transaction ID. Synchronous, blocks until transaction
  is mined."
  [bounty-addr tx-id]
  (let [bounty-contract (load-bounty-contract bounty-addr)
        tx-id-numeric (read-string tx-id)
        tx-id-web3j (uint256 tx-id-numeric)
        ret (-> bounty-contract
                (.executeTransaction tx-id-web3j)
                .get)]
    (assert ret)
    (.getTransactionHash ret)))
