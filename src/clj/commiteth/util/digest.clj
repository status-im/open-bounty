(ns commiteth.util.digest
  (:import [javax.crypto])
  (:require [clojure.string :as str]))

;; credit: https://gist.github.com/visibletrap/4571244
(defn hex-hmac-sha1 [key input]
  (let [secret (javax.crypto.spec.SecretKeySpec. (.getBytes key "UTF-8") "HmacSHA1")
        hmac-sha1 (doto (javax.crypto.Mac/getInstance "HmacSHA1") (.init secret))
        bytes (.doFinal hmac-sha1 (.getBytes input "UTF-8"))]
    (str/join (map (partial format "%02x") bytes))))
