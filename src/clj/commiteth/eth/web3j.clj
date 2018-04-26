(ns commiteth.eth.web3j
  (:require [commiteth.config :refer [env]]
            [clojure.tools.logging :as log])
  (:import [org.web3j
            protocol.Web3j
            protocol.http.HttpService
            protocol.core.methods.request.RawTransaction
            utils.Numeric
            crypto.TransactionEncoder
            crypto.WalletUtils]))

(defn eth-rpc-url [] (env :eth-rpc-url "http://localhost:8545"))

(def web3j-obj
  (delay (Web3j/build (HttpService. (eth-rpc-url)))))

(defn wallet-file-path []
  (env :eth-wallet-file))

(defn wallet-password []
  (env :eth-password))

(def creds-obj
  (delay
    (let [password  (wallet-password)
          file-path (wallet-file-path)]
      (if (and password file-path)
        (WalletUtils/loadCredentials
          password
          file-path)
        (throw (ex-info "Make sure you provided proper credentials in appropriate resources/config.edn"
                        {:password password :file-path file-path}))))))

(defn get-signed-tx [gas-price gas-limit to data nonce]
  "Create a sign a raw transaction.
  'From' argument is not needed as it's already
  encoded in credentials.
  See https://web3j.readthedocs.io/en/latest/transactions.html#offline-transaction-signing"
  (log/infof "Signing TX: nonce: %s, gas-price: %s, gas-limit: %s, data: %s"
             nonce gas-price gas-limit data)
  (-> (RawTransaction/createTransaction nonce gas-price gas-limit to data)
      (TransactionEncoder/signMessage @creds-obj)
      (Numeric/toHexString)))



