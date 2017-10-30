(ns commiteth.routes.home
  (:require [commiteth.layout :as layout]
            [commiteth.github.core :as github]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :refer [ok header found]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [commiteth.config :refer [env]]))

(defonce ^:const version (System/getProperty "commiteth.version"))

(defn home-page [{user-id :id
                  login :login
                  token :token
                  admin-token :admin-token}]
  (layout/render "home.html" {:user-id user-id
                              :login login
                              :token token
                              :admin-token admin-token
                              :authorize-url (github/signup-authorize-url)
                              :authorize-url-admin (github/admin-authorize-url)
                              :commiteth-version version
                              :on-testnet? (env :on-testnet)}))

(defn landing-page []
  (layout/render "index.html"))

(defroutes home-routes
  (GET "/app" {{user :identity} :session}
       (home-page user))
  (GET "/" {session :session}
       (landing-page))
  (GET "/logout" {session :session}
       (assoc (found (str (env :server-address) "/"))
              :session nil)))
