(ns commiteth.activity
  (:require [re-frame.core :as rf]))



(defn activity-item [{{image-url :profile-image-url
                       display-name :display-name} :user
                      timestamp :timestamp
                      description :description} item]

  [:div.item.activity-item
   [:div.ui.mini.circular.image
    [:img {:src image-url}]]
   [:div.content
    [:div.header display-name]
    [:div.description
     [:p description]]
    [:div.time timestamp]]])

(defn activity-page []
  (let [activity-items (rf/subscribe [:activity-feed])]
    (fn []
      [:div.ui.container
       (into [:div.ui.items]
             (for [item @activity-items]
               [activity-item item]))])))
