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
            [commiteth.eth.core :as eth]
            [commiteth.github.core :as github]
            [clojure.tools.logging :as log]
            [commiteth.eth.core :as eth]
            [commiteth.config :refer [env]]
            [commiteth.util.util :refer [decimal->str]]
            [crypto.random :as random]
            [clojure.set :refer [rename-keys]]
            [clojure.string :as str]))

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


(defn enable-repo [repo-id repo full-repo token]
  (log/debug "enable-repo" repo-id repo)
  (when (github/webhook-exists? full-repo token)
    (github/remove-our-webhooks full-repo token))

  (let [hook-secret (random/base64 32)]
    (repositories/update-repo repo-id {:state 1
                                       :hook_secret hook-secret})
    (let [created-hook (github/add-webhook full-repo token hook-secret)]
      (log/debug "Created webhook:" created-hook)
      (repositories/update-repo repo-id {:hook_id (:id created-hook)})))
  (github/create-label full-repo token)
  (repositories/update-repo repo-id {:state 2})
  (bounties/add-bounties-for-existing-issues full-repo))


(defn disable-repo [repo-id full-repo hook-id token]
  (log/debug "disable-repo" repo-id full-repo)
  (github/remove-webhook full-repo hook-id token)
  (repositories/update-repo repo-id {:hook_secret ""
                                     :state 0
                                     :hook_id nil}))


(defn handle-toggle-repo [user params]
  (log/debug "handle-toggle-repo" user params)
  (let [{user-id :id} user
        {repo-id :id
         full-repo :full_name
         owner-avatar-url :owner-avatar-url
         token   :token
         repo    :name} params
        [owner _] (str/split full-repo #"/")
        db-user (users/get-user (:id user))]
    (if (empty? (:address db-user))
      {:status 400
       :body "Please add your ethereum address to your profile first"}
      (try
        (let [db-item (repositories/create (merge params {:user_id user-id
                                                          :owner owner}))
              is-enabled (= 2 (:state db-item))]
          (if is-enabled
            (disable-repo repo-id full-repo (:hook_id db-item) token)
            (enable-repo repo-id repo full-repo token))
          (ok (merge
               {:enabled (not is-enabled)}
               (select-keys params [:id :full_name]))))
        (catch Exception e
          (log/error "exception when enabling repo" e)
          (repositories/update-repo repo-id {:state -1})
          (internal-server-error))))))

(defn in? [coll elem]
  (some #(= elem %) coll))

(defn handle-get-user-repos [user token]
  (log/debug "handle-get-user-repos")
  (let [github-repos (github/get-user-repos token)
        enabled-repos (vec (repositories/get-enabled (:id user)))
        repo-enabled? (fn [repo] (in? enabled-repos (:id repo)))
        update-enabled (fn [repo] (assoc repo :enabled (repo-enabled? repo)))]
    (into {}
          (map (fn [[group repos]] {group
                                   (map update-enabled repos)})
               github-repos))))


(defn user-bounties [user]
  (let [owner-bounties (bounties-db/owner-bounties (:id user))]
    (into {}
          (for [ob owner-bounties
                :let [b (update ob :balance decimal->str)]]
            [(:issue_id b)
             (conj b
                   (let [claims (map
                                 #(update % :balance decimal->str)
                                 (bounties-db/bounty-claims (:issue_id b)))]
                     {:claims claims}))]))))


(defn top-hunters []
  (let [renames {:user_name :display-name
                 :avatar_url :avatar-url
                 :total_eth :total-eth}]
    (map #(-> %
              (rename-keys renames)
              (update :total-eth decimal->str))
         (bounties-db/top-hunters))))


(defn activity-feed []
  (let [renames {:user_name :display-name
                 :user_avatar_url :avatar-url
                 :issue_title :issue-title
                 :type :item-type
                 :repo_name :repo-name
                 :repo_owner :repo-owner
                 :issue_number :issue-number}
        activity-items (bounties-db/bounty-activity)]
    (map #(-> %
              (rename-keys renames)
              (update :balance decimal->str))
         activity-items)))


(defapi service-routes
  (when (:dev env)
    {:swagger {:ui   "/swagger-ui"
               :spec "/swagger.json"
               :data {:info {:version     "0.1"
                             :title       "commitETH API"
                             :description "commitETH API"}}}})

  (context "/api" []
           (GET "/top-hunters" []
                (log/debug "/top-hunters")
                (ok (top-hunters)))
           (GET "/activity-feed" []
                (log/debug "/activity-feed")
                (ok (activity-feed)))
           (GET "/open-bounties" []
                (log/debug "/open-bounties")
                (ok (map #(update % :balance decimal->str)
                         (bounties-db/open-bounties))))
           (context "/user" []
                    (GET "/" []
                         :auth-rules authenticated?
                         :current-user user
                         (ok {:user (dissoc
                                     (users/get-user (:id user))
                                     :email)}))
                    (POST "/address" []
                          :auth-rules authenticated?
                          :body-params [user-id :- Long, address :- String]
                          :summary "Update user address"
                          (if-not (eth/valid-address? address)
                            {:status 400
                             :body "Invalid Ethereum address"}
                            (let [result (users/update-user-address
                                          user-id
                                          address)]
                              (if (= 1 result)
                                (ok)
                                (internal-server-error)))))
                    (GET "/repositories" {:keys [params]}
                         :auth-rules authenticated?
                         :current-user user
                         (ok {:repositories (handle-get-user-repos
                                             user
                                             (:token params))}))
                    (GET "/enabled-repositories" []
                         :auth-rules authenticated?
                         :current-user user
                         (ok (repositories/get-enabled (:id user))))
                    (POST "/bounty/:issue{[0-9]{1,9}}/payout" {:keys [params]}
                          :auth-rules authenticated?
                          :current-user user
                          (do
                            (log/debug "/bounty/X/payout" params)
                            (let [{issue       :issue
                                   payout-hash :payout-hash} params
                                  result (bounties-db/update-payout-hash
                                          (Integer/parseInt issue)
                                          payout-hash)]
                              (log/debug "result" result)
                              (if (= 1 result)
                                (ok)
                                (internal-server-error)))))
                    (GET "/bounties" []
                         :auth-rules authenticated?
                         :current-user user
                         (log/debug "/user/bounties")
                         (ok (user-bounties user)))
                    (POST "/repository/toggle" {:keys [params]}
                          :auth-rules authenticated?
                          :current-user user
                          (handle-toggle-repo user params)))))
