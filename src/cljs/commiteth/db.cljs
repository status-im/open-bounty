(ns commiteth.db)

(def default-db
  {:page             :activity
   :user             nil
   :repos-loading?   false
   :repos            {}
   :all-bounties     []
   :owner-bounties   []
   :error            nil
   :pagination       {}
   :pagination-props {:page-size 10
                      :pages-max 10}
   :top-hunters [{:profile-image-url "https://randomuser.me/api/portraits/men/4.jpg"
                  :display-name "Place Holder"
                  :eth-earned "11 000.00"}
                 {:profile-image-url "https://randomuser.me/api/portraits/men/6.jpg"
                  :display-name "Dummy User"
                  :eth-earned "8 400.00"}]
   :activity-feed [{:type :submit-claim
                    :user {:display-name "Dummy User"
                           :profile-image-url "https://randomuser.me/api/portraits/men/6.jpg"}
                    :description "Submitted a claim for X"
                    :timestamp "1 day ago"}
                   {:type :submit-claim
                    :user {:display-name "Place Holder"
                           :profile-image-url "https://randomuser.me/api/portraits/men/4.jpg"}
                    :description "Posted ETH 15 bounty to Y"
                    :timestamp "2 days ago"}]})
