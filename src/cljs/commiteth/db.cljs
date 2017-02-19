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
                  :eth-earned "8 400.00"}]})
