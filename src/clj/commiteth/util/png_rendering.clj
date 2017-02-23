(ns commiteth.util.png-rendering
  (:require [commiteth.layout :refer [render]]
            [commiteth.config :refer [env]]
            [commiteth.db.comment-images :as db]
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
            :size [255 255])))


(defn gen-comment-image [address balance issue-url]
  (let [qr-image  (-> (image->base64 (generate-qr-image address))
                      (String. "ISO-8859-1"))
        html (:body (render "bounty.html"
                            {:qr-image  qr-image
                             :balance   balance
                             :address   address
                             :issue-url issue-url}))
        command (env :html2png-command "wkhtmltoimage")
        {out :out err :err exit :exit}
        (sh command "-f" "png" "--quality" "80" "--width" "1336" "-" "-"
            :out-enc :bytes :in html)]
    (if (= 0 exit)
      out
      (do (log/error "Failed to generate PNG file" err exit out)
          nil))))


(defn export-comment-image
  "Retrieve image PNG from DB and write to file"
  [issue-id filename]
  (with-open [w (io/output-stream filename)]
    (.write w (:png_data (db/get-image-data issue-id)))))
