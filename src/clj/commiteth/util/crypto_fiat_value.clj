(ns commiteth.util.crypto-fiat-value
  (:require [clj-http.client :as http]
            [mount.core :as mount]
            [clojure.tools.logging :as log]
            [commiteth.config :refer [env]]
            [clojure.data.json :as json]))


(defn fiat-api-provider []
  (env :fiat-api-provider :coinmarketcap))

(defn json-api-request [url]
  (->> (http/get url)
       (:body)
       (json/read-str)))


(defn get-token-usd-price-cryptonator
  "Get current USD value for a token using cryptonator API"
  [tla]
  (let [token (subs (str tla) 1)
        url (str "https://api.cryptonator.com/api/ticker/"
                 token
                 "-usd")
        m (json-api-request url)]
    (-> (get-in m ["ticker" "price"])
        (read-string))))


(def tla-to-id-mapping (atom {}))

(defn make-tla-to-id-mapping
  "Coinmarketcap API uses it's own IDs for tokens instead of TLAs"
  []
  (let [data (json-api-request "https://api.coinmarketcap.com/v1/ticker/?limit=0")]
    (into {} (map
              (fn [x] [(keyword (get x "symbol")) (get x "id")])
              data))))

(defn get-token-usd-price-coinmarketcap
  "Get current USD value for a token using coinmarketcap API"
  [tla]
  (let [token-id (get @tla-to-id-mapping tla)
        url (format "https://api.coinmarketcap.com/v1/ticker/%s" token-id)
        data (json-api-request url)]
    (-> (first data)
        (get "price_usd")
        (read-string))))

(defn- get-price-fn []
  (let [fns {:cryptonator get-token-usd-price-cryptonator
                      :coinmarketcap get-token-usd-price-coinmarketcap}]
    (get fns (fiat-api-provider))))

(defn bounty-usd-value
  "Get current USD value of a bounty. bounty is a map of token-tla (keyword) to value"
  [bounty]
  (let [get-token-usd-price (get-price-fn)]
    (reduce + (map (fn [[tla value]]
                     (let [usd-price (get-token-usd-price tla)]
                       (* usd-price value)))
                   bounty))))


(mount/defstate
  crypto-fiat-util
  :start
  (do
    (reset! tla-to-id-mapping (make-tla-to-id-mapping))
    (log/info "crypto-fiat-util started"))
  :stop
  (log/info "crypto-fiat-util stopped"))
