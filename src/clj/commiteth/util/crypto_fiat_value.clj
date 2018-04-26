(ns commiteth.util.crypto-fiat-value
  (:require [mount.core :as mount]
            [clj-time.core :as t]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [commiteth.config :refer [env]]
            [commiteth.util.util :refer [json-api-request]]))


(defn- fiat-api-provider []
  (env :fiat-api-provider :coinmarketcap))

(defn- get-token-usd-price-cryptonator
  "Get current USD value for a token using cryptonator API"
  [tla]
  (log/infof "%s: Getting price-data from Cryptonator" tla)
  (let [token (subs (str tla) 1)
        url (format "https://api.cryptonator.com/api/ticker/%s-usd" token)
        m (json-api-request url)]
    (-> (get-in m ["ticker" "price"])
        (edn/read-string))))

(defn- make-tla-to-id-mapping
  "Coinmarketcap API uses it's own IDs for tokens instead of TLAs"
  []
  (->> (json-api-request "https://api.coinmarketcap.com/v1/ticker/?limit=0")
       (map (fn [x] [(keyword (get x "symbol")) (get x "id")]))
       (into {})))

(defn- get-token-usd-price-coinmarketcap
  "Get current USD value for a token using coinmarketcap API"
  [tla token-id]
  {:pre [(some? token-id)]}
  (log/infof "%s: Getting price-data from CoinMarketCap (token-id %s)" tla token-id)
  (-> (json-api-request (format "https://api.coinmarketcap.com/v1/ticker/%s" token-id))
      (first)
      (get "price_usd")
      (edn/read-string)))

(defrecord PriceInfo [tla usd date])

(defn recent? [price-info]
  (t/after? (:date price-info) (t/minus (t/now) (t/minutes 5))))

(defprotocol IFiatCryptoConverter
  (start [_])
  (convert-usd [_ tla amount]))

(defrecord FiatCryptoConverter [provider state]
  IFiatCryptoConverter
  (start [_]
    (when (= :coinmarketcap provider)
      (swap! state assoc :tla->id (make-tla-to-id-mapping))))
  (convert-usd [_ tla amount]
    (if-let [recent-price-info (and (some-> (get-in @state [:price-info tla]) recent?)
                                    ;; return actual price info
                                    (get-in @state [:price-info tla]))]
      (* (:usd recent-price-info) amount)
      ;; if we don't have price-info we need to fetch & store it
      (let [price (case provider
                    :coinmarketcap (get-token-usd-price-coinmarketcap
                                    tla
                                    (get-in @state [:tla->id tla]))
                    :cryptonator (get-token-usd-price-cryptonator tla))]
        (swap! state assoc-in [:price-info tla] (->PriceInfo tla price (t/now)))
        (* price amount)))))

(mount/defstate
  fiat-converter
  :start (let [provider (fiat-api-provider)]
           (log/infof "Starting FiatCryptoConverter %s" provider)
           (doto (->FiatCryptoConverter provider (atom {}))
             (start)))
  :stop  (log/info "Stopping FiatCryptoConverter"))

;; public api ------------------------------------------------------------------

(defn bounty-usd-value
  "Get current USD value for the crypto-amounts passed as argument.
  Example: {:ETH 123, :SNT 456}"
  [crypto-amounts]
  (->> crypto-amounts
       (map (fn [[tla value]]
              (convert-usd fiat-converter tla value)))
       (reduce + 0)))


(comment
  (fiat-api-provider)

  (def fc (->FiatCryptoConverter (fiat-api-provider) (atom {})))
  (start fc)

  (convert-usd fc :SNT 400)

  (bounty-usd-value {:ETH 2 :ANT 2 :SNT 5})

  (mount/start)

  )
