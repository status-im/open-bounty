(ns commiteth.home.page
  (:require [re-frame.core :as rf]))

(def github-url "https://github.com/")

(defn issue-url
  [user repo issue-number]
  (str github-url user "/" repo "/issues/" issue-number))

(defn pull-url
  [user repo pull-number]
  (str github-url user "/" repo "/pull/" pull-number))

(defn user-url
  [user]
  (str github-url user))

(defn get-amount
  [address]
  (.-length address))

(defn bounty-row [{issue-id      :issue_id
                   issue-number  :issue_number
                   pr-number     :pr_number
                   user          :user_login
                   owner         :owner_name
                   repo          :repo_name
                   issue-title   :issue_title
                   address       :payout_address
                   issue-address :issue_address}]
  ^{:key issue-id}
  [:li.list-group-item
   [:div
    [:a {:href (issue-url owner repo issue-number)} issue-title]]
   [:div
    [:a {:href (pull-url owner repo pr-number)} "fixed"]
    " by "
    [:a {:href (user-url user)} user]]
   [:div "Payout address: " address]
   [:div "Amount: " (get-amount issue-address) " ETH"]])

(defn bounties-list []
  (let [bounties (rf/subscribe [:bounties])]
    (fn []
      [:ul.list-group
       (map bounty-row @bounties)])))


(defn issue-row [{issue-id      :issue_id
                  issue-number  :issue_number
                  owner         :owner_name
                  repo          :repo_name
                  issue-title   :issue_title
                  issue-address :issue_address}]
  ^{:key issue-id}
  [:li.list-group-item
   [:div
    [:a {:href (issue-url owner repo issue-number)} issue-title]]
   [:div "Amount: " (get-amount issue-address) " ETH"]])

(defn issues-list []
  (let [issues (rf/subscribe [:issues])]
    (fn []
      [:ul.list-group
       (map issue-row @issues)])))

(defn home-page []
  (fn []
    [:div
     [:h3 "List of issues fixed by PRs awaiting to be signed "]
     [bounties-list]
     [:h3 "List of all issues"]
     [issues-list]]))
