(ns commiteth.routes.home
  (:require [commiteth.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.response :refer [redirect]]
            [ring.util.http-response :refer [ok header]]
            [clojure.java.io :as io]))

(defn home-page [{user-id :id login :login token :token}]
  (layout/render "home.html" {:userId user-id :login login :token token}))

(defroutes home-routes
  (GET "/" {{identity :identity} :session}
       (home-page identity))
  (GET "/logout" {session :session}
       (-> (redirect "/")
           (assoc :session (dissoc session :identity))))
  (GET "/docs" [] (-> (ok (-> "docs/docs.md" io/resource slurp))
                      (header "Content-Type" "text/plain; charset=utf-8"))))
