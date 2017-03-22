(ns commiteth.db)

(def default-db
  {:page             :activity
   :user             nil
   :repos-loading?   false
   :repos            {}
   :open-bounties []
   :owner-bounties   {}
   :top-hunters []
   :activity-feed []
   :gh-token nil
   :gh-admin-token nil})
