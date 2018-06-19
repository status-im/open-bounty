(ns commiteth.routes.qrcodes
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [commiteth.db.bounties :as bounties]
            [commiteth.db.comment-images :as comment-images]
            [commiteth.github.core :as github]
            [clojure.tools.logging :as log])
  (:import [java.io ByteArrayInputStream]))

;; TODO(endenwer) qr is handler as static image now. This route is used for old comments.
;; Remove it when it is not necessary anymore.
(defapi qr-routes
  (context "/qr" []
           (GET "/:owner/:repo/bounty/:issue{[0-9]{1,9}}/:hash/qr.png" [owner repo issue hash]
                (log/debug "qr PNG GET" owner repo issue hash)
                (if-let [{:keys [contract-address repo issue-id balance-eth]}
                         (bounties/get-bounty owner
                                              repo
                                              (Integer/parseInt issue))]
                  (do
                    (log/debug "address:" contract-address)
                    (log/debug owner repo issue balance-eth)
                    (log/debug hash)
                    (if contract-address
                      (if-let [{:keys [png-data]}
                               (comment-images/get-image-data
                                 issue-id hash)]
                        (do (log/debug "PNG found")
                            {:status 200
                             :content-type "image/png"
                             :headers {"cache-control" "no-cache"}
                             :body (ByteArrayInputStream. png-data)})
                        (log/debug "PNG not found"))))
                  (bad-request)))))
