(ns commiteth.interceptors
  (:require [commiteth.db :as db]
            [re-frame.core :as rf]
            [clojure.data :as data]))

(defn get-confirming-issue-id [owner pending-revocations]
  "returns the issue id for the current revocation matching the desired owner type"
  (some (fn [[issue-id revocation]]
          (when (= owner (:confirming-account revocation))
            issue-id))
        pending-revocations))

(defn dispatch-confirm-payout [bounty]
  "dispatches a bounty via reframe dispatch"
  (rf/dispatch [:confirm-payout bounty]))

(defn dispatch-set-pending-revocation [bounty]
  "update the currently confirming account to owner"
  (rf/dispatch [:set-pending-revocation (:issue-id bounty) :owner]))

(defn dispatch-remove-pending-revocation [bounty]
  "dispatches a bounty via reframe dispatch"
  (rf/dispatch [:remove-pending-revocation (:issue-id bounty)]))

(def watch-confirm-hash
  "revocations move through 2 states, confirmation by commiteth and then the repo owner
  if a commiteth revocation is detected, check to see if its confirm hash is set, and, if it is
  dispatch a confirm payout event and update the confirming account to owner

  *Warning* this inteceptor is only intended for use with the
  `:load-owner-bounties` event

  More information on re-frame interceptors can be found here:
  https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md"

  (rf/->interceptor
    :id     :watch-confirm-hash
    :after  (fn confirm-hash-update-after
              [context]
              (println "watch confirm hash interceptor...")
              (let [pending-revocations (get-in context [:effects :db ::db/pending-revocations])
                    updated-bounties    (get-in context [:effects :db :owner-bounties])
                    confirming-issue-id (get-confirming-issue-id :commiteth pending-revocations)]
                (when-let [revoking-bounty (get updated-bounties confirming-issue-id)] 
                  (if (:confirm_hash revoking-bounty)
                    (do (dispatch-confirm-payout revoking-bounty)
                        (dispatch-set-pending-revocation revoking-bounty))
                    (println (str "currently revoking " confirming-issue-id " but confirm hash has not been set yet."))))
                ;; interceptor must return context
                context))))


(def watch-payout-receipt
  "examine pending revocations with their currently confirming account set to owner
  when one of them has its payout_receipt set, dispatch `remove-pending-revocation` 

  *Warning* this inteceptor is only intended for use with the
  `:load-owner-bounties` event

  More information on re-frame interceptors can be found here:
  https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md"

  (rf/->interceptor
    :id     :watch-payout-receipt
    :after  (fn payout-receipt-update-after
              [context]
              (println "watch payout receipt interceptor...")
              (let [pending-revocations (get-in context [:effects :db ::db/pending-revocations])
                    updated-bounties    (get-in context [:effects :db :owner-bounties])
                    confirming-issue-id (get-confirming-issue-id :owner pending-revocations)]
                (when-let [revoking-bounty (get updated-bounties confirming-issue-id)]
                  (if (:payout_receipt revoking-bounty)
                    (dispatch-remove-pending-revocation revoking-bounty)
                    (println (str "currently revoking " confirming-issue-id " but payout receipt has not been set yet."))))
                ;; interceptor must return context
                context))))
