(ns commiteth.db)

(def default-db
  {:page             :issues
   :user             nil
   :user-profile     nil
   :repos            []
   :enabled-repos    {}
   :all-bounties     []
   :owner-bounties   []
   :pagination       {}
   :pagination-props {:page-size 10
                      :pages-max 10}})
