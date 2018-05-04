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
                  ;; we only care about the case where
                  ;; any of them were nill before and set currently
                  ;; but first lets log these to see where we're at
                  (println "whole before: " start-ob)
                  (println "whole after: " end-ob)

                  (println "diff before: " only-before)
                  (println "diff after: " only-after)
                  (println "equal at:" both)

                  (if only-after
                    ;; TODO we'll need to backtrack to get the issue-id
                    ;; in order to dispatch confirm payout
                    (println "new confirm hash detected!"))
                  
                  context
                  #_(when changes-after
                    (do (println  "examining changes in:" event-name)
                        (println  "after:" changes-after)
                        ;; now we need to see if the update included
                        ;; confirm hash
                        ))))))))
