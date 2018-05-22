(ns commiteth.github.core
  (:require [tentacles
             [core :as tentacles]
             [repos :as repos]
             [oauth :as oauth]
             [search :as search]
             [users :as users]
             [repos :as repos]
             [issues :as issues]
             [orgs :as orgs]]
            [ring.util.codec :as codec]
            [clj-http.client :as http]
            [commiteth.config :refer [env]]
            [digest :refer [sha-256]]
            [commiteth.db.issues :as db-issues]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
            [commiteth.util.png-rendering :as png-rendering]
            [commiteth.db.issues :as db-issues]
            [commiteth.db.bounties :as db-bounties]
            [commiteth.db.comment-images :as comment-images]
            [clojure.string :as str])
  (:import [java.util UUID]))

(def ^:dynamic url "https://api.github.com/")
(defn server-address [] (:server-address env))
(defn client-id [] (:github-client-id env))
(defn client-secret [] (:github-client-secret env))
(defn redirect-uri [] (str (server-address) "/callback"))
(defn hook-secret [] (:github-hook-secret env))
(defn self [] (:github-user env))
(defn self-password [] (:github-password env))
(defn on-testnet? [] (env :on-testnet))
(defn webhook-secret [] (env :webhook-secret))



(defn authorize-url [scope]
  (let [params (codec/form-encode {:client_id    (client-id)
                                   :redirect_uri (redirect-uri)
                                   :scope        scope
                                   :allow_signup true
                                   :state        (str (UUID/randomUUID))})]
    (str "https://github.com/login/oauth/authorize" "?" params)))

(defn signup-authorize-url []
  (authorize-url "user:email"))

;; NOTE: Capabilities picked for Github apps if true, Oauth if false
(defn github-app-enabled? []
  (env :github-app-enabled) true)

(defn admin-authorize-url []
  (if (github-app-enabled?)
    (authorize-url "user:email")
    (authorize-url "admin:repo_hook repo user:email admin:org_hook")))

(defn access-settings-url []
  (str "https://github.com/settings/connections/applications/" (client-id)))

(defn post-for-token
  [code state]
  (http/post "https://github.com/login/oauth/access_token"
             {:content-type :json
              :form-params  {:client_id     (client-id)
                             :client_secret (client-secret)
                             :code          code
                             :redirect_uri  (redirect-uri)
                             :state         state}}))

(defn auth-params
  [token]
  {:oauth-token  token
   :client-id    (client-id)
   :client-token (client-secret)})

(defn- self-auth-params []
  {:auth (str (self) ":" (self-password))})

(def repo-fields
  [:id
   :name
   :full_name
   :description
   :html_url
   :has_issues
   :open_issues
   :issues_url
   :fork
   :created_at
   :permissions
   :private])

(defn get-user-repos
  "List all repos managed by the given user. Returns map of repository
  maps grouped by owner. Owner is an organization name or the user's
  own login."
  [token]
  (let [all-repos-with-admin-access
        (filter #(-> % :permissions :admin)
                (map #(merge
                       {:owner-login (get-in % [:owner :login])}
                       {:owner-type (get-in % [:owner :type])}
                       {:owner-avatar-url (get-in % [:owner :avatar_url])}
                       (select-keys % repo-fields))
                     (repos/repos
                      (merge (auth-params token) {:type      "all"
                                                  :all-pages true}))))]
    (group-by :owner-login all-repos-with-admin-access)))

(defn get-user
  [token]
  (users/me (auth-params token)))

(defn get-user-email
  [token]
  (let [emails (users/emails (auth-params token))]
    (->
     (filter :primary emails)
     first
     :email)))

