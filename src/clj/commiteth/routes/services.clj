(ns commiteth.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [compojure.api.meta :refer [restructure-param]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]
            [commiteth.db.users :as users]
            [commiteth.db.repositories :as repositories]
            [commiteth.db.bounties :as bounties-db]
            [commiteth.bounties :as bounties]
            [commiteth.github.core :as github]
            [clojure.tools.logging :as log]
            [commiteth.eth.core :as eth]))

(defn access-error [_ _]
  (unauthorized {:error "unauthorized"}))

(defn wrap-restricted [handler rule]
  (restrict handler {:handler  rule
                     :on-error access-error}))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-restricted rule]))

(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(defapi service-routes
  {:swagger {:ui   "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version     "0.1"
                           :title       "commitETH API"
                           :description "commitETH API"}}}}

  (context "/api" []

           (context "/bounties" []
                    (GET "/all" []
                         (ok (bounties-db/list-all-bounties))))

           (context "/user" []
                    (GET "/" []
                         :auth-rules authenticated?
                         :current-user user
                         (ok {:user (users/get-user (:id user))}))
                    (POST "/address" []
                          :auth-rules authenticated?
                          :body-params [user-id :- String, address :- String]
                          :summary "Update user address"
                          (let [result (users/update-user-address (Integer/parseInt user-id) address)]
                            (if (= 1 result)
                              (ok)
                              (internal-server-error))))
                    (GET "/repositories" []
                         :auth-rules authenticated?
                         :current-user user
                         (ok {:repositories (->> (github/list-repos (:token user))
                                                 (map #(select-keys %
                                                                    [:id :html_url :name :full_name :description])))}))
                    (GET "/enabled-repositories" []
                         :auth-rules authenticated?
                         :current-user user
                         (ok (repositories/get-enabled (:id user))))
                    (POST "/bounty/:issue{[0-9]{1,9}}/payout" {:keys [params]}
                          :auth-rules authenticated?
                          :current-user user
                          (let [{issue       :issue
                                 payout-hash :payout-hash} params
                                result (bounties-db/update-payout-hash
                                        (Integer/parseInt issue)
                                        payout-hash)]
                            (if (= 1 result)
                              (ok)
                              (internal-server-error))))
                    (GET "/bounties" []
                         :auth-rules authenticated?
                         :current-user user
                         (ok (map #(conj % (let [balance (:balance %)]
                                             {:balance-eth (eth/hex->eth balance 6)
                                              :balance-wei (eth/hex->big-integer balance)}))
                                  (bounties-db/list-owner-bounties (:id user)))))
                    (POST "/repository/toggle" {:keys [params]}
                          :auth-rules authenticated?
                          :current-user user
                          (ok (let [{repo-id :id
                                     repo    :full_name} params
                                    {token   :token
                                     login   :login
                                     user-id :id} user
                                    result (or
                                            (repositories/create (merge params {:user_id user-id}))
                                            (repositories/toggle repo-id))]
                                (if (:enabled result)
                                  (let [created-hook (github/add-webhook repo token)]
                                    (log/debug "Created webhook:" created-hook)
                                    (future
                                      (github/create-label repo token)
                                      (repositories/update-hook-id repo-id (:id created-hook))
                                      (bounties/add-bounties-for-existing-issues result)))
                                  (github/remove-webhook repo (:hook_id result) token))
                                result))))))
