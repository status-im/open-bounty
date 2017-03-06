(ns commiteth.util.png-rendering
  (:require [commiteth.layout :refer [render]]
            [commiteth.config :refer [env]]
            [commiteth.github.core :as github]
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
      (do
        (log/debug "PNG generated succesfully" out err exit html)
        out)
      (do (log/error "Failed to generate PNG file" err exit out html)
          nil))))


(defn export-comment-image
  "Retrieve image PNG from DB and write to file"
  [owner repo issue-number filename]
  (let [{owner :owner
         repo :repo
         issue-id :issue_id
         balance :balance} (db-bounties/get-bounty owner repo issue-number)
        hash (github/github-comment-hash
              owner
              repo
              issue-number
              balance)]
    (with-open [w (io/output-stream filename)]
      (.write w (:png_data (db/get-image-data issue-id hash))))))
