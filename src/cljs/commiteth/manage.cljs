(ns commiteth.manage
  (:require [re-frame.core :as rf]
            [commiteth.common :refer [input checkbox]]
            [commiteth.subscriptions :refer [user-address-path]]
            [commiteth.issues :refer [issues-list-table issue-url]]
            [clojure.set :refer [rename-keys]]))

(defn repository-row [repo]
  (let [{repo-id     :id
         url         :html_url
         name        :name
         description :description} repo]
    ^{:key repo-id}
    [:div.d-table.width-full
     [:div.d-table.col-12.width-full.py-4.border-bottom.issue
      [checkbox {:value-path [:enabled-repos repo-id]
                 :style      {:width 32 :margin-left 10}
                 :on-change  #(rf/dispatch [:toggle-repo repo])}]
      [:div.d-table-cell.col-11.pr-3.v-align-top
       [:h4.f4
        [:a {:href url} name]]
       [:p.text-gray.mt-1 description]]]]))

(defn repos-list []
  (let [repos (rf/subscribe [:repos])]
    (fn []
      [:div
       (map repository-row @repos)])))

(defn issue-row [{title        :issue_title
                  issue-id     :issue_id
                  issue-number :issue_number
                  owner        :owner_name
                  repo         :repo_name
                  balance      :balance}]
  ^{:key issue-id}
  [:li.issue
   [:div.d-table.table-fixed.width-full
    [:div.float-left.pt-3.pl-3
     [:span.tooltipped.tooltipped-n
      {:aria-label "Closed issue"}
      [:svg.octicon.octicon-issue-closed.closed
       {:aria-hidden "true",
        :height      "16",
        :version     "1.1",
        :viewBox     "0 0 14 16",
        :width       "14"}
       [:path
        {:d
         "M7 10h2v2H7v-2zm2-6H7v5h2V4zm1.5 1.5l-1 1L12 9l4-4.5-1-1L12 7l-1.5-1.5zM8 13.7A5.71 5.71 0 0 1 2.3 8c0-3.14 2.56-5.7 5.7-5.7 1.83 0 3.45.88 4.5 2.2l.92-.92A6.947 6.947 0 0 0 8 1C4.14 1 1 4.14 1 8s3.14 7 7 7 7-3.14 7-7l-1.52 1.52c-.66 2.41-2.86 4.19-5.48 4.19v-.01z"}]]]]
    [:button.btn.btn-danger.float-right
     {:type     "submit"
      :style    {:margin-top   "4px"
                 :margin-right "4px"}
      :on-click #(println %)}
     (str "Sign and send " balance " ETH")]
    [:div.float-left.col-9.p-3.lh-condensed
     [:span.mr-2.float-right.text-gray.text-small
      (str " #" issue-number)]
     [:a.box-row-link.h4
      {:href (issue-url owner repo issue-number)} title]]]])

(defn manage-page []
  (fn []
    [:div.container
     [:h3 "Bounties"]
     [(issues-list-table {:issues-path  [:owner-bounties]
                          :issue-row-fn issue-row
                          :show-header? false})]
     [:h3 "Repositories"]
     [repos-list]]))
