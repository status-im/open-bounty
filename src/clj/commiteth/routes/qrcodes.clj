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
                (when-let [{address      :contract_address
                            login        :login
                            repo         :repo
                            issue-id     :issue_id
                            balance      :balance}
                           (bounties/get-bounty owner
                                                repo
                                                (Integer/parseInt issue))]
                  (log/debug "address:" address)
                  (if (and address
                           (= hash (github/github-comment-hash owner repo issue)))
                    (let [{png-data :png_data} (comment-images/get-image-data issue-id)
                          image-byte-stream  (ByteArrayInputStream. png-data)
                          response  {:status 200
                                     :content-type "image/png"
                                     :headers {"cache-control" "no-cache"}
                                     :body image-byte-stream}]
                      (log/debug "response" response)
                      response)
                    (bad-request))))))
