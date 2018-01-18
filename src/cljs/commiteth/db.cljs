(ns commiteth.db)

(def bounty-sorting-types {::bounty-sorting-type|most-recent   "Most recent"
                           ::bounty-sorting-type|lowest-value  "Lowest value"
                           ::bounty-sorting-type|highest-value "Highest value"
                           ::bounty-sorting-type|owner         "Owner"})

(def default-db
  {:page                   :bounties
   :user                   nil
   :repos-loading?         false
   :repos                  {}
   :activity-feed-loading? false
   :open-bounties-loading? false
   :open-bounties          []
   :owner-bounties         {}
   :top-hunters            []
   :activity-feed          []
   ::bounty-sorting-type   ::bounty-sorting-type|most-recent})
