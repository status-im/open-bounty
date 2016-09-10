(ns commiteth.routes.qrcodes
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [commiteth.db.bounties :as bounties]
            [clj.qrgen :as qr]))

(defn generate-qr-code
  [address]
  (qr/as-input-stream
    (qr/from (str "ethereum:" address) :size [256 256])))

(defapi qr-routes
  (context "/qr" []
    (GET "/:user/:repo/bounty/:issue{[0-9]{1,9}}/qr.png" [user repo issue]
      (let [address (bounties/get-bounty-address user repo (Integer/parseInt issue))]
        (if address
          (ok (generate-qr-code address))
          (bad-request))))))
