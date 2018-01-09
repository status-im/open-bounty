(ns user
  (:require [mount.core :as mount]
            [commiteth.figwheel :refer [start-fw stop-fw cljs]]))

(defn start []
  (require 'commiteth.core)
  (mount/start-without (ns-resolve 'commiteth.core 'repl-server)))

(defn stop []
  (require 'commiteth.core)
  (mount/stop-except (ns-resolve 'commiteth.core 'repl-server)))

(defn restart []
  (stop)
  (start))
