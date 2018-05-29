(ns commiteth.routes.qrcodes
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [commiteth.db.bounties :as bounties]
            [commiteth.db.comment-images :as comment-images]
            [commiteth.github.core :as github]
            [clojure.tools.logging :as log])
  (:import [java.io ByteArrayInputStream]))


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
                    (log/debug hash (github/github-comment-hash owner repo issue balance-eth))
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
