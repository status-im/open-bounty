(ns user
  (:require [mount.core :as mount]
            [commiteth.figwheel :refer [start-fw stop-fw cljs]]
            commiteth.core))

(defn start []
  (mount/start-without #'commiteth.core/repl-server))

(defn stop []
  (mount/stop-except #'commiteth.core/repl-server))

(defn restart []
  (stop)
  (start))
