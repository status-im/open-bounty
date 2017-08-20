(ns commiteth.eth.web3j
    (:require [commiteth.eth.core :as eth]
            [commiteth.config :refer [env]])
  (:import [org.web3j
            protocol.Web3j
            protocol.http.HttpService
            crypto.Credentials
            crypto.WalletUtils]))


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
