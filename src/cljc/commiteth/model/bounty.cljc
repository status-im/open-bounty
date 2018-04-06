(ns commiteth.model.bounty
  (:require [commiteth.util :as util]))

;; Most of the functions in here are currently intended for use inside the CLJS codebase
;; record maps look vastly different on frontend and backend due to simple things like kebab/camel
;; casing as well as more complex stuff like Postgres views shuffling data around

;; In the future we may want to establish Clojure records to assign names to the various
;; incarnations of maps we currently have adding the following functions to those records
;; via a protocol. Clojure records could also be serialized via transit making it easier
;; to communicate what datatypes are returned where.


(defn merged? [claim]
  (= 1 (:pr_state claim)))

(defn paid? [claim]
  (not-empty (:payout_hash claim)))

(defn bot-confirm-unmined? [bounty]
  (empty? (:confirm_hash bounty)))

(defn confirming? [bounty]
  (:confirming? bounty))

(defn issue-url
  [bounty]
  {:pre [(:repo-owner bounty) (:repo-name bounty) (:issue-number bounty)]}
  (str "https://github.com/" (:repo-owner bounty) "/" (:repo-name bounty) "/issues/" (:issue-number bounty)))

(defn crypto-balances [bounty]
  ;; TODO add some assertions
  (cond-> (:tokens bounty)
    (< 0 (util/parse-float (:balance-eth bounty)))
    (conj [:ETH (:balance-eth bounty)])))
