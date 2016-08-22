(ns commiteth.routes.redirect
  (:require [clojure.walk :refer [keywordize-keys]]
            [compojure.core :refer [defroutes GET]]
            [ring.util.codec :as codec]
            [commiteth.github.core :as github]
            [commiteth.db.users :as users]
            [commiteth.layout :refer [error-page]]
            [ring.util.response :as response]))

(defn- create-user
  [token]
  (let [user (github/get-user token)
        {email :email
         name  :name
         login :login} user]
    (if-not (users/exists? login)
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
        (do
          (create-user access-token)
          (response/redirect "/"))))))
