(ns commiteth.issues
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn- page-btn
  [table selected-page current-page]
  ^{:key current-page}
  [:a {:class    (when (= selected-page current-page) "current")
       :on-click #(rf/dispatch [:set-page table current-page])}
   (str (inc current-page))])

(defn pagination [table]
  (fn []
    (let [{page  :page
           pages :pages} @(rf/subscribe [:pagination table])
          {pages-max :pages-max} @(rf/subscribe [:get-in [:pagination-props]])]
      (when (> pages 1)
        (let [last-page  (dec pages)
              start-page (max 1 (min (- pages pages-max) (max 1 (- page (quot pages-max 2)))))
              end-page   (min (dec pages) (+ pages-max start-page))
              gap-left   (> start-page 1)
              gap-right  (< end-page last-page)]
          [:div.paginate-container
           [:div.pagination
            [:span.previous-page
             (let [disabled (= page 0)]
               {:class    (when disabled "disabled")
                :on-click (when-not disabled
                            #(rf/dispatch [:set-page table (dec page)]))})
             "Previous"]
            (page-btn table page 0)
            (when gap-left [:span.gap "…"])
            (map #(page-btn table page %) (range start-page end-page))
            (when gap-right [:span.gap "…"])
            (page-btn table page last-page)
            [:span.next-page
             (let [disabled (= page last-page)]
               {:class    (when disabled "disabled")
                :on-click (when-not disabled
                            #(rf/dispatch [:set-page table (inc page)]))})
             "Next"]]])))))

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
     [:a.box-row-link.h4
      {:href (issue-url owner repo issue-number)} title]
     [:span.gh-header-number
      (str " #" issue-number)]
     [:div.mt-1.text-small.text-gray
      (repo-link owner repo)]]]])

(defn issues-list-table
  [issues-path issue-row-fn]
  (fn []
    (let [{page :page} @(rf/subscribe (into [:pagination] issues-path))
          {page-size :page-size} @(rf/subscribe [:get-in [:pagination-props]])
          issues      (rf/subscribe issues-path)
          issues-page (->> @issues
                        (drop (* page page-size))
                        (take page-size))]
      [:div.issues-list-table
       [:ul.issues-list
        (map issue-row-fn issues-page)]])))

(defn issues-page []
  (fn []
    [:div.container
     [(issues-list-table [:all-bounties] issue-row)]
     [(pagination :all-bounties)]]))
