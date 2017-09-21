(ns commiteth.eth.token-data
  (:require [commiteth.eth.token-registry :as token-reg]
            [commiteth.config :refer [env]]
            [mount.core :as mount]
            [clojure.tools.logging :as log]))

(def token-data-atom (atom {}))

(defn update-data []
  (let [test-data (env :testnet-token-data)
        token-data
        (if (and (env :on-testnet true)
                 test-data)
          test-data
          (token-reg/load-parity-tokenreg-data))]
    (reset! token-data-atom token-data)))

(mount/defstate
  token-data
  :start
  (do
    (update-data)
    (log/info "token-data started"))
  :stop
  (log/info "token-data stopped"))

(defn as-map []
  @token-data-atom)

(defn token-info [tla]
  (get @token-data-atom (keyword tla)))

(defn token-info-by-addr [addr]
  (let [tokens-data (as-map)]
    (first (filter (fn [[tla data]]
                     (= (:address data) addr))
                   tokens-data))))
