(ns commiteth.routes
  (:require [bide.core :as bide]
            [re-frame.core :as rf]))

(defonce router
  (bide/router [["/"               :bounties]
                ["/activity"       :activity]
                ["/repos"          :repos]
                ["/dashboard"      :dashboard]
                ["/dashboard/to-confirm" :dashboard/to-confirm]
                ["/dashboard/to-merge"   :dashboard/to-merge]
                ["/settings"       :settings]
                ["/usage-metrics"  :usage-metrics]]))

(defn on-navigate
  "A function which will be called on each route change."
  [name params query]
  (println "Route change to: " name params query)
  (rf/dispatch [:set-active-page name params query]))

(defn setup-nav! []
  (bide/start! router {:default     :bounties
                       :on-navigate on-navigate}))

(defn nav!
  ([route-id]
   (nav! route-id nil))
  ([route-id params]
   (nav! route-id params nil))
  ([route-id params query]
   (bide/navigate! router route-id params query)))

