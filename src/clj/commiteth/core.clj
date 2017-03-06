(ns commiteth.core
  (:require [commiteth.handler :as handler]
            [clojure.tools.nrepl.server :as nrepl-server]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [luminus.http-server :as http]
            [luminus-migrations.core :as migrations]
            [commiteth.config :refer [env]]
            [commiteth.scheduler]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]
            [mount.core :as mount])
  (:gen-class))

(def ^:const cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]])

(mount/defstate
http-server
  :start
  (http/start
    (-> env
      (assoc :handler (handler/app))
      (update :port #(or (-> env :options :port) %))))
  :stop
  (http/stop http-server))

(mount/defstate ^{:on-reload :noop}
repl-server
  :start
  (when-let [nrepl-port (env :nrepl-port)]
    (log/info "Starting NREPL server on port" nrepl-port)
    (nrepl-server/start-server :port nrepl-port
                               :handler cider-nrepl-handler))
  :stop
  (when repl-server
    (nrepl-server/stop-server repl-server)))


(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (log/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                      (parse-opts cli-options)
                      mount/start-with-args
                      :started)]
    (log/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& args]
  (cond
    (some #{"migrate" "rollback"} args)
    (do
      (mount/start
        #'commiteth.config/env
        #'commiteth.db.core/*db*)
      (migrations/migrate args (select-keys env [:jdbc-database-url]))
      (System/exit 0))
    :else
    (start-app args)))
