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
            [cheshire.core :refer [generate-string]]
            [clojure.tools.logging :as log]))



(defn- create-user [token user]
  (let [{name    :name
         login   :login
         user-id :id
         avatar-url :avatar_url} user
        email (github/get-user-email token)]
    (users/create-user user-id login name email avatar-url token)))

(defn- get-or-create-user
  [token]
  (let [user (github/get-user token)
        {email   :email
         user-id :id} user]
    (log/debug "get-or-create-user" user)
    (or
      (users/update-user-token user-id token)
      (create-user token user))))

(defroutes redirect-routes
  (GET "/callback" [code state]
    (let [resp         (github/post-for-token code state)
          body         (keywordize-keys (codec/form-decode (:body resp)))
          access-token (:access_token body)]
      (if (:error body)
        ;; Why does Mist browser sends two redirects at the same time? The latter results in 401 error.
        (response/redirect "/")
        (let [user (get-or-create-user access-token)]
          (-> (response/redirect "/")
            (assoc :session {:identity user})))))))
