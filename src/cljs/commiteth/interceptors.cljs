(ns commiteth.interceptors
  (:require [commiteth.db :as db]
            [re-frame.core :refer [->interceptor
                                   dispatch]]
            [clojure.data :as data]))

(defn bounty-confirm-hashes [owner-bounties]
  (-> owner-bounties
      vals
      (->> (map #(:confirm_hash %)))))

(def confirm-hash-update
  "An interceptor which exaimines the event diff for `:load-owner-bounties`
  and dispatches a `confirm-payout` event when one of the owner bounties has
  its confirm_hash updated.

  *Warning* this inteceptor is only intended for use with the
  `:load-owner-bounties` event

  More information on re-frame interceptors can be found here:
  https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md"
  (->interceptor
    :id     :confirm-hash-update
    :after  (fn confirm-hash-update-after
              [context]
              (let [event-name (-> context
                                   :coeffects
                                   :event
                                   first)
                    start-ob   (get-in context [:coeffects :db :owner-bounties])
                    end-ob     (get-in context [:effects :db :owner-bounties])]
                (if (empty? start-ob)
                  ;; don't treat initial load as an update to watch for
                  context
                  (let [[only-before only-after both] (data/diff
                                                       (bounty-confirm-hashes start-ob)
                                                       (bounty-confirm-hashes end-ob))]

                    ;; making it here means ther was a change in the confirm hashes
                    ;; that was not the result of just setting them on page load
                    (if only-after
                    ;; TODO we'll need to backtrack to get the issue-id
                      ;; in order to dispatch confirm payout

                      ;; now let's see if we can get the issue-id of the one that chagned
                      (let [updated-issues (->> end-ob
                                                (filter (fn [[issue-id owner-bounty]]
                                                          (let [current-confirm-hash (:confirm_hash owner-bounty)
                                                                old-confirm-hash (get-in start-ob [issue-id :confirm_hash])]
                                                            (println "old-confirm-hash is" old-confirm-hash)
                                                            (println "new confirm-hash is" current-confirm-hash)
                                                            (and (nil? old-confirm-hash) (some? new-confirm-hash)))))
                                                vals)]
                        (println "updated issues are " updated-issues))
                      context)))))))
