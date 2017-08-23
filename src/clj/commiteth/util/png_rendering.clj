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


(defn token-map->list [tokens]
  (mapv (fn [[tla balance]] {:tla (subs (str tla) 1)
                            :balance balance})
        tokens))

(defn image-height [tokens]
  (+ 300 (* 32 (count (keys tokens)))))

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


(defn export-comment-image
  "Retrieve image PNG from DB and write to file"
  [owner repo issue-number filename]
  (let [{owner :owner
         repo :repo
         issue-id :issue_id
         balance-eth :balance_eth} (db-bounties/get-bounty owner repo issue-number)
        hash (github/github-comment-hash
              owner
              repo
              issue-number
              balance-eth)]
    (with-open [w (io/output-stream filename)]
      (.write w (:png_data (db/get-image-data issue-id hash))))))


(comment
  (with-open [w (io/output-stream "foo.png")]
    (.write w (gen-comment-image "0xf00barbeeff00barbeeff00barbeeff00barbeef" "12.2" {:SNT 250 :GNO 100 :WTF 3452.42} "http://github.com/someorg/somerepo/issues/42"))))
