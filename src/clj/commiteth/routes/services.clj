(ns commiteth.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [compojure.api.meta :refer [restructure-param]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]
            [commiteth.db.core :as db]
            [commiteth.db.users :as users]
            [commiteth.db.usage-metrics :as usage-metrics]
            [commiteth.db.repositories :as repositories]
            [commiteth.db.bounties :as bounties-db]
            [commiteth.bounties :as bounties]
            [commiteth.eth.core :as eth]
            [commiteth.github.core :as github]
            [clojure.tools.logging :as log]
            [commiteth.config :refer [env]]
            [commiteth.util.util :refer [usd-decimal->str
                                         eth-decimal->str]]
            [crypto.random :as random]
            [clojure.set :refer [rename-keys]]
            [clojure.string :as str]))

(defn add-bounties-for-existing-issues? []
  (env :add-bounties-for-existing-issues false))

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


(defn status-team-member? [req]
  (let [token (get-in req [:params :token])
        member? (github/status-team-member? token)]
    (log/debug "token" token "member?" member?)
    member?))

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

(def bounty-renames
  ;; TODO this needs to go away ASAP we need to be super consistent
  ;; about keys unless we will just step on each others toes constantly
  {:user_name :display-name
   :user_avatar_url :avatar-url
   :issue_title :issue-title
   :pr_title :pr-title
   :pr_number :pr-number
   :pr_id :pr-id
   :type :item-type
   :repo_name :repo-name
   :repo_owner :repo-owner
   :issue_number :issue-number
   :issue_id :issue-id
   :value_usd :value-usd
   :claim_count :claim-count
   :balance_eth :balance-eth
   :user_has_address :user-has-address})

(defn ^:private enrich-owner-bounties [owner-bounty]
  (let [claims      (map
                     #(update % :value_usd usd-decimal->str)
                     (bounties-db/bounty-claims (:issue_id owner-bounty)))
        with-claims (assoc owner-bounty :claims claims)]
    (-> with-claims
        (rename-keys bounty-renames)
        (update :value-usd usd-decimal->str)
        (update :balance-eth eth-decimal->str)
        (assoc :state (bounties/bounty-state with-claims)))))

(defn user-bounties [user]
  (let [owner-bounties (bounties-db/owner-bounties (:id user))]
    (->> owner-bounties
         (map enrich-owner-bounties)
         (map (juxt :issue-id identity))
         (into {}))))

(defn top-hunters []
  (let [renames {:user_name :display-name
                 :avatar_url :avatar-url
                 :total_usd :total-usd}]
    (map #(-> %
              (rename-keys renames)
              (update :total-usd usd-decimal->str))
         (bounties-db/top-hunters))))


(defn prettify-bounty-items [bounty-items]
  (let [format-float (fn [bounty balance]
                       (try 
                         (format "%.2f" (double balance))
                         (catch Throwable ex 
                           (log/error (str (:repo-owner bounty)
                                           "/"
                                           (:repo-name bounty)
                                           "/"
                                           (:issue-number bounty)) 
                                      "Failed to convert token value:" balance)
                           "0.00")))
        update-token-values (fn [bounty]
                              (->> bounty
                                   :tokens
                                   (map (fn [[tla balance]]
                                          [tla (format-float bounty balance)]))
                                   (into {})
                                   (assoc bounty :tokens)))]
    (map #(-> %
              (rename-keys bounty-renames)
              (update :value-usd usd-decimal->str)
              (update :balance-eth eth-decimal->str)
              update-token-values)
         bounty-items)))

(defn activity-feed []
  (prettify-bounty-items (bounties-db/bounty-activity)))

(defn open-bounties []
  (prettify-bounty-items (bounties-db/open-bounties)))



(defn current-user-token [user]
  (some identity (map #(% user) [:token :admin-token])))

(defn handle-get-user [user admin-token]
  (let [status-member? (github/status-team-member? admin-token)]
    {:user
     (-> (users/get-user (:id user))
         (dissoc :email)
         (assoc :status-team-member? status-member?))}))

(defn user-whitelisted? [user]
  (let [whitelist (env :user-whitelist #{})]
    (whitelist user)))

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
                (ok (open-bounties)))
           (GET "/usage-metrics" []
                :query-params [token :- String]
                :auth-rules status-team-member?
                :current-user user
                (do
                  (log/debug "/usage-metrics" user)
                  (ok (usage-metrics/usage-metrics-by-day))))

           (context "/user" []

                    (GET "/" {:keys [params]}
                         :auth-rules authenticated?
                         :current-user user
                         (ok (handle-get-user user (:token params))))

                    (POST "/" []
                          :auth-rules authenticated?
                          :current-user user
                          :body [body {:address s/Str
                                       :is_hidden_in_hunters s/Bool}]
                          :summary "Updates user's fields."

                          (let [user-id (:id user)
                                {:keys [address]} body]

                            (when-not (eth/valid-address? address)
                              (log/debugf "POST /user: Wrong address %s" address)
                              (bad-request! (format "Invalid Ethereum address: %s" address)))

                            (db/with-tx
                              (when-not (db/user-exists? {:id user-id})
                                (not-found! "No such a user."))
                              (db/update! :users body ["id = ?" user-id]))

                            (ok)))

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
                         (ok (user-bounties user))))))
