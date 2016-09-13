(ns commiteth.github.core
  (:require [tentacles.repos :as repos]
            [tentacles.users :as users]
            [tentacles.repos :as repos]
            [tentacles.issues :as issues]
            [ring.util.codec :as codec]
            [clj-http.client :as http]
            [commiteth.config :refer [env]]
            [digest :refer [sha-256]]
            [clojure.tools.logging :as log])
  (:import [java.util UUID]))

(defn server-address [] (:server-address env))
(defn client-id [] (:github-client-id env))
(defn client-secret [] (:github-client-secret env))
(defn redirect-uri [] (str (server-address) "/callback"))
(def allow-signup true)
(def hook-secret "Mu0eiV8ooy7IogheexathocaiSeLeineiLue0au8")

;; @todo: github user which will post QR codes (already banned)
(def self "h0gL")
(def self-password "Fahh7ithoh8Ahghe")

(defn authorize-url []
  (let [params (codec/form-encode {:client_id    (client-id)
                                   :redirect_uri (redirect-uri)
                                   :scope        "admin:repo_hook repo"
                                   :allow_signup allow-signup
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

(defn- auth-params
  [token]
  {:oauth-token  token
   :client-id    (client-id)
   :client-token (client-secret)})

(defn- self-auth-params []
  {:auth (str self ":" self-password)})

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

(def login-field [:owner :login])

(defn list-repos
  "List all repos managed by the given user."
  [token]
  (->>
    (map #(merge
           {:login (get-in % login-field)}
           (select-keys % repo-fields))
      (repos/repos (merge (auth-params token) {:type "all"})))
    (filter #(not (:fork %)))
    (filter #(-> % :permissions :admin))))

(defn get-user
  [token]
  (users/me (auth-params token)))

(defn add-webhook
  [token user repo]
  (println "adding webhook")
  (repos/create-hook user repo "web"
    {:url          (str (server-address) "/webhook")
     :content_type "json"}
    (merge (auth-params token)
      {:events ["issues", "issue_comment", "pull_request"]
       :active true})))

(defn remove-webhook
  [token user repo hook-id]
  (println "removing webhook")
  (repos/delete-hook user repo hook-id (auth-params token)))

(defn github-comment-hash
  [user repo issue-number]
  (digest/sha-256 (str "SALT_Yoh2looghie9jishah7aiphahphoo6udiju" user repo issue-number)))

(defn- get-qr-url
  [user repo issue-number]
  (let [hash (github-comment-hash user repo issue-number)]
    (str (server-address) (format "/qr/%s/%s/bounty/%s/%s/qr.png" user repo issue-number hash))))

(defn- md-url
  ([text url]
   (str "[" text "](" url ")"))
  ([url]
   (md-url url url)))

(defn- md-image
  [alt src]
  (str "!" (md-url alt src)))

(defn generate-comment
  [user repo issue-number balance]
  (let [image-url (md-image "QR Code" (get-qr-url user repo issue-number))
        balance   (str balance " ETH")
        site-url  (md-url (server-address) (server-address))]
    (format "Current balance: %s\n%s\n%s" balance image-url site-url)))

(defn post-comment
  [user repo issue-number balance]
  (let [comment (generate-comment user repo issue-number balance)]
    (log/info "Comment to" (str user "/" repo) ":" comment)
    (issues/create-comment user repo issue-number comment (self-auth-params))))

(defn update-comment
  [user repo comment-id issue-number balance]
  (let [comment (generate-comment user repo issue-number balance)]
    (log/info (str "Updating " user "/" repo " comment #" comment-id " with contents: " comment))
    (issues/edit-comment user repo comment-id comment (self-auth-params))))

(defn get-issue
  [user repo issue-number]
  (issues/specific-issue user repo issue-number (self-auth-params)))

(defn get-issue-events
  [user repo issue-id]
  (issues/issue-events user repo issue-id (self-auth-params)))

(defn create-label
  [user repo token]
  (issues/create-label user repo "bounty" "00ff00" (auth-params token)))
