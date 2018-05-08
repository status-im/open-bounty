(ns commiteth.interceptors
  (:require [commiteth.db :as db]
            [re-frame.core :as rf]
            [clojure.data :as data]))

(defn get-bounty-field [owner-bounties field-name]
  "for app-db representation of owner bounties, return a list of just the relevant field"
  (-> owner-bounties
      vals
      (->> (map #(field-name %)))))

(defn filter-updated-bounties [field-name old-bounties new-bounties]
  "filters collection of bounties to only those with a field that has been recently set"
  (filter (fn [[issue-id owner-bounty]]
            (let [new-field-value (field-name owner-bounty)
                  old-field-value (get-in old-bounties [issue-id field-name])]
              (when (and (nil? old-field-value) (some? new-field-value))
                (println "old value for " field-name " is " old-field-value)
                (println "new value for " field-name "is " new-field-value)
                true)))
          new-bounties))

(defn dispatch-confirm-payout [bounty]
  "dispatches a bounty via reframe dispatch"
  (rf/dispatch [:confirm-payout {:issue_id         (:issue-id bounty)
                                 :owner_address    (:owner_address bounty)
                                 :contract_address (:contract_address bounty)
                                 :confirm_hash     (:confirm_hash bounty)}]))

(defn dispatch-remove-pending-revocation [bounty]
  "dispatches a bounty via reframe dispatch"
  (rf/dispatch [:remove-pending-revocation (:issue-id bounty)]))

(def watch-confirm-hash
  "An interceptor which exaimines the event diff for `:load-owner-bounties`
  and dispatches a `confirm-payout` event when one of the owner bounties has
  its confirm_hash updated.

  *Warning* this inteceptor is only intended for use with the
  `:load-owner-bounties` event

  More information on re-frame interceptors can be found here:
  https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md"

  (rf/->interceptor
    :id     :watch-confirm-hash
    :after  (fn confirm-hash-update-after
              [context]
              (println "watch confirm hash interceptor...")
              (let [event-name          (-> context
                                            :coeffects
                                            :event
                                            first)
                    pending-revocations (get-in context [:coeffects :db ::db/pending-revocations])
                    start-ob            (get-in context [:coeffects :db :owner-bounties])
                    end-ob              (get-in context [:effects :db :owner-bounties])]
                ;; proceed when the change isn't caused by page load
                (when (not-empty start-ob) 
                  (let [[only-before only-after both] (data/diff
                                                       (get-bounty-field start-ob :confirm_hash)
                                                       (get-bounty-field end-ob :confirm_hash))]
                    ;; proceed when confirm hashes have changed and there is a pending revocation
                    (when (and only-after (not-empty pending-revocations))
                      (println "pending revocations are " pending-revocations)
                      (let [updated-bounties (->> end-ob
                                                  (filter-updated-bounties :confirm_hash start-ob)
                                                  vals)]
                        ;; for bounties which just had confirm hash set: perform
                        ;; dispatch side effect but interceptor must return context
                        (doseq [bounty updated-bounties]
                          (dispatch-confirm-payout bounty))))))
                context))))


(def watch-payout-receipt
  "An interceptor which exaimines the event diff for `:load-owner-bounties`
  and dispatches a `remove-pending-revocation` event when one of them has
  its payout_receipt set.

  *Warning* this inteceptor is only intended for use with the
  `:load-owner-bounties` event

  More information on re-frame interceptors can be found here:
  https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md"

  (rf/->interceptor
    :id     :watch-payout-receipt
    :after  (fn payout-receipt-update-after
              [context]
              (println "watch payout receipt interceptor...")
              (let [event-name          (-> context
                                            :coeffects
                                            :event
                                            first)
                    pending-revocations (get-in context [:coeffects :db ::db/pending-revocations])
                    start-ob            (get-in context [:coeffects :db :owner-bounties])
                    end-ob              (get-in context [:effects :db :owner-bounties])]
                ;; proceed when the change isn't caused by page load
                (when (not-empty start-ob) 
                  (let [[only-before only-after both] (data/diff
                                                       (get-bounty-field start-ob :payout_receipt)
                                                       (get-bounty-field end-ob :payout_receipt))]
                    ;; proceed when payout_receipt has been updated and there is a pending revocation
                    (when (and only-after (not-empty pending-revocations))
                      (println "pending revocations are " pending-revocations)
                      (let [updated-bounties (->> end-ob
                                                  (filter-updated-bounties :payout_receipt start-ob)
                                                  vals)]
                        ;; for bounties which just had confirm hash set: perform
                        ;; dispatch side effect but interceptor must return context
                        (doseq [bounty updated-bounties]
                          (dispatch-remove-pending-revocation bounty))))))
                context))))
