(ns commiteth.bounties
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [commiteth.common :refer [moment-timestamp
                                      issue-url]]
            [commiteth.handlers :as handlers]
            [commiteth.db :as db]
            [commiteth.ui-model :as ui-model]
            [commiteth.subscriptions :as subs]))


(defn bounty-item [bounty]
  (let [{avatar-url   :repo_owner_avatar_url
         owner        :repo-owner
         repo-name    :repo-name
         issue-title  :issue-title
         issue-number :issue-number
         updated      :updated
         tokens       :tokens
         balance-eth  :balance-eth
         value-usd    :value-usd
         claim-count  :claim-count} bounty
        full-repo  (str owner "/" repo-name)
        repo-url   (str "https://github.com/" full-repo)
        repo-link  [:a {:href repo-url} full-repo]
        issue-link [:a
                    {:href (issue-url owner repo-name issue-number)}
                    issue-title]]
    [:div.open-bounty-item
     [:div.open-bounty-item-content
      [:div.header issue-link]
      [:div.bounty-item-row
       [:div.time (moment-timestamp updated)]
       [:span.bounty-repo-label repo-link]]

      [:div.footer-row
       [:div.balance-badge "ETH " balance-eth]
       (for [[tla balance] tokens]
         ^{:key (random-uuid)}
         [:div.balance-badge.token
          (str (subs (str tla) 1) " " balance)])
       [:span.usd-value-label "Value "] [:span.usd-balance-label (str "$" value-usd)]
       (when (> claim-count 0)
         [:span.open-claims-label (str claim-count " open claim"
                                       (when (> claim-count 1) "s"))])]]
     [:div.open-bounty-item-icon
      [:div.ui.tiny.circular.image
       [:img {:src avatar-url}]]]]))

(defn bounties-filter-tooltip [content]
  [:div.open-bounties-filter-element-tooltip
   content])

