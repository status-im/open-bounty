(ns commiteth.routes.redirect
  (:require [clojure.walk :refer [keywordize-keys]]
            [compojure.core :refer [defroutes GET]]
            [ring.util.codec :as codec]
            [commiteth.github.core :as github]
            [commiteth.db.users :as users]
            [commiteth.config :refer [env]]
            [ring.util.http-response :refer [content-type ok found]]
            [commiteth.util.hubspot :as hubspot]
            [cheshire.core :refer [generate-string]]
            [clojure.tools.logging :as log]
            [clojure.string :as str]))



(defn- create-user [token user]
  (let [{name    :name
         login   :login
         user-id :id
         avatar-url :avatar_url} user
        email (github/get-user-email token)]
    (users/create-user user-id login name email avatar-url)))

(defn- get-or-create-user
  [token]
  (let [user (github/get-user token)
        {email   :email
         user-id :id} user]
    (log/debug "get-or-create-user" user)
    (or
      (users/get-user user-id)
      (create-user token user))))

(defroutes redirect-routes
  (GET "/callback" [code state]
    (let [resp         (github/post-for-token code state)
          body         (keywordize-keys (codec/form-decode (:body resp)))
          scope        (:scope body)
          access-token (:access_token body)]
      (log/debug "github sign-in callback, response body:" body)
      (if (:error body)
        ;; Why does Mist browser sends two redirects at the same time? The latter results in 401 error.
        (found (str (env :server-address) "/app"))
        (let [admin-token? (str/includes? scope "repo")
              token-key (if admin-token? :admin-token :token)
              gh-user (github/get-user access-token)
              new-user? (nil? (users/get-user (:id gh-user 0)))
              user (assoc (get-or-create-user access-token)
                          token-key access-token)]
          (when new-user?
            (hubspot/create-hubspot-contact (:email user)
                                              (:name user "")
                                              (:login user)))
          (assoc (found (str (env :server-address) "/app"))
                 :session {:identity user}))))))
