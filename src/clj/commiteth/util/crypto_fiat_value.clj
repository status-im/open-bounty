(ns commiteth.util.crypto-fiat-value
  (:require [clj-http.client :as http]
            [clojure.string :as str]
            [clojure.data.json :as json]))


(defn get-token-usd-price
  "Get current USD value for a token using cryptonator API"
  [token]
  (let [url (str "https://api.cryptonator.com/api/ticker/"
                 token
                 "-usd")
        m (->> (http/get url)
               (:body)
               (json/read-str))]
    (-> (get-in m ["ticker" "price"])
        (read-string))))


(defn bounty-usd-value
  "Get current USD value of a bounty. bounty is a map of token-tla (keyword) to value"
  [bounty]
  (reduce + (map (fn [[token value]]
                   (let [tla (subs (str token) 1)
                         usd-price (get-token-usd-price tla)]
                     (* usd-price value)))
                 bounty)))
