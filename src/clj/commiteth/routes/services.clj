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


(defn enable-repo [repo-id repo full-repo login token]
  (log/debug "enable-repo" repo-id repo)
  (let [hook-secret (random/base64 32)]
    (try
      (repositories/update-repo repo-id {:state 1
                                         :hook_secret hook-secret})
      (let [created-hook (github/add-webhook full-repo token hook-secret)]
        (log/debug "Created webhook:" created-hook)
        (github/create-label full-repo token)
        (repositories/update-repo repo-id {:state 2
                                           :hook_id (:id created-hook)})
        (bounties/add-bounties-for-existing-issues repo repo-id login))
      (catch Exception e
        (log/info "exception when creating webhook" (.getMessage e) e)
        (repositories/update-repo repo-id {:state -1})))))


(defn disable-repo [repo-id full-repo hook-id token]
  (log/debug "disable-repo" repo-id full-repo)
  (do
    (github/remove-webhook full-repo hook-id token)
    (repositories/update-repo repo-id {:hook_secret ""
                                       :state 0
                                       :hook_id nil})))


(defn handle-toggle-repo [user params]
  (log/debug "handle-toggle-repo" user params)
  (let [{token   :token
         login   :login
         user-id :id} user
        {repo-id :id
         full-repo :full_name
         repo    :name} params
        [owner _] (str/split full-repo #"/")
        db-item (repositories/create (merge params {:user_id user-id
                                                    :login owner}))
        is-enabled (= 2 (:state db-item))]
    (if is-enabled
      (disable-repo repo-id full-repo (:hook_id db-item) token)
      (enable-repo repo-id repo full-repo login token))
    (merge
     {:enabled (not is-enabled)}
     (select-keys params [:id :full_name]))))

(defn in? [coll elem]
  (some #(= elem %) coll))

(defn handle-get-user-repos [user]
  (log/debug "handle-get-user-repos")
  (let [github-repos (github/get-user-repos (:token user))
        enabled-repos (vec (repositories/get-enabled (:id user)))
        repo-enabled? (fn [repo] (in? enabled-repos (:id repo)))
        update-enabled (fn [repo] (assoc repo :enabled (repo-enabled? repo)))]
    (into {}
          (map (fn [[group repos]] {group
                                   (map update-enabled repos)})
               github-repos))))


(defn decimal->str [n]
  (format "%.4f" n))

(defn user-bounties [user]
  (let [owner-bounties (bounties-db/list-owner-bounties (:id user))]
    (into {}
          (for [ob owner-bounties
                :let [b (update ob :balance decimal->str)]]
            [(:issue_id b)
             (conj b
                   (let [claims (->> (bounties-db/bounty-claims (:issue_id b))
                                     (map #(update % :balance decimal->str)))]
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
  {:swagger {:ui   "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version     "0.1"
                           :title       "commitETH API"
                           :description "commitETH API"}}}}

  (context "/api" []
           (GET "/top-hunters" []
                (log/debug "/top-hunters")
                (ok (top-hunters)))
           (GET "/activity-feed" []
                (log/debug "/activity-feed")
                (ok (activity-feed)))
           (context "/user" []
                    (GET "/" []
                         :auth-rules authenticated?
                         :current-user user
                         (ok {:user (dissoc
                                     (users/get-user (:id user))
                                     :token
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
                    (GET "/repositories" []
                         :auth-rules authenticated?
                         :current-user user
                         (ok {:repositories (handle-get-user-repos user)}))
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
                          (ok (handle-toggle-repo user params))))))
