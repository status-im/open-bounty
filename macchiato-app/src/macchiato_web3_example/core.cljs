(ns macchiato-web3-example.core
  (:require
    [macchiato-web3-example.config :refer [env]]
    [macchiato-web3-example.middleware :refer [wrap-defaults]]
    [macchiato-web3-example.routes :refer [router]]
    [macchiato.server :as http]
    [macchiato.middleware.session.memory :as mem]
    [mount.core :as mount :refer [defstate]]
    [taoensso.timbre :refer-macros [log trace debug info warn error fatal]]))

(defn server []
  (mount/start)
  (let [host (or (:host @env) "127.0.0.1")
        port (or (some-> @env :port js/parseInt) 3000)]
    (http/start
      {:handler    (wrap-defaults router)
       :host       host
       :port       port
       :on-success #(info "macchiato-web3-example started on" host ":" port)})))
