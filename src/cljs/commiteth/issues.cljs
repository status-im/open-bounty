(ns commiteth.issues
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn pagination []
  (fn []
    [:div.paginate-container
     [:div.pagination
      [:span.previous-page.disabled "Previous"]
      [:em.current "1"]
      [:a {:rel "next", :href "#"} "2"]
      [:a {:href "#"} "3"]
      [:a {:href "#"} "4"]
      [:a {:href "#"} "5"]
      [:span.gap "…"]
      [:a {:href "#"} "10"]
      [:a {:href "#"} "11"]
      [:a.next-page {:rel "next", :href "#"} "Next"]]]))

(defn table-list-filters []
  (fn []
    [:div.table-list-filters
     [:div.float-left.pl-3
      [:a.btn-link.selected
       {:href "#"}
       [:svg.octicon.octicon-issue-opened
        {:aria-hidden "true",
         :height      "16",
         :version     "1.1",
         :viewBox     "0 0 14 16",
         :width       "14"}
        [:path
         {:d
          "M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}]]
       "              255 Open  "]
      [:a.btn-link
       {:href "#"}
       [:svg.octicon.octicon-check
        {:aria-hidden "true",
         :height      "16",
         :version     "1.1",
         :viewBox     "0 0 12 16",
         :width       "12"}
        [:path {:d "M12 5l-8 8-4-4 1.5-1.5L4 10l6.5-6.5z"}]]
       " 1,358 Closed  "]]
     [:div.float-right
      [:div.select-menu.float-left
       [:button.btn-link.select-menu-button
        {:type "button", :aria-haspopup "true"}
        "      Author    "]]
      [:div.select-menu.float-left
       [:button.btn-link.select-menu-button
        {:type "button", :aria-haspopup "true"}
        "      Labels    "]]
      [:div.select-menu.float-left
       [:button.btn-link.select-menu-button
        {:type "button", :aria-haspopup "true"}
        "      Milestones    "]]
      [:div.select-menu.float-left
       [:button.btn-link.select-menu-button
        {:type "button", :aria-haspopup "true"}
        "      Assignee    "]]
      [:div.select-menu.float-left
       [:button.btn-link.select-menu-button
        {:type "button", :aria-haspopup "true"}
        "      Sort    "]]]]))

(defn repo-link
  [owner repo]
  (let [coordinates (str owner "/" repo)
        url         (str "https://github.com/" coordinates)]
    [:span
     [:svg.octicon.octicon-repo {:height  "16"
                                 :version "1.1"
                                 :viewBox "0 0 16 16"
                                 :width   "16"}
      [:path {:d "M4 9H3V8h1v1zm0-3H3v1h1V6zm0-2H3v1h1V4zm0-2H3v1h1V2zm8-1v12c0 .55-.45 1-1 1H6v2l-1.5-1.5L3 16v-2H1c-.55 0-1-.45-1-1V1c0-.55.45-1 1-1h10c.55 0 1 .45 1 1zm-1 10H1v2h2v-1h3v1h5v-2zm0-10H2v9h9V1z"}]]
     [:a.text-gray {:href url} coordinates]]))

(defn issue-url
  [owner repo issue-number]
  (str "https://github.com/" owner "/" repo "/issues/" issue-number))

(defn issue-row [{title        :issue_title
                  issue-id     :issue_id
                  issue-number :issue_number
                  owner        :owner_name
                  repo         :repo_name}]
  ^{:key issue-id}
  [:li.issue
   [:div.d-table.table-fixed.width-full
    [:div.float-left.pt-3.pl-3
     [:span.tooltipped.tooltipped-n
      {:aria-label "Open issue"}
      [:svg.octicon.octicon-issue-opened.open
       {:aria-hidden "true",
        :height      "16",
        :version     "1.1",
        :viewBox     "0 0 14 16",
        :width       "14"}
       [:path
        {:d
         "M7 2.3c3.14 0 5.7 2.56 5.7 5.7s-2.56 5.7-5.7 5.7A5.71 5.71 0 0 1 1.3 8c0-3.14 2.56-5.7 5.7-5.7zM7 1C3.14 1 0 4.14 0 8s3.14 7 7 7 7-3.14 7-7-3.14-7-7-7zm1 3H6v5h2V4zm0 6H6v2h2v-2z"}]]]]
    [:div.float-left.col-9.p-3.lh-condensed
     [:span.mr-2.float-right.text-gray.text-small
      (str " #" issue-number)]
     [:a.box-row-link.h4
      {:href (issue-url owner repo issue-number)} title]
     [:div.mt-1.text-small.text-gray
      (repo-link owner repo)]]
    [:div.float-right.col-2
     [:div.float-left.col-7.pt-3.pr-3.text-right
      [:div.avatar-stack.tooltipped.tooltipped-multiline.tooltipped-s
       {:aria-label "Assigned to obscuren"}
       [:a.avatar
        {:href       "#",
         :aria-label "View everything assigned to obscuren"}
        [:img.from-avatar
         {:alt    "obscuren",
          :src    "https://avatars3.githubusercontent.com/u/6264126",
          :width  "20",
          :height "20"}]]]]
     [:div.float-right.col-5.no-wrap.pt-3.pr-3.text-right
      [:a.muted-link
       {:href "#", :aria-label "1 comment"}
       [:svg.octicon.octicon-comment.v-align-middle
        {:aria-hidden "true",
         :height      "16",
         :version     "1.1",
         :viewBox     "0 0 16 16",
         :width       "16"}
        [:path
         {:d
          "M14 1H2c-.55 0-1 .45-1 1v8c0 .55.45 1 1 1h2v3.5L7.5 11H14c.55 0 1-.45 1-1V2c0-.55-.45-1-1-1zm0 9H7l-2 2v-2H2V2h12v8z"}]]
       [:span.text-small "1"]]]]]])

(defn issues-list-table [{:keys [issues-path issue-row-fn show-header?] :or {show-header? true}}]
  (let [issues (rf/subscribe issues-path)]
    (fn []
      [:div.issues-list-table
       (when show-header?
         [:div.issues-toolbar.table-list-header.clearfix
          [table-list-filters]])
       [:ul.issues-list
        (map issue-row-fn @issues)]])))

(defn issues-page []
  (fn []
    [:div.container
     [(issues-list-table {:issues-path  [:all-bounties]
                          :issue-row-fn issue-row})]
     [pagination]]))
