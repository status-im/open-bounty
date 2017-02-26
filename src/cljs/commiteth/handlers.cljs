(ns commiteth.handlers
  (:require [commiteth.db :as db]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-fx]]
            [ajax.core :refer [GET POST]]
            [cuerdas.core :as str]
            [plumbing.core :refer [dissoc-in]]))

(reg-fx
 :http
 (fn [{:keys [method url on-success on-error finally params]}]
   (method url
           {:headers {"Accept" "application/transit+json"}
            :handler on-success
            :error-handler on-error
            :finally finally
            :params  params})))
(reg-fx
 :delayed-dispatch
 (fn [{:keys [args timeout]}]
   (js/setTimeout #(dispatch args)
                  timeout)))

(reg-fx
 :redirect
 (fn [{:keys [path]}]
   (println "redirecting to" path)
   (set! (.-pathname js/location) path))


 (reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db)))

(reg-event-db
 :assoc-in
 (fn [db [_ path value]]
   (assoc-in db path value)))


(reg-event-db
 :set-active-page
 (fn [db [_ page]]
   (assoc db :page page)))

(reg-event-fx
 :set-flash-message
 (fn [{:keys [db]} [_ type text]]
   (merge  {:db (assoc db :flash-message [type text])}
           (when (= type :success)
             {:delayed-dispatch {:args [:clear-flash-message]
                                 :timeout 2000}}))))

(reg-event-db
 :clear-flash-message
 (fn [db _]
   (dissoc db :flash-message)))

(reg-event-fx
 :set-active-user
 (fn [{:keys [db]} [_ user]]
   {:db         (assoc db :user user)
    :dispatch [:load-user-profile]}))

(reg-event-fx
 :sign-out
 (fn [{:keys [db]} [_]]
   {:db (assoc db :user nil)
    :redirect {:path "/logout"}}))


(reg-event-fx
 :save-payout-hash
 (fn [{:keys [db]} [_ issue-id payout-hash]]
   {:db   db
    :http {:method     POST
           :url        (str/format "/api/user/bounty/%s/payout" issue-id)
           :on-success #(dispatch [:payout-confirmed issue-id])
           :on-error   #(dispatch [:payout-confirm-failed issue-id])
           :params     {:payout-hash payout-hash}}}))


