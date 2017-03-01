(ns commiteth.github.core
  (:require [tentacles.repos :as repos]
            [tentacles.users :as users]
            [tentacles.repos :as repos]
            [tentacles.issues :as issues]
            [tentacles.core :as tentacles]
            [ring.util.codec :as codec]
            [commiteth.config :refer [env]]
            [clj-http.client :as http]
            [commiteth.config :refer [env]]
            [digest :refer [sha-256]]
            [clojure.tools.logging :as log]
            [cheshire.core :as json]
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

(defn authorize-url []
  (let [params (codec/form-encode {:client_id    (client-id)
                                   :redirect_uri (redirect-uri)
                                   :scope        "admin:repo_hook user:email repo admin:org_hook"
                                   :allow_signup true
                                   :state        (str (UUID/randomUUID))})]
    (str "https://github.com/login/oauth/authorize" "?" params)))

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
        (->>
         (map #(merge
                {:owner-login (get-in % [:owner :login])}
                {:owner-type (get-in % [:owner :type])}
                (select-keys % repo-fields))
              (repos/repos (merge (auth-params token) {:type      "all"
                                                       :all-pages true})))
         (filter #(not (:fork %)))
         (filter #(-> % :permissions :admin)))]
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

(defn our-webhooks
  [owner repo token]
  (let [hooks (repos/hooks owner repo (auth-params token))
        url-base (:server-address env)]
    (log/debug "url-base" url-base)
    (filter (fn [{{url :url} :config}] (str/starts-with? url url-base))
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

(defn generate-comment
  [owner repo issue-number contract-address balance balance-str]
  (let [image-url (md-image "QR Code" (get-qr-url owner repo issue-number balance))
        balance   (str balance-str " ETH")
        site-url  (md-url (server-address) (server-address))]
    (format (str "Current balance: %s\n"
                 "Contract address: %s\n"
                 "%s\n%s")
            balance-str contract-address image-url site-url)))

(defn post-comment
  [owner repo issue-number contract-address balance balance-str]
  (let [comment (generate-comment owner repo issue-number contract-address balance balance-str)]
    (log/debug "Posting comment to" (str owner "/" repo "/" issue-number) ":" comment)
    (issues/create-comment owner repo issue-number comment (self-auth-params))))

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

(defn update-comment
  [owner repo comment-id issue-number contract-address balance]
  (let [comment (generate-comment owner repo issue-number contract-address balance)]
    (log/debug (str "Updating " owner "/" repo "/" issue-number
                    " comment #" comment-id " with contents: " comment))
    (let [req (make-patch-request "repos/%s/%s/issues/comments/%s"
                                  [owner repo comment-id]
                                  (assoc (self-auth-params) :body comment))]
      (tentacles/safe-parse (http/request req)))))

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
