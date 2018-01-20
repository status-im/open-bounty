(ns commiteth.db
  (:require [commiteth.ui-model :as ui-model]))

(def default-db
  {:page                        :bounties
   :user                        nil
   :repos-loading?              false
   :repos                       {}
   :activity-feed-loading?      false
   :open-bounties-loading?      false
   :open-bounties               []
   ::open-bounties-sorting-type ::ui-model/bounty-sorting-type|most-recent
   ::open-bounties-filters      {::ui-model/bounty-filter-type|value    nil
                                 ::ui-model/bounty-filter-type|currency nil
                                 ::ui-model/bounty-filter-type|date     nil
                                 ::ui-model/bounty-filter-type|owner    nil}
   :owner-bounties              {}
   :top-hunters                 []
   :activity-feed               []})
