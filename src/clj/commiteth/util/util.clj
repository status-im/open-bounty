(ns commiteth.util.util
  (:require
   [clj-http.client :as http]
   [clojure.string :as str]
   [clojure.data.json :as json]))


(defn eth-decimal->str [n]
  (format "%.6f" n))

(defn usd-decimal->str [n]
  (format "%.2f" n))

(defn json-api-request [url]
  (->> (http/get url)
       (:body)
       (json/read-str)))

(defmacro to-map [& vars]
  (into {} (map #(vector (keyword %1) %1) vars)))

(defmacro to-db-map [& vars]
  (into {} (map #(vector (keyword (str/replace (name %1) "-" "_")) %1) vars)))
