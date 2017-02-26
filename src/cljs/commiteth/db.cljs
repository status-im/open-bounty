(ns commiteth.db)

(def default-db
  {:page             :activity
   :user             nil
   :repos-loading?   false
   :repos            {}
   :owner-bounties   {}
   :top-hunters []
   :activity-feed [] #_[{:type :create-bounty
                         :user {:display-name "Dummy User"
                                :avatar-url "https://randomuser.me/api/portraits/men/6.jpg"}
                         :issue-title "Feature X"
                         :issue-url "https://github.com/foo/bar/issues/2"
                         :timestamp "1 day ago"}
                        {:type :submit-claim
                         :user {:display-name "Place Holder"
                                :avatar-url "https://randomuser.me/api/portraits/men/4.jpg"}
                         :balance-eth "15"
                         :timestamp "2 days ago"}
                        {:type :balance-update
                         :user {:display-name "Place Holder"
                                :avatar-url "https://randomuser.me/api/portraits/men/4.jpg"}

                         :issue-title "Feature Y"
                         :issue-url "https://github.com/foo/bar/issues/1"
                         :balance-eth "15"
                         :timestamp "2 days ago"}]})
