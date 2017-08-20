(ns commiteth.eth.token-data
  (:require [commiteth.eth.token-registry :as token-reg]
            [commiteth.config :refer [env]]
            [mount.core :as mount]
            [clojure.tools.logging :as log]))

(def token-data-atom (atom {}))

(mount/defstate
  token-data
  :start
  (do
    (log/info "token-data started")
    (let [token-data
          (if (env :on-testnet true)
            (if-let [token-data (env :testnet-token-data)]
              token-data
              (token-reg/load-parity-tokenreg-data token-reg/STATUS-RINKEBY-ADDR))

            (token-reg/load-parity-tokenreg-data token-reg/PARITY-MAINNET-ADDR))]
      (reset! token-data-atom token-data)))
  :stop
  (log/info "token-data stopped"))

(defn as-map []
  @token-data-atom)

(defn token-info [tla]
  (get @token-data-atom (keyword tla)))
