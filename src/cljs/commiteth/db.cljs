(ns commiteth.db)

(def default-db
  {:page             :activity
   :user             nil
   :repos-loading?   false
   :repos            {}
   :owner-bounties   {}
   :top-hunters [{:avatar-url "https://randomuser.me/api/portraits/men/4.jpg"
                  :display-name "Place Holder"
                  :total-eth "11 000.00"}
                 {:avatar-url "https://randomuser.me/api/portraits/men/6.jpg"
                  :display-name "Dummy User"
                  :total-eth "8 400.00"}]
   :activity-feed [{:type :submit-claim
                    :user {:display-name "Dummy User"
                           :avatar-url "https://randomuser.me/api/portraits/men/6.jpg"}
                    :description "Submitted a claim for X"
                    :timestamp "1 day ago"}
                   {:type :submit-claim
                    :user {:display-name "Place Holder"
                           :avatar-url "https://randomuser.me/api/portraits/men/4.jpg"}
                    :description "Posted ETH 15 bounty to Y"
                    :timestamp "2 days ago"}]})
