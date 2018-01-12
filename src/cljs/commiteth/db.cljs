(ns commiteth.db)

(def default-db
  {:page             :bounties
   :user             nil
   :repos-loading?   false
   :repos            {}
   :activity-feed-loading? false
   :open-bounties-loading? false
   :open-bounties []
   :bounty-page-number 1
   :activity-page-number 1
   :owner-bounties   {}
   :top-hunters []
   :activity-feed []})
