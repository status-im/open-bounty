(ns commiteth.eth.web3j
    (:require [commiteth.config :refer [env]])
  (:import [org.web3j
            protocol.Web3j
            protocol.http.HttpService
            protocol.core.DefaultBlockParameterName
            protocol.core.methods.response.EthGetTransactionCount
            protocol.core.methods.request.RawTransaction
            utils.Numeric
            crypto.Credentials
            crypto.TransactionEncoder
            crypto.WalletUtils]))


(defn wallet-file-path []
  (env :eth-wallet-file))

(defn wallet-password []
  (env :eth-password))

(defn creds []
  (let [password  (wallet-password)
        file-path (wallet-file-path)]
    (if (and password file-path)
      (WalletUtils/loadCredentials
       password
       file-path)
      (throw (ex-info "Make sure you provided proper credentials in appropriate resources/config.edn"
                      {:password password :file-path file-path})))))

(defn create-web3j []
  (Web3j/build (HttpService. (env :eth-rpc-url "http://localhost:8545"))))

(defn get-signed-tx [gas-price gas-limit to data]
  "Create a sign a raw transaction.
   'From' argument is not needed as it's already
   encoded in credentials.
   See https://web3j.readthedocs.io/en/latest/transactions.html#offline-transaction-signing"
  (let [web3j (create-web3j)
        nonce (.. (.ethGetTransactionCount web3j 
                                           (env :eth-account) 
                                           DefaultBlockParameterName/LATEST)
                  sendAsync
                  get
                  getTransactionCount)
        tx (RawTransaction/createTransaction
             nonce
             gas-price
             gas-limit
             to
             data)
        signed (TransactionEncoder/signMessage tx (creds))
        hex-string (Numeric/toHexString signed)]
    hex-string))
