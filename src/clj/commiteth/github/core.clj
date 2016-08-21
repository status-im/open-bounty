(ns commiteth.github.core
  (:require [tentacles.repos :as repos]
            [ring.util.codec :as codec]
            [clj-http.client :as http])
  (:import [java.util UUID]))

(def client-id "caf23d90246fa99ca545")
(def client-secret "e8e7a088e7769c77e9e2d87c47ef81df51080bf3")
(def redirect-uri "http://localhost:3000/callback")
(def allow-signup true)

(defn authorize-url []
  (let [params (codec/form-encode {:client_id    client-id
                                   :redirect_uri redirect-uri
                                   :allow_signup allow-signup
                                   :state        (str (UUID/randomUUID))})]
    (str "https://github.com/login/oauth/authorize" "?" params)))

(defn post-for-token
  [code state]
  (http/post "https://github.com/login/oauth/access_token"
    {:content-type :json
     :form-params  {:client_id     client-id
                    :client_secret client-secret
                    :code          code
                    :redirect_uri  redirect-uri
                    :state         state}}))

(defn list-repos
  [token]
  (repos/repos
    {:oauth-token  token
     :client-id    client-id
     :client-token client-secret}))
