(ns commiteth.util.png-rendering
  (:require [commiteth.layout :refer [render]]
            [commiteth.config :refer [env]]
            [commiteth.db.comment-images :as db]
            [commiteth.db.bounties :as db-bounties]
            [clj.qrgen :as qr]
            [clojure.data.codec.base64 :as b64]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io])
  (:use [clojure.java.shell :only [sh]])
  (:import [java.io InputStream]))


(defn image->base64 [input-stream]
  (let [baos (java.io.ByteArrayOutputStream.)]
    (b64/encoding-transfer input-stream baos)
    (.toByteArray baos)))


(defn ^InputStream generate-qr-image
  [address]
  (qr/as-input-stream
   (qr/from (str "ethereum:" address)
            :size [384 384])))


(defn token-map->list [tokens]
  (let [fmt-balance (fn [x] (format "%.02f" (double x)))]
    (mapv (fn [[tla balance]] {:tla (subs (str tla) 1)
                              :balance (fmt-balance balance)})
          tokens)))

(defn image-height [tokens]
  (let [n-tokens (count (keys tokens))]
    (+ 355 (if (< n-tokens 2) 0
               (* 32 (- n-tokens 1))))))

(defn gen-comment-image [address balance-eth tokens issue-url]
  (let [qr-image  (String. (image->base64 (generate-qr-image address))
                           "ISO-8859-1")
        html (:body (render "bounty.html"
                            {:qr-image qr-image
                             :eth-balance balance-eth
                             :tokens (token-map->list tokens)
                             :image-height (image-height tokens)
                             :address address
                             :issue-url issue-url}))
        command (env :html2png-command "wkhtmltoimage")
        {out :out err :err exit :exit}
        (sh command "-f" "png" "--quality" "80" "--width" "1336" "-" "-"
            :out-enc :bytes :in html)]
    (if (zero? exit)
      (do
        (log/debug "PNG generated succesfully" out err exit html)
        out)
      (do (log/error "Failed to generate PNG file" err exit out html)
          nil))))



(comment
  (with-open [w (io/output-stream "foo.png")]
    (.write w (gen-comment-image "0xf00barbeeff00barbeeff00barbeeff00barbeef" "12.2" {:SNT 250.2000000343
                                                                                      :GNO 100
                                                                                      :WTF 3452.42
                                                                                      } "http://github.com/someorg/somerepo/issues/42"))))
