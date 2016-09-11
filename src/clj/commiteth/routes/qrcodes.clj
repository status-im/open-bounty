(ns commiteth.routes.qrcodes
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [commiteth.db.bounties :as bounties]
            [commiteth.layout :as layout]
            [commiteth.util.images :refer :all]
            [clj.qrgen :as qr]
            [commiteth.eth.core :as eth])
  (:import [javax.imageio ImageIO]
           [java.io InputStream]))

(defn ^InputStream generate-qr-code
  [address]
  (qr/as-input-stream
    (qr/from (str "ethereum:" address) :size [256 256])))

(defn generate-html
  [address balance issue-url]
  (:body (layout/render "bounty.html" {:balance   balance
                                       :address   address
                                       :issue-url issue-url})))

(defn generate-image
  [address balance issue-url width height]
  (let [qr-code-image (ImageIO/read (generate-qr-code address))
        comment-image (html->image (generate-html address balance issue-url) width height)]
    (combine-images qr-code-image comment-image)))

(defapi qr-routes
  (context "/qr" []
    (GET "/:user/:repo/bounty/:issue{[0-9]{1,9}}/qr.png" [user repo issue]
      (let [{address      :contract_address
             login        :login
             repo         :repo
             issue-number :issue_number} (bounties/get-bounty-address user repo (Integer/parseInt issue))
            balance   (eth/get-balance-eth address 8)
            issue-url (str login "/" repo "/issues/" issue-number)]
        (if address
          (ok (generate-image address balance issue-url 768 256))
          (bad-request))))))
