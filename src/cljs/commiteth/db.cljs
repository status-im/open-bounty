(ns commiteth.db
  (:require [commiteth.ui-model :as ui-model]))

(def default-db
  {:page                        :bounties
   :user                        nil
   :user-profile-loaded?        false
   :repos-loading?              false
   :repos                       {}
   :activity-feed-loading?      false
   :open-bounties-loading?      false
   :open-bounties               []
   :page-number                 1
   :bounty-page-number          1
   :activity-page-number        1
   ::open-bounties-sorting-type ::ui-model/bounty-sorting-type|most-recent
   ::open-bounties-filters      {::ui-model/bounty-filter-type|value            nil
                                 ::ui-model/bounty-filter-type|currency         nil
                                 ::ui-model/bounty-filter-type|date             nil
                                 ::ui-model/bounty-filter-type|owner            nil
                                 ::ui-model/bounty-filter-type|issue-title-text nil}
   ::open-bounty-claims         #{}
   :owner-bounties              {}
   :top-hunters                 []
   :activity-feed               []})
