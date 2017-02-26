(ns commiteth.activity
  (:require [re-frame.core :as rf]
            [reagent.core :as r]))



(defn activity-item [{image-url :user_avatar_url
                      display-name :user_name
                      timestamp :updated
                      balance :balance
                      issue-title :issue_title
                      item-type :type} item]

  [:div.item.activity-item
   [:div.ui.mini.circular.image
    [:img {:src image-url}]]
   [:div.content
    [:div.header display-name]
    [:div.description
     [:p item-type]
     [:p issue-title]]
    #_[:div.time timestamp]]])

(defn activity-page []
  (let [activity-items (rf/subscribe [:activity-feed])]
    (fn []
      [:div.ui.container
       (if (empty? @activity-items)
         [:div.ui.text "No data"]
         (into [:div.ui.items]
               (for [item @activity-items]
                 [activity-item item])))])))
