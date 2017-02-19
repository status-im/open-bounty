(ns commiteth.routes.qrcodes
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [commiteth.db.bounties :as bounties]
            [commiteth.layout :as layout]
            [commiteth.util.images :refer :all]
            [clj.qrgen :as qr]
            [commiteth.eth.core :as eth]
            [commiteth.github.core :as github]
            [clojure.tools.logging :as log])
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
        comment-image (html->image
                       (generate-html address balance issue-url) width height)]
    (combine-images qr-code-image comment-image)))


(defapi qr-routes
  (context "/qr" []
           (GET "/:owner/:repo/bounty/:issue{[0-9]{1,9}}/:hash/qr.png" [owner repo issue hash]
                (log/debug "qr PNG GET" owner repo issue hash (bounties/get-bounty-address owner
                                                        repo
                                                        (Integer/parseInt issue)))
                (when-let [{address      :contract_address
                            login        :login
                            repo         :repo
                            issue-number :issue_number}
                           (bounties/get-bounty-address owner
                                                        repo
                                                        (Integer/parseInt issue))]
                  (when address
                    (let [balance (eth/get-balance-eth address 8)]
                      (log/debug "address:" address "balance:" balance)
                      (if (and address
                               (= hash (github/github-comment-hash owner repo issue)))
                        (let [issue-url (str login "/" repo "/issues/" issue-number)
                              image-url (generate-image address balance issue-url 768 256)
                              response  (assoc-in (ok image-url)
                                                  [:headers "cache-control"] "no-cache")]
                          (log/debug "balance:" address "response" response)
                          response)
                        (bad-request))))))))