(reg-event-fx
 :load-top-hunters
 (fn [{:keys [db]} [_]]
   {:db   db
    :http {:method     GET
           :url        "/api/top-hunters"
           :on-success #(dispatch [:set-top-hunters %])}}))


(reg-event-db
 :set-top-hunters
 (fn [db [_ top-hunters]]
   (assoc db :top-hunters top-hunters)))


(reg-event-fx
 :load-owner-bounties
 (fn [{:keys [db]} [_]]
   {:db   db
    :http {:method     GET
           :url        "/api/user/bounties"
           :on-success #(dispatch [:set-owner-bounties %])}}))

(reg-event-db
 :set-owner-bounties
 (fn [db [_ issues]]
   (assoc db :owner-bounties issues)))

(reg-event-fx
 :load-user-profile
 (fn [{:keys [db]} [_]]
   {:db   db
    :http {:method     GET
           :url        "/api/user"
           :on-success #(dispatch [:set-user-profile %])
           :on-error #(dispatch [:sign-out])}}))

(reg-event-fx
 :set-user-profile
 (fn [{:keys [db]} [_ user-profile]]
   {:db
    (assoc db :user (:user user-profile))
    :dispatch-n [[:load-user-repos]
                 [:load-owner-bounties]]}))

(reg-event-db
 :clear-repos-loading
 (fn [db [_]]
   (assoc db :repos-loading? false)))

(reg-event-db
 :set-user-repos
 (fn [db [_ repos]]
   (assoc db :repos repos)))

(reg-event-fx
 :load-user-repos
 (fn [{:keys [db]} [_]]
   {:db   (assoc db :repos-loading? true)
    :http {:method     GET
           :url        "/api/user/repositories"
           :on-success #(dispatch [:set-user-repos (:repositories %)])
           :on-error   #(dispatch [:set-flash-message
                                   :error "Failed to load repositories"])
           :finally    #(dispatch [:clear-repos-loading])}}))


(defn update-repo-state [all-repos full-name data]
  (let [[owner repo-name] (js->clj (.split full-name "/"))]
    (println "update-repo-busy-state" owner repo-name)
    (update all-repos
            owner
            (fn [repos] (map (fn [repo] (if (= (:name repo) repo-name)
                                        (assoc repo
                                               :busy? (:busy? data)
                                               :enabled (:enabled data))
                                        repo))
                            repos)))))

(reg-event-fx
 :toggle-repo
 (fn [{:keys [db]} [_ repo]]
   (println repo)
   {:db   (assoc db :repos (update-repo-state (:repos db) (:full_name repo) {:busy? true
                                                                             :enabled (:enabled repo)}))
    :http {:method     POST
           :url        "/api/user/repository/toggle"
           :on-success #(dispatch [:repo-toggle-success %])
           ;; TODO           :on-error #(dispatch [:repo-toggle-error %])
           :finally #(println "finally" %)
           :params     (select-keys repo [:id :login :full_name :name])}}))




(reg-event-db
 :repo-toggle-success
 (fn [db [_ repo]]
   (println "repo-toggle-success" repo)
   (assoc db :repos (update-repo-state (:repos db)
                                       (:full_name repo)
                                       {:busy? false
                                        :enabled (:enabled repo)} ))))


(reg-event-fx
 :update-address
 (fn [{:keys [db]} [_]]
   {:db db
    :dispatch [:set-active-page :update-address]}))


(reg-event-fx
 :save-user-address
 (fn [{:keys [db]} [_ user-id address]]
   (prn "save-user-address" user-id address)
   {:db   (assoc db :updating-address true)
    :http {:method     POST
           :url        "/api/user/address"
           :on-success #(do
                          (dispatch [:assoc-in [:user [:address] address]])
                          (dispatch [:set-flash-message
                                     :success
                                     "Address saved"]))
           :on-error   #(dispatch [:set-flash-message
                                   :error
                                   (:response %)])
           :finally    #(dispatch [:clear-updating-address])
           :params     {:user-id user-id :address address}}}))

(reg-event-db
 :clear-updating-address
 (fn [db _]
   (dissoc db :updating-address)))



(defn send-transaction-callback
  [issue-id]
  (println "send-transaction-callback")
  (fn [error payout-hash]
    (println "send-transaction-callback fn")
    (when error
      (dispatch [:set-flash-message
                 :error
                 (str "Error sending transaction: " error)]))
    (when payout-hash
      (dispatch [:save-payout-hash issue-id payout-hash]))))


(reg-event-fx
 :confirm-payout
 (fn [{:keys [db]} [_ {issue-id         :issue_id
                      owner-address    :owner_address
                      contract-address :contract_address
                      confirm-hash     :confirm_hash} issue]]
   (let [send-transaction-fn (aget js/web3 "eth" "sendTransaction")
         payload {:from  owner-address
                  :to    contract-address
                  :value 1
                  :data  (str "0x797af627" confirm-hash)}]
     (println "confirm-payout" owner-address contract-address)
     (try
       (apply send-transaction-fn [(clj->js payload)
                                   (send-transaction-callback issue-id)])
       {:db (assoc-in db [:owner-bounties issue-id :confirming?] true)}
       (catch js/Error e
         {:db (assoc-in db [:owner-bounties issue-id :confirm-failed?] true)
          :dispatch [:set-flash-message
                     :error
                     (str "Failed to send transaction" e)]})))))

(reg-event-db
 :payout-confirmed
 (fn [db [_ issue-id]]
   (-> db
       (dissoc-in [:owner-bounties (:issue_id issue) :confirming?] false)
       (assoc-in [:owner-bounties (:issue_id issue) :confirmed?] true))))

(reg-event-db
 :payout-confirm-failed
 (fn [db [_ issue-id]]
   (-> db
       (dissoc-in [:owner-bounties (:issue_id issue) :confirming?] false)
       (assoc-in [:owner-bounties (:issue_id issue) :confirm-failed?] true))))
