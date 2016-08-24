(ns commiteth.routes.redirect
  (:require [clojure.walk :refer [keywordize-keys]]
            [compojure.core :refer [defroutes GET]]
            [ring.util.codec :as codec]
            [commiteth.github.core :as github]
            [commiteth.db.users :as users]
            [commiteth.layout :refer [error-page]]
            [ring.util.http-response :refer [content-type ok]]
            [ring.util.response :as response]
            [commiteth.layout :refer [render]]
            [cheshire.core :refer [generate-string]]))

(defn- get-or-create-user
  [token]
  (let [user (github/get-user token)
        {email :email
         name  :name
         login :login} user]
    (or
      (users/update-user-token login token)
      (users/create-user login name email token))))

(defroutes redirect-routes
  (GET "/callback" [code state]
    (let [resp         (github/post-for-token code state)
          body         (keywordize-keys (codec/form-decode (:body resp)))
          access-token (:access_token body)]
      (if-let [error (:error body)]
        (:body
          (error-page {:status 401
                       :title  error}))
        (let [user (get-or-create-user access-token)]
          (-> (response/redirect "/")
            (assoc :session {:identity user})))))))
