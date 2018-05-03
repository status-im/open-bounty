(ns commiteth.middleware
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth.backends.session :refer [session-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [clojure.tools.logging :as log]
            [commiteth.db.core :refer [*db*]]
            [commiteth.env :refer [defaults]]
            [commiteth.config :refer [env]]
            [commiteth.layout :refer [error-page]]
            [jdbc-ring-session.cleaner :refer [start-cleaner stop-cleaner]]
            [jdbc-ring-session.core :refer [jdbc-store]]
            [mount.core :refer [defstate]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session-timeout :refer [wrap-idle-session-timeout]]
            [ring.util.response :as response]))

(declare ^:dynamic *identity*)

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t)
        (error-page {:status  500
                     :title   "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
   handler
   {:error-response
    (error-page
     {:status 403
      :title  "Invalid anti-forgery token"})}))

(defn wrap-formats [handler]
  (let [wrapped (wrap-restful-format
                 handler
                 {:formats [:json-kw :transit-json :transit-msgpack]})]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn on-error [request response]
  (error-page
   {:status 403
    :title  (str "Access to " (:uri request) " is not authorized")}))

(defn wrap-restricted [handler]
  (restrict handler {:handler  authenticated?
                     :on-error on-error}))

(defn wrap-identity [handler]
  (fn [request]
    (binding [*identity* (get-in request [:session :identity])]
      (handler request))))

(defn wrap-auth [handler]
  (let [backend (session-backend)]
    (-> handler
        wrap-identity
        (wrap-authentication backend)
        (wrap-authorization backend))))

(defstate session-cleaner
  :start #(start-cleaner *db*)
  :stop  #(stop-cleaner session-cleaner))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-auth
      wrap-flash
      (wrap-idle-session-timeout {:timeout-response (response/redirect "/")
                                  :timeout          (:session-timeout env)})
      (wrap-session {:cookie-attrs {:http-only true}
                     :store        (jdbc-store *db*)})
      (wrap-defaults (-> site-defaults
                         (assoc-in [:security :anti-forgery] false)
                         (dissoc :session)))
      wrap-gzip
      wrap-internal-error))
