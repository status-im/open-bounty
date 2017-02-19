(ns commiteth.util.digest
  (:import [javax.crypto]))

;; credit: https://gist.github.com/visibletrap/4571244
(defn hex-hmac-sha1 [key input]
  (let [secret (javax.crypto.spec.SecretKeySpec. (. key getBytes "UTF-8") "HmacSHA1")
        hmac-sha1 (doto (javax.crypto.Mac/getInstance "HmacSHA1") (.init secret))
        bytes (. hmac-sha1 doFinal (. input getBytes "UTF-8"))]
    (apply str (map (partial format "%02x") bytes))))
