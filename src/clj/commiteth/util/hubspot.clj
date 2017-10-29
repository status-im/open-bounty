(ns commiteth.util.hubspot
  (:require [commiteth.config :refer [env]]
            [clojure.tools.logging :as log]
            [clj-http.client :as http]))

(defn hubspot-api-key []
  (let [key (env :hubspot-api-key)]
    (assert (not-empty key))
    key))

(def BASE-URL "https://api.hubapi.com")

(defn get-all-contacts []
  (let [url (str BASE-URL
                 "/contacts/v1/lists/all/contacts/all?hapikey="
                 (hubspot-api-key))]

    (http/get url
              {:accept :json
               :as :json})))

(defn create-hubspot-contact
  "Create a hubspot contact using data for a newly signed up user.
  https://developers.hubspot.com/docs/methods/contacts/create_contact"
  [email name github-login]
  (let [endpoint-url (str BASE-URL "/contacts/v1/contact/?hapikey=" (hubspot-api-key))
        payload {:properties [{:property :email
                               :value email}
                              {:property "github_display_name"
                               :value name}
                              {:property "github_username"
                               :value github-login}]}]
    (http/post endpoint-url {:form-params payload
                             :content-type :json})))
