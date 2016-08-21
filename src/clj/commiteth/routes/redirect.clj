(ns commiteth.routes.redirect
  (:require [clojure.walk :refer [keywordize-keys]]
            [compojure.core :refer [defroutes GET]]
            [ring.util.codec :as codec]
            [commiteth.github.core :as github]))

(defroutes redirect-routes
  (GET "/callback" [code state]
    (let [resp         (github/post-for-token code state)
          body         (keywordize-keys (codec/form-decode (:body resp)))
          access-token (:access_token body)]
      (if-let [error (:error body)]
        (str "Error: " error)
        (str "Your access token: " access-token "<br/>"
          "Your repositories: " (reduce str (github/list-repos access-token)))))))