(defn status-team-member?
  [token]
  (let [user-login (:login (users/me (auth-params token)))
        user-teams (map :name (users/my-teams (auth-params token)))
        status-org-members (map :login (orgs/members "status-im" (self-auth-params)))]
    (or
     (true? (some #(= "Status" %) user-teams))
     (true? (some #(= user-login %) status-org-members)))))

(defn our-webhooks
  [owner repo token]
  (let [hooks (repos/hooks owner repo (auth-params token))
        url-base (:server-address env)]
    (log/debug "url-base" url-base)
    (filter (fn [{{url :url} :config}] (and url (str/starts-with? url url-base)))
            hooks)))


(defn webhook-exists?
  "Returns true if a webhook starting with our server url exists"
  [full-repo token]
  (let [[owner repo] (str/split full-repo #"/")
        hooks (our-webhooks owner repo token)]
    (not-empty hooks)))


(defn remove-webhook
  [full-repo hook-id token]
  ;; TODO: possible error ignored
  (let [[owner repo] (str/split full-repo #"/")]
    (log/debug "removing webhook" (str owner "/" repo) hook-id token)
    (repos/delete-hook owner repo hook-id (auth-params token))))


(defn remove-our-webhooks
  "Removes webhooks created by us for given repo"
  [full-repo token]
  (let [[owner repo] (str/split full-repo #"/")
        hooks (our-webhooks owner repo token)]
    (doall
     (map (fn [{hook-id :id}]
            (remove-webhook full-repo hook-id token))
          hooks))))


(defn add-webhook
  [full-repo token secret]
  (log/debug "adding webhook" full-repo token)
  (let [[owner repo] (str/split full-repo #"/")]
    (repos/create-hook owner repo "web"
                       {:url          (str (server-address) "/webhook")
                        :secret secret
                        :content_type "json"}
                       (merge (auth-params token)
                              {:events ["issues", "issue_comment", "pull_request"]
                               :active true}))))


(defn github-comment-hash
  [owner repo issue-number balance]
  (digest/sha-256 (str "SALT_Yoh2looghie9jishah7aiphahphoo6udiju" owner repo issue-number balance)))

(defn- get-qr-url
  [owner repo issue-number balance]
  (let [hash (github-comment-hash owner repo issue-number balance)]
    (str (server-address) (format "/qr/%s/%s/bounty/%s/%s/qr.png" owner repo issue-number hash))))

(defn- md-url
  ([text url]
   (str "[" text "](" url ")"))
  ([url]
   (md-url url url)))

(defn- md-image
  [alt src]
  (str "!" (md-url alt src)))

(defn etherscan-tx-url [tx-id]
   (str "https://"
          (when (on-testnet?) "ropsten.")
          "etherscan.io/tx/" tx-id))

(defn generate-deploying-comment
  [owner repo issue-number tx-id]
  (let [deploying-image (md-image
                         "Contract deploying"
                         (str (server-address) "/img/deploying_contract.png"))
        tx-link (md-url tx-id (etherscan-tx-url tx-id))]
    (format (str "%s\n"
                 "Transaction: %s\n")
            deploying-image
            tx-link)))

(defn network-text []
  (str "Network: " (if (on-testnet?)
                     "Testnet (Ropsten)"
                     "Mainnet")
       "\n"))

(defn token-balances-text [token-balances]
  (when-not (empty? token-balances)
    (str "Tokens: "
         (str/join " " (map (fn [[tla balance]] (format "%s: %.2f"
                                                       (subs (str tla) 1)
                                                       (double balance)))
                            token-balances))
         "\n")))

(defn contract-addr-text [addr]
  (let [url-base (if (on-testnet?) "https://ropsten.etherscan.io"
                     "https://etherscan.io")]
    (str "Contract address: [" addr "](" url-base "/address/" addr ")\n")))

(defn generate-open-comment
  [owner repo issue-number contract-address eth-balance tokens]
  (let [image-url (md-image "QR Code" (get-qr-url owner repo issue-number eth-balance))
        site-url  (md-url (server-address) (server-address))]
    (format (str "Current balance: %s ETH\n"
                 (token-balances-text tokens)
                 (contract-addr-text contract-address)
                 "%s\n"
                 (network-text)
                 "To claim this bounty sign up at %s and make sure to update your Ethereum address "
                 "in `My Payment Details` so that the bounty is correctly allocated.\n"
                 (if (on-testnet?)
                   "To fund it, send test ETH or test ERC20/ERC223 tokens to the contract address."
                   "To fund it, send ETH or ERC20/ERC223 tokens to the contract address."))
            eth-balance image-url site-url)))

(defn learn-more-text []
  (let [site-url (md-url (server-address) (server-address))]
    (format "Visit %s to learn more.\n" site-url)))

(defn generate-merged-comment
  [contract-address eth-balance tokens winner-login winner-address-missing?]
  (format (str "Balance: %s ETH\n"
               (token-balances-text tokens)
               (contract-addr-text contract-address)
               (network-text)
               (str "Status: " (if winner-address-missing?
                                 "Pending user to save ETH address"
                                 "Pending maintainer confirmation")  "\n")
               "Winner: %s\n"
               (learn-more-text))
          eth-balance winner-login))

(defn generate-paid-comment
  [contract-address eth-balance-str tokens payee-login]
  (format (str "Balance: %s ETH\n"
               (token-balances-text tokens)
               (contract-addr-text contract-address)
               (network-text)
               "Paid to: %s\n"
               (learn-more-text))
          eth-balance-str payee-login))

(defn make-patch-request [end-point positional query]
  (let [{:keys [auth oauth-token]
         :as   query} query
        req          (merge-with merge
                                 {:url        (tentacles/format-url end-point positional)
                                  :basic-auth auth
                                  :method     :patch}
                                 (when oauth-token
                                   {:headers {"Authorization" (str "token " oauth-token)}}))
        raw-query    (:raw query)
        proper-query (tentacles/query-map (dissoc query :auth
                                                  :oauth-token
                                                  :all-pages
                                                  :accept
                                                  :user-agent
                                                  :otp))]
    (assoc req :body (json/generate-string (or raw-query proper-query)))))

(defn update-bounty-comment-image [{:keys [issue-id owner repo issue-number contract-address balance-eth tokens]}]
  (let [hash (github-comment-hash owner repo issue-number balance-eth)
        issue-url (str owner "/" repo "/issues/" (str issue-number))
        png-data (png-rendering/gen-comment-image
                  contract-address
                  balance-eth
                  tokens
                  issue-url)]
    (log/debug "update-bounty-comment-image" issue-id owner repo issue-number)
    (log/debug contract-address balance-eth)
    (log/debug "hash" hash)

    (if png-data
      (comment-images/save-image! issue-id hash png-data)
      (log/error "Failed ot generate PNG"))))

(defn post-deploying-comment
  [issue-id tx-id]
  (let [{owner :owner
         repo :repo
         issue-number :issue_number
         comment-id :comment_id} (db-issues/get-issue-by-id issue-id)
        comment (generate-deploying-comment owner repo issue-number tx-id) ]
    (log/info "Posting comment to" (str owner "/" repo "/" issue-number) ":" comment)
    (if comment-id
      (let [req (make-patch-request "repos/%s/%s/issues/comments/%s"
                                    [owner repo comment-id]
                                    (assoc (self-auth-params) :body comment))]
        (tentacles/safe-parse (http/request req)))
      (let [resp (issues/create-comment owner repo issue-number comment (self-auth-params))]
        (db-issues/update-comment-id issue-id (:id resp))
        (log/infof "issue %s: post-deploying-comment response: %s" issue-id resp)
        resp))))

(defn update-comment
  "Update comment for an open bounty issue"
  [{:keys [issue-id owner repo comment-id issue-number contract-address 
           balance-eth tokens
           payout-receipt
           winner-login winner-address transaction-hash] :as issue}
   state]
  (let [comment (case state
                  :deploying
                  (generate-deploying-comment owner repo issue-number transaction-hash)
                  (:opened :update-balances)
                  (generate-open-comment owner
                                         repo
                                         issue-number
                                         contract-address
                                         balance-eth
                                         tokens)
                  (:pending-maintainer-confirmation :pending-contributor-address)
                  (generate-merged-comment contract-address
                                           balance-eth
                                           tokens
                                           winner-login
                                           (str/blank? winner-address))
                  :paid
                  (generate-paid-comment contract-address
                                         balance-eth
                                         tokens
                                         winner-login)
                  nil)]
    (log/info (str "Updating " owner "/" repo "/" issue-number
                    " comment #" comment-id " with contents: " comment))
    (if (= state :deploying) 
      (let [resp (issues/create-comment owner repo issue-number comment (self-auth-params))
            comment-id (:id resp)]
        (db-issues/update-comment-id issue-id comment-id))
      (when comment
        (let [req (make-patch-request "repos/%s/%s/issues/comments/%s"
                                      [owner repo comment-id]
                                      (assoc (self-auth-params) :body comment))]
          (tentacles/safe-parse (http/request req)))))))

(defn get-issue
  [owner repo issue-number]
  (issues/specific-issue owner repo issue-number (self-auth-params)))

(defn get-issues
  [owner repo]
  (issues/issues owner repo))


(defn get-issue-events
  [owner repo issue-number]
  (issues/issue-events owner repo issue-number (self-auth-params)))

(defn create-label
  [full-repo token]
  (let [[owner repo] (str/split full-repo #"/")]
    (log/debug "creating bounty label" (str owner "/" repo) token)
    (issues/create-label owner repo "bounty" "fafad2" (auth-params token))))

(defn get-labeled-issues-for-repos [repos auth-params]
  "Find all issues with a bounty label in provided repos"
  (let [get-last-part (fn get-last-part [s]
                        (subs s (inc (str/last-index-of s "/"))))
        get-issue-info (fn get-issue-info [r issue]
                         (hash-map :owner r
                                   :repo (get-last-part (:repository_url issue))
                                   :id (:id issue)
                                   :number (:number issue)
                                   :title (:title issue)
                                   :url (:html_url issue)
                                   :created_at (:created_at issue)
                                   :closed_at (:closed_at issue))) 
        issues (for [r repos]
                 (loop [repo-issues [] i 1]
                   (let [params (into auth-params
                                      {:sort "created" :order "desc" :page i})
                         issues-page (-> (search/search-issues 
                                           nil
                                           {:label "bounty" :user r :type "issue"}
                                           params)
                                         :items)]
                     (if (first issues-page)
                       (recur (into repo-issues 
                                    (map (partial get-issue-info r) issues-page)) 
                              (inc i))
                       repo-issues))))]
    (apply concat issues)))