(defn bounties-filter-tooltip-value-input [label tooltip-open? opts]
  [:div.open-bounties-filter-element-tooltip-value-input-container
   [:div.:input.open-bounties-filter-element-tooltip-value-input-label
    label]
   [:input.open-bounties-filter-element-tooltip-value-input
    {:type      "range"
     :min       (:min opts)
     :max       (:max opts)
     :step      (:step opts)
     :value     (:current-val opts)
     :on-change (when-let [f (:on-change-val opts)]
                  #(-> % .-target .-value int f))
     :on-focus  #(reset! tooltip-open? true)}]])

(defn bounties-filter-tooltip-value [current-filter-value tooltip-open?]
  (let [default-min       0
        default-max       1000
        common-range-opts {:min default-min :max default-max}
        current-min       (or (first current-filter-value) default-min)
        current-max       (or (second current-filter-value) default-max)
        on-change-fn      (fn [min-val max-val]
                            (rf/dispatch [::handlers/set-open-bounty-filter-type
                                          ::ui-model/bounty-filter-type|value
                                          [(min min-val (dec default-max))
                                           (max max-val (inc default-min))]]))
        on-min-change-fn  (fn [new-min]
                            (let [new-max (max current-max (inc new-min))]
                              (on-change-fn new-min new-max)))
        on-max-change-fn  (fn [new-max]
                            (let [new-min (min current-min (dec new-max))]
                              (on-change-fn new-min new-max)))]
    [:div
     "$0 - $1000+"
     [bounties-filter-tooltip-value-input "Min" tooltip-open? (merge common-range-opts
                                                                     {:current-val   current-min
                                                                      :on-change-val on-min-change-fn})]
     [bounties-filter-tooltip-value-input "Max" tooltip-open? (merge common-range-opts
                                                                     {:current-val   current-max
                                                                      :on-change-val on-max-change-fn})]]))

(defn bounties-filter-tooltip-currency [current-filter-value tooltip-open?]
  (let [currencies (rf/subscribe [::subs/open-bounties-currencies])]
    [:div.open-bounties-filter-list
     (for [currency @currencies]
       (let [active? (and current-filter-value (current-filter-value currency))]
         [:div.open-bounties-filter-list-option-checkbox
          [:label
           {:on-click #(rf/dispatch [::handlers/set-open-bounty-filter-type
                                     ::ui-model/bounty-filter-type|currency
                                     (cond
                                       (and active? (= #{currency} current-filter-value)) nil
                                       active? (disj current-filter-value currency)
                                       :else (into #{currency} current-filter-value))])}
           [:input
            {:type     "checkbox"
             :on-focus #(reset! tooltip-open? true)}]
           [:div.text currency]]]))]))

(defn bounties-filter-tooltip-date [current-filter-value tooltip-open?]
  [:div.open-bounties-filter-list
   (for [[option-type option-text] ui-model/bounty-filter-type-date-options-def]
     ^{:key (str option-type)}
     [:div.open-bounties-filter-list-option
      (merge {:on-click #(do (rf/dispatch [::handlers/set-open-bounty-filter-type
                                           ::ui-model/bounty-filter-type|date
                                           option-type])
                             (reset! tooltip-open? false))}
             (when (= option-type current-filter-value)
               {:class "active"}))
      option-text])])

(defn bounties-filter-tooltip-owner [current-filter-value tooltip-open?]
  (let [owners (rf/subscribe [::subs/open-bounties-owners])]
    [:div.open-bounties-filter-list
     (for [owner @owners]
       (let [active? (and current-filter-value (current-filter-value owner))]
         [:div.open-bounties-filter-list-option-checkbox
          [:label
           {:on-click #(rf/dispatch [::handlers/set-open-bounty-filter-type
                                     ::ui-model/bounty-filter-type|owner
                                     (cond
                                       (and active? (= #{owner} current-filter-value)) nil
                                       active? (disj current-filter-value owner)
                                       :else (into #{owner} current-filter-value))])}
           [:input
            {:type     "checkbox"
             :on-focus #(reset! tooltip-open? true)
             :checked  (when active? "checked")}]
           [:div.text owner]]]))]))

(defn- tooltip-view-for-filter-type [filter-type]
  (condp = filter-type
    ::ui-model/bounty-filter-type|value bounties-filter-tooltip-value
    ::ui-model/bounty-filter-type|currency bounties-filter-tooltip-currency
    ::ui-model/bounty-filter-type|date bounties-filter-tooltip-date
    ::ui-model/bounty-filter-type|owner bounties-filter-tooltip-owner))

(defn bounty-filter-view [filter-type current-filter-value]
  (let [open? (r/atom false)]
    (fn [filter-type current-filter-value]
      [:div.open-bounties-filter-element-container
       {:tab-index 0
        :on-focus  #(reset! open? true)
        :on-blur   #(reset! open? false)}
       [:div.open-bounties-filter-element
        {:on-mouse-down #(swap! open? not)
         :class         (when (or current-filter-value @open?)
                          "open-bounties-filter-element-active")}
        [:div.text
         (if current-filter-value
           (ui-model/bounty-filter-value->short-text filter-type current-filter-value)
           (ui-model/bounty-filter-type->name filter-type))]
        (when current-filter-value
          [:img.remove
           {:src           "bounty-filter-remove.svg"
            :tab-index     0
            :on-focus      #(.stopPropagation %)
            :on-mouse-down (fn [e]
                             (.stopPropagation e)
                             (rf/dispatch [::handlers/set-open-bounty-filter-type filter-type nil])
                             (reset! open? false))}])]
       (when @open?
         [bounties-filter-tooltip
          [(tooltip-view-for-filter-type filter-type) current-filter-value open?]])])))

(defn bounty-filters-view []
  (let [current-filters (rf/subscribe [::subs/open-bounties-filters])]
    [:div.open-bounties-filter
     ; doall because derefs are not supported in lazy seqs: https://github.com/reagent-project/reagent/issues/18
     (doall
       (for [filter-type ui-model/bounty-filter-types]
         ^{:key (str filter-type)}
         [bounty-filter-view
          filter-type
          (get @current-filters filter-type)]))]))

(defn bounties-sort []
  (let [open? (r/atom false)]
    (fn []
      (let [current-sorting (rf/subscribe [::subs/open-bounties-sorting-type])]
        [:div.open-bounties-sort
         {:tab-index 0
          :on-blur   #(reset! open? false)}
         [:div.open-bounties-sort-element
          {:on-click #(swap! open? not)}
          (ui-model/bounty-sorting-types-def @current-sorting)
          [:div.icon-forward-white-box
           [:img
            {:src "icon-forward-white.svg"}]]]
         (when @open?
           [:div.open-bounties-sort-element-tooltip
            (for [[sorting-type sorting-name] ui-model/bounty-sorting-types-def]
              ^{:key (str sorting-type)}
              [:div.open-bounties-sort-type
               {:on-click #(do
                             (reset! open? false)
                             (rf/dispatch [::handlers/set-open-bounties-sorting-type sorting-type]))}
               sorting-name])])]))))

(defn bounties-list [open-bounties]
  [:div.ui.container.open-bounties-container
   [:div.open-bounties-header "Bounties"]
   [:div.open-bounties-filter-and-sort
    [bounty-filters-view]
    [bounties-sort]]
   (if (empty? open-bounties)
     [:div.view-no-data-container
      [:p "No recent activity yet"]]
     (into [:div.ui.items]
           (for [bounty open-bounties]
             [bounty-item bounty])))])


(defn bounties-page []
  (let [open-bounties          (rf/subscribe [:open-bounties])
        open-bounties-loading? (rf/subscribe [:get-in [:open-bounties-loading?]])]
    (fn []
      (if @open-bounties-loading?
        [:div.view-loading-container
         [:div.ui.active.inverted.dimmer
          [:div.ui.text.loader.view-loading-label "Loading"]]]
        [bounties-list @open-bounties]))))
