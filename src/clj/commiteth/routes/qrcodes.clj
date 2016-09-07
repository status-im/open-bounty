(ns commiteth.routes.qrcodes
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [clj.qrgen :as qr]))

(defn generate-qr-code
  [address]
  (qr/as-input-stream
    (qr/from (str "ethereum:" address) :size [256 256])))

(defapi qr-routes
  (context "/qr.png" []
    (GET "/" {{address :address} :params}
      (ok (generate-qr-code address)))))
