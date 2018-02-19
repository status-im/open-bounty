(ns commiteth.db.seed
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.future :refer :all]
            [reifyhealth.specmonstah.core :as rs]
            [commiteth.config :refer [env]]
            [commiteth.db.core :refer [*db*] :as db]
            [mount.core :as mount]
            [clojure.java.jdbc :as j]))

(def non-nil-pos-int
  (s/and pos-int? (complement nil?)))

;; names taken from https://www.ssa.gov/oact/babynames/decades/century.html
(def female-names
  '("mary" "patricia" "jennifer" "elizabeth" "linda" "barbara" "susan" "jessica" "margaret"))

(def male-names
  '("john" "robert" "william" "david" "richard" "joseph" "thomas" "charles"))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(def base-address-generator (gen/fmap #(apply str %)
           (gen/vector (gen/char-alphanumeric) 40)))

(s/def ::id non-nil-pos-int)

(s/def ::name (s/with-gen string?
                    #(s/gen (-> male-names
                                (into female-names)
                                set))))

(s/def ::login (s/and string? #(< (count %) 8)))

(s/def ::email (s/with-gen (s/and string? #(re-matches email-regex %))
                 #(gen/fmap (fn [username] (str username "@status.im"))
                            (gen/string-alphanumeric))))

(s/def ::avatar_url (s/with-gen string?
                      #(gen/fmap (fn [username] (str "https://api.adorable.io/avatars/285/" username "@adorable.png"))
                                 (s/gen ::name))))
#_
(s/def ::address (s/with-gen (s/and string? #(= 42 (count %)))
                   #(gen/fmap (fn [rest-of-address] (str "0x" rest-of-address))
                              base-address-generator)))

(s/def ::address (s/with-gen (s/and string? #(= 42 (count %)))
                   ;; this address needs to be valid for ropsten
                   #(s/gen #{"0xFC5A9d404edeDb10AA6fd379b07324aAA76019F9"})))

(s/def ::user (s/keys :req-un [::id
                               ::name
                               ::login
                               ::email
                               ::avatar_url
                               ::address]))

(s/def ::repo_id non-nil-pos-int)
(s/def ::user_id non-nil-pos-int)
(s/def ::owner string?)

(def bounty-repos
  #{"status-react" "open-bounty" "status-go" "ETHDenver"})

(s/def ::repo (s/with-gen string?
                #(s/gen bounty-repos)))

(s/def ::hook_id pos-int?)

(s/def ::state pos-int?)

(s/def ::hook_secret string?)

(s/def ::owner_avatar_url ::avatar_url)

(s/def ::repository (s/keys :req-un [::repo_id
                                     ::user_id
                                     ::owner
                                     ::repo
                                     ::hook_id
                                     ::state
                                     ::hook_secret
                                     ::owner_avatar_url]))

(s/def ::repo_id non-nil-pos-int)
(s/def ::issue_id non-nil-pos-int)
(s/def ::issue_number non-nil-pos-int)
(def issue-nouns  ["compiler" "solc" "re-frame" "wallet" "ethereum" "ropsten network"])
(def issue-verbs ["broke" "fix" "deploy" "not working" "hanging on page load" "on fire"])
(defn gen-issue-title []
  "generates a random issue title"
  (str (rand-nth issue-nouns) " " (rand-nth issue-verbs)))

(s/def ::title (s/with-gen string?
                 #(s/gen (conj #{} (gen-issue-title)))))

(defn generate-currency-value [max-bounty precision]
  (-> max-bounty
      rand
      bigdec
      (.setScale precision java.math.RoundingMode/HALF_EVEN)
      float))

(s/def ::balance_eth (s/with-gen float?
                       #(s/gen (conj #{} (generate-currency-value 3 4)))))

(def sample-tokens ["SNT" "ANT"])
(s/def ::tokens (s/with-gen map?
                  #(let [k (rand-nth sample-tokens)] ;; clojure.spec.gen.alpha does not support gen/let
                     (gen/hash-map k (gen/choose 0 3000)))))

(s/def ::value_usd (s/with-gen float?
                     #(s/gen (conj #{} (generate-currency-value 300 2)))))

(s/def ::commit_sha string?)
(s/def ::issue_number non-nil-pos-int)

(s/def ::issue (s/keys :req-un [::repo_id
                                ::issue_id
                                ::title
                                ::balance_eth
                                ::tokens
                                ::value_usd
                                ::issue_number
                                ::commit_sha]))

(s/def ::pr_id non-nil-pos-int)
(s/def ::pr_number non-nil-pos-int)

(s/def ::pull_request (s/keys :req-un [::pr_id
                                       ::pr_number
                                       ::title
                                       ::repo_id
                                       ::user_id
                                       ::commit_sha
                                       ::issue_number
                                       ::state
                                       ::issue_id]))

(defn gen-spec [spec]
  "utility used by specmonstah to generate sample data"
  (gen/generate (s/gen spec)))

(def relations
  (rs/expand-relation-template
   {::user         [{}]
    ::repository   [{:user_id [::user :id]
                     :owner [::user :name]}]
    ::issue        [{:repo_id [::repository :repo_id]}]
    ::pull_request [{:user_id  [::user :id]
                     :repo_id  [::repository :repo_id]
                     :issue_id [::issue :issue_id]}]}))

(def pull-request-result (rs/gen-tree gen-spec relations [::pull_request]))

(defn entity->table-name [entity]
  "lookup of spec entity to postgres table name"
  (-> entity
      {:user         :users
       :repository   :repositories
       :issue        :issues
       :pull_request :pull_requests}))

;; this is the actual "task" that needs to be run, everything else is setup
(defn seed-postgres [num-linked-entities]
  "inserts entities into postgres with appropriate foreign key values"
  (let [entity-count     (read-string num-linked-entities) ;; string here intended for use directly from cli
        inserted-records (atom [])
        insert!          (fn [record] (swap! inserted-records conj record))]
    (do (mount/start
         #'commiteth.config/env
         #'commiteth.db.core/*db*)
        (dotimes [n entity-count]
          (reset! inserted-records [])
          ;; this step is essentially to flatten the data
          ;; specmonstah will nest pretty deeply
          ;; the output of doall is considerably flatter
          (rs/doall insert! gen-spec relations [::pull_request])
          (println "Seeding postgres with sample data: " (+ n 1))
          (->>  @inserted-records
                (map (fn [[k v]]
                       (let [table-name (-> k
                                            name
                                            keyword
                                            entity->table-name)]
                         (j/insert! (env :jdbc-database-url) table-name v))))
                (clojure.pprint/pprint))))))

;; (seed-postgres "3")


