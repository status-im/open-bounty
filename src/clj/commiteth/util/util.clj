(ns commiteth.util.util
  (:require
   [clj-http.client :as http]
   [clojure.data.json :as json]))


(defn eth-decimal->str [n]
  (format "%.6f" n))

(defn usd-decimal->str [n]
  (format "%.2f" n))

(defn json-api-request [url]
  (->> (http/get url)
       (:body)
       (json/read-str)))

(defn contains-all-keys [m ks]
  {:pre [(map? m) [(vector? ks)]]}
  (every?
   #(contains? m %) ks))
