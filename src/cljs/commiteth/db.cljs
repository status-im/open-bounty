(ns commiteth.db)

(def default-db
  {:page             :bounties
   :user             nil
   :repos-loading?   false
   :repos            {}
   :activity-feed-loading? false
   :open-bounties-loading? false
   :open-bounties []
   :owner-bounties   {}
   :top-hunters []
   :activity-feed []})
