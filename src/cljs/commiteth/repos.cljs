(ns commiteth.repos
  (:require [re-frame.core :as rf]))


(defn repo-toggle-button [enabled busy on-click]
  (let [add-busy-styles (fn [x] (conj x (when busy {:class (str "busy loading")})))
        button (if enabled
                 [:div.ui.button.small.repo-added-button (add-busy-styles {})
                  [:i.icon.check]
                  "Added"]
                 [:div.ui.button.small
                  (add-busy-styles {:on-click on-click})
                  "Add"])]

    [:div.ui.two.column.container
     button
     (when enabled
       [:a.ui.item.remove-link {:on-click on-click} "Remove"])]))


(defn repo-card [repo]
  [:div.ui.card
   [:div.content
    [:div.repo-label [:a {:href (:html_url repo)} (:full_name repo)]]
    [:div.repo-description (:description repo)]]
   [:div.repo-button-container
    [repo-toggle-button
     (:enabled repo)
     (:busy? repo)
     #(rf/dispatch [:toggle-repo repo])]]])

(defn repo-group-title [group login]
  [:h3
   (if (= group login)
     "Personal repositories"
     group)])


(defn repos-list []
  (let [repos (rf/subscribe [:repos])
        user (rf/subscribe [:user])
        repo-groups (keys @repos)]
    (fn []
      (into [:div]
            (for [[group group-repos]
                  (map (fn [group] [group (get @repos group)]) repo-groups)]
              [:div [repo-group-title group (:login @user)]
               (into [:div.ui.cards]
                     (map repo-card group-repos))])))))


(defn repos-page []
  (let [repos-loading? (rf/subscribe [:repos-loading?])]
    (fn []
      (if @repos-loading?
        [:div
         [:div.ui.active.dimmer
          [:div.ui.loader]]]
        [repos-list]))))
