(ns commiteth.config
  (:require [cprop.core :refer [load-config]]
            [cprop.source :as source]
            [mount.core :refer [args defstate] :as mount]))

(defstate env :start (load-config
                       :merge
                       [(args)
                        (source/from-system-props)
                        (source/from-env)]))

(defn start! []
  (mount/start #'env))

(defn stop! []
  (mount/stop #'env))
