(ns user
  (:require [mount.core :as mount]
            [commiteth.figwheel :refer [start-fw stop-fw cljs]]
            [clojure.tools.namespace.repl :as repl]))

(repl/set-refresh-dirs "src" "dev" "test")

(defn start
  "Start all the application components"
  []
  (require 'commiteth.core)
  (mount/start-without (ns-resolve 'commiteth.core 'repl-server)))

(defn stop
  "Stop all the application components"
  []
  (require 'commiteth.core)
  (mount/stop-except (ns-resolve 'commiteth.core 'repl-server)))

(defn refresh
  "Reload the latest namespace definitions"
  []
  (repl/refresh))

(defn reset
  "Restart application after refreshing namespace definitions"
  []
  (stop)
  (repl/refresh :after 'user/start))

(defn restart []
  "Restart without refreshing namespace definitions"
  (stop)
  (start))
