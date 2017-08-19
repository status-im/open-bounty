(ns commiteth.eth.token-registry
  (:require [commiteth.eth.core :as eth]
            [commiteth.config :refer [env]])
  (:import [org.web3j
            abi.datatypes.generated.Uint256
            protocol.Web3j
            protocol.http.HttpService
            crypto.Credentials
            crypto.WalletUtils]
           commiteth.contracts.TokenReg))

(defonce PARITY-TOKENREG-ADDR "0x5f0281910af44bfb5fc7e86a404d0304b0e042f1")
(defonce GAS_PRICE (eth/gas-price))
(defonce GAS_LIMIT (BigInteger/valueOf 21000))

(defn wallet-file-path []
  (env :eth-wallet-file))

(defn wallet-password []
  (env :eth-password))

(defn creds []
  (WalletUtils/loadCredentials
   (wallet-password)
   (wallet-file-path)))

(defn create-web3j []
  (Web3j/build (HttpService. (eth/eth-rpc-url))))

(defn load-tokenreg-contract []
  (TokenReg/load PARITY-TOKENREG-ADDR
                 (create-web3j)
                 (creds)
                 GAS_PRICE
                 GAS_LIMIT))


(defn load-parity-tokenreg-data
  "Construct a mapping of ERC20 token mnemonic -> token data (name, address, digits, owner) from data
  in Parity's mainnet token registry contract."
  []
  (let [contract (load-tokenreg-contract)]
    (assert (.isValid contract))
    (let [token-count (-> contract .tokenCount .get .getValue)]
      (println "token-count" token-count)
      (into {}
            (map (fn [[addr mnemonic digits name owner]]
                   [(-> mnemonic .toString (keyword))
                    {:name (.toString name)
                     :digits (.getValue digits)
                     :address (.toString addr)
                     :owner (.toString owner)}])
                 (for [i (range token-count)]
                   (-> (.token contract
                               (org.web3j.abi.datatypes.generated.Uint256. i))
                       .get)))))))
