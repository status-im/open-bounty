(ns commiteth.util.crypto-fiat-value
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]))


(defn get-token-usd-price [token]
  (let [url (str "https://api.cryptonator.com/api/ticker/"
                 token
                 "-usd")
        m (->> (http/get url)
               (:body)
               (json/read-str))]
    (-> (get-in m ["ticker" "price"])
        (read-string))))
