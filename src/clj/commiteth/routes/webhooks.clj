(ns commiteth.routes.webhooks
  (:require [compojure.core :refer [defroutes POST]]
            [commiteth.github.core :as github]
            [ring.util.http-response :refer [ok]]))

(def label-name "bounty")

(defn handle-issue
  [issue]
  (when-let [action (:action issue)]
    (when (and
            (= "labeled" action)
            (= label-name (get-in issue [:label :name])))
      (github/post-comment
        (get-in issue [:repository :owner :login])
        (get-in issue [:repository :name])
        (get-in issue [:issue :number]))))
  (ok (str issue)))

(defroutes webhook-routes
  (POST "/webhook" {:keys [params headers]}
    (case (get headers "x-github-event")
      "issues" (handle-issue params)
      (ok))))
