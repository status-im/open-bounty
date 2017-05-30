(ns commiteth.routes.home
  (:require [commiteth.layout :as layout]
            [commiteth.github.core :as github]
            [compojure.core :refer [defroutes GET]]
            [ring.util.response :refer [redirect]]
            [ring.util.http-response :refer [ok header]]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]))

(defonce ^:const version (System/getProperty "commiteth.version"))
(defonce ^:const on-testnet? (System/getProperty "commiteth.onTestnet"))

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
                              :on-testnet? on-testnet?}))

(defroutes home-routes
  (GET "/" {{user :identity} :session}
       (home-page user))
  (GET "/logout" {session :session}
       (assoc (redirect "/")
              :session nil)))
