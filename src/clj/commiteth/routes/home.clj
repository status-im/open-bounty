(ns commiteth.routes.home
  (:require [commiteth.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page [{login :login token :token}]
  (layout/render "home.html" {:login login :token token}))

(defroutes home-routes
  (GET "/" {{identity :identity} :session}
    (home-page identity))
  (GET "/logout" [] (home-page nil))
  (GET "/docs" [] (-> (response/ok (-> "docs/docs.md" io/resource slurp))
                    (response/header "Content-Type" "text/plain; charset=utf-8"))))
