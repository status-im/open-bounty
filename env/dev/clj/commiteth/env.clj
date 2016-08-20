(ns commiteth.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [commiteth.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[commiteth started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[commiteth has shut down successfully]=-"))
   :middleware wrap-dev})
