(ns commiteth.interceptors
  (:require [commiteth.db :as db]
            [re-frame.core :as rf]
            [clojure.data :as data]))

(defn bounty-confirm-hashes [owner-bounties]
  "for app-db representation of owner bounties, return a list of confirm hashes"
  (-> owner-bounties
      vals
      (->> (map #(:confirm_hash %)))))

(defn filter-updated-bounties [old-bounties new-bounties]
  "filters collection of bounties to only those with a confirm has that has been recently set"
  (filter (fn [[issue-id owner-bounty]]
            (let [current-confirm-hash (:confirm_hash owner-bounty)
                  old-confirm-hash     (get-in old-bounties [issue-id :confirm_hash])]
              (and (nil? old-confirm-hash) (some? current-confirm-hash))))
          new-bounties))

(defn dispatch-bounty [bounty]
  "dispatches a bounty via reframe dispatch"
  (rf/dispatch [:confirm-payout {:issue_id         (:issue-id bounty)
                                 :owner_address    (:owner-address bounty)
                                 :contract_address (:contract_address bounty)
                                 :confirm_hash     (:confirm-hash bounty)}]))

(def confirm-hash-update
  "An interceptor which exaimines the event diff for `:load-owner-bounties`
  and dispatches a `confirm-payout` event when one of the owner bounties has
  its confirm_hash updated.

  *Warning* this inteceptor is only intended for use with the
  `:load-owner-bounties` event

  More information on re-frame interceptors can be found here:
  https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md"

  (rf/->interceptor
    :id     :confirm-hash-update
    :after  (fn confirm-hash-update-after
              [context]
              (let [event-name (-> context
                                   :coeffects
                                   :event
                                   first)
                    start-ob   (get-in context [:coeffects :db :owner-bounties])
                    end-ob     (get-in context [:effects :db :owner-bounties])]
                (when (not-empty start-ob)
                  (let [[only-before only-after both] (data/diff
                                                       (bounty-confirm-hashes start-ob)
                                                       (bounty-confirm-hashes end-ob))]

                      (when only-after ;; confirm hashes changed, now find out which ones
                        (let [updated-bounties (->> end-ob
                                                    (filter-updated-bounties start-ob)
                                                    vals)]
                          ;; for bounties which just had confirm hash set: perform side effect
                          ;; but interceptor must return context
                          (map (fn [bounty] (dispatch-bounty bounty)) updated-bounties)))))
                context))))
