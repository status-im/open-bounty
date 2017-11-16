(ns macchiato-web3-example.middleware
  (:require
    [macchiato.middleware.defaults :as defaults]))

(defn wrap-defaults [handler]
  (defaults/wrap-defaults handler defaults/site-defaults))


