(ns commiteth.util.svg-rendering
  (:require [clojure.tools.logging :as log]
            [net.cgrand.enlive-html :as enlive]
            [dali
             [layout :as dali-layout]
             [syntax :as dali-syntax]
             [io     :as dali-io]]
            [dali.layout.stack]
            [clojure.java.io :as io]
            [clojure.data.codec.base64 :as base64])
  (:import [io.nayuki.fastqrcodegen QrCode QrCode$Ecc]))

(def ^:private roboto-font-css
  (let [in (io/input-stream "resources/fonts/Roboto-Light-Stripped.ttf")
        baos (java.io.ByteArrayOutputStream.)]
    (base64/encoding-transfer in baos)
    (clojure.string/join
     "\n"
     ["@font-face {"
      "font-family: 'Roboto';"
      (str "src: url('data:application/x-font-ttf;base64,"
           (.toString baos "UTF-8")
           "');")
      "}"])))

(def ^:private status-logo
  (-> (slurp "resources/images/status_logo_blue.svg")
      (.getBytes "UTF-8")
      java.io.ByteArrayInputStream.
      enlive/xml-resource
      dali-io/extract-svg-content
      (get-in [:content nil])
      dali-io/enlive->hiccup))

(defn- render-svg-string [doc]
  (-> doc
      dali-syntax/dali->ixml
      dali-layout/resolve-layout
      dali-syntax/ixml->xml
      dali-io/xml->svg-document-string))

(defn generate-qr-image
  [address]
  (-> (QrCode/encodeText (str "ethereum:" address) QrCode$Ecc/LOW)
      (.toSvgString 0)
      (.getBytes "UTF-8")
      (java.io.ByteArrayInputStream.)))

(defn image-height [tokens]
  (let [n-tokens (count (keys tokens))]
    (+ 355 (if (< n-tokens 2) 0
               (* 32 (- n-tokens 1))))))

(defn gen-comment-image [address balance-eth tokens issue-url]
  (render-svg-string
   [:dali/page
    [:defs (dali-syntax/css roboto-font-css)]
    [:rect
     {:stroke "#e7e7e7ff"
      :stroke-width 1
      :fill "white"}
     [10 10] [1336 355] 5]
    [:g {:transform "matrix(0.27 0 0 0.27 32 32)"}
     status-logo]
    [:text {:font-family "Roboto"
            :font-size 18
            :stroke :none
            :fill "#a8aab1ff"
            :x 32 :y 170}
     issue-url]
    [:dali/stack
     {:position [34 190]
      :anchor :left
      :direction :right
      :gap 30}
     [:text {:font-family "Roboto"
             :font-size 48
             :stroke :none
             :fill "#a8aab1"}
      "ETH"]
     [:text {:font-family "Roboto"
             :font-size 48
             :stroke :none
             :fill "#343434ff"}
      balance-eth]]
    [:g {:transform "matrix(9 0 0 9 980 56)"}
     (-> (generate-qr-image address)
         enlive/xml-resource
         dali-io/extract-svg-content
         (get-in [:content nil])
         dali-io/enlive->hiccup)]]))

(defn test-svg []
  (spit "dali.svg" (gen-comment-image
                    "0xde940f7d50f286319a989e72b20f254a6b455ab0"
                    "12.2"
                    {:SNT 250.2000000343
                     :GNO 100}
                    "http://asfasdf")))
