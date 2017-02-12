(ns commiteth.cards
  (:require [reagent.core :as r]
            [devcards.core]
            [commiteth.core-test]
            [cljs.test :refer-macros [is testing]]
            [devcards.core :refer-macros [defcard-rg
                                          deftest
                                          start-devcard-ui!]]))


(enable-console-print!)


(def app-state (r/atom {:repo-state :disabled
                        :active-tab :activity
                        :user {:login "foobar"
                               :profile-image "https://randomuser.me/api/portraits/men/4.jpg"}}))

(defn fake-toggle-action [app-state]
  (let [repo-state (:repo-state @app-state)]
    (when-not (= repo-state :busy)
      (swap! app-state assoc :repo-state :busy)
      (let [to-state (if (= repo-state :enabled)
                       :disabled
                       :enabled)]
        (.setTimeout js/window
                     #(swap! app-state assoc :repo-state to-state)
                     1000)))))


(defn repo-toggle-button [button-state on-click]
  (case button-state
    :enabled [:div.ui.two.column.container
              [:div.ui.button.small.repo-added-button
               [:i.icon.check]
               "Added"]
              [:a.ui.item.remove-link {:on-click on-click} "Remove"]]
    :disabled [:div.ui.button.small {:on-click on-click} "Add"]
    :busy [:div.ui.button.small.disabled.loading "Busy..."]))

(defn repo-dynamic [state-ratom]
  [:div.ui.card
   [:div.content
    [:div.repo-label "here-be-dragons"]
    [:div.repo-description "Here is a description for the repository."]
    [:div.ui.floated.center
     [repo-toggle-button
      (:repo-state @app-state)
      #(fake-toggle-action app-state)]]]])

(defcard-rg repo-card-dynamic
  [repo-dynamic app-state]
  app-state)


(defn dropdown-component [state-ratom]
  (let [menu (if (:dropdown-open? @state-ratom)
               [:div.ui.menu.transition.visible]
               [:div.ui.menu])]
    [:div.ui.right.dropdown.item
     {:on-click #(swap! state-ratom update-in [:dropdown-open?] not)}
     (:name @state-ratom)
     [:i.dropdown.icon]
     (into menu
           (for [item (:items @state-ratom)]
             ^{:key item} [:div.item item]))]))


(defn dropdown-component2 [dropdown-open? caption items]
  (let [menu (if @dropdown-open?
               [:div.ui.menu.transition.visible]
               [:div.ui.menu])]
    [:div.ui.browse.item.dropdown
     {:on-click #(swap! dropdown-open? not)}
     caption
     [:i.dropdown.icon]
     (into menu
           (for [item items]
             ^{:key item} [:div.item item]))]))


(defn user-component [state-ratom]
  (if-let [user (get-in @state-ratom [:user])]
    (let [login (:login user)]
      [:div.ui.text.menu
       [:div.item
        [:img.ui.mini.circular.image {:src (:profile-image user)}]]
       [dropdown-component2 (r/atom false) login ["Update address" "Sign out"]]

       #_[:a.browse.item.username-label
          ;;    {:href (str "https://github.com/" login)}

          ;; login
          ;; [:i.dropdown.icon]

          ;;        [:a.ui.button.tiny {:href "/logout"} "Sign out"]
          ]])
    [:a.ui.button.tiny {:href "#";;js/authorizeUrl
                        } "Sign in"]))

(defn activate-tab! [tab]
  (swap! app-state assoc :active-tab tab))

(defn tabs [app-state]
  (let [active-tab (:active-tab app-state)]
    [:div.ui.attached.tabular.menu.tiny
     (for [[tab caption] [[:activity "Activity"]
                          [:manage "Repositories"]
                          [:bounties "Bounties"]]]
       (let [props {:class (str "ui item"
                                (when (= active-tab tab) " active"))
                    :on-click #(activate-tab! tab)}]
         ^{:key tab} [:div props caption]))]))

(defn page-header-dynamic [state-ratom]
  [:div.ui.grid.commiteth-header
   [:div.ui.grid.four.column.container
    [:div.column
     [:img {:src "/img/logo.svg"}]]
    ^{:key 1} [:div.column]
    ^{:key 2} [:div.column]
    [:div.column
     [user-component state-ratom]]]
   (when-not (:user @state-ratom)
     [:div.ui.text.content.justified
      [:div.ui.divider.hidden]
      [:h2.ui.header "Commit ETH"]
      [:h3.ui.subheader "Earn ETH by committing to open source projects"]
      [:div.ui.divider.hidden]])
   [tabs @state-ratom]])

(defcard-rg page-header
  [page-header-dynamic app-state]
  app-state
  {})

#_(defcard-rg login-button
    [login-button {:login "foobar"}])


(defn top-hunters-dynamic [state-ratom]
  [:div "TODO"])




#_{:activity-item {:type :bounty-created
                   :issue-id 1
                   :foo 42}}

(defcard-rg feeditem-bounty-created
  "An activity feed item with a bounty-created event"
  [:div.ui.segment
   [:div.ui.grid
    [:div.six.column.row
     [:div.column
      [:img.ui.tiny.circular.image {:src "https://randomuser.me/api/portraits/men/4.jpg"}]]
     [:div.five.wide.column
      [:div.ui.grid
       [:div.row
        [:h3 "Random User"]]
       [:div.row
        [:div.content
         "ETH 15 bounty for " [:a {:href "#"} "Fix crash"]]]
       [:div.row
        [:div.ui.label.tiny "ETH 15"]
        [:div.time "2h ago"]]]]]]])


(defcard-rg feeditem-claim-submitted
  "An activity feed item with a claim event"
  [:div.ui.segment
   [:div.ui.grid
    [:div.six.column.row
     [:div.column
      [:img.ui.tiny.circular.image {:src "https://randomuser.me/api/portraits/men/5.jpg"}]]
     [:div.five.wide.column
      [:div.ui.grid
       [:div.row
        [:h3 "Pseudo-random User"]]
       [:div.row
        [:div.content
         "Submitted a claim for " [:a {:href "#"} "Fix crash"]]]
       [:div.row
        [:div.time "2h ago"]]]]]]])


(defcard-rg user-menu
  "Top right user menu component"
  (fn [state _]
    [:div.ui.text.menu
     [:div.ui.item
      [:img.ui.mini.circular.image {:src "https://randomuser.me/api/portraits/men/4.jpg"}]]
     [dropdown-component state]])
  (r/atom {:dropdown-open? false
           :name "Random User"
           :items ["foo" "bar"]})
  {:inspect-data true})

(deftest tests-can-also-be-done-here
  (is (= 0 0)))
