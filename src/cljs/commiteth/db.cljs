(ns commiteth.db)

(def default-db
  {:page             :issues
   :user             nil
   :repos-loading?   false
   :repos            []
   :enabled-repos    {}
   :all-bounties     []
   :owner-bounties   []
   :error            nil
   :pagination       {}
   :pagination-props {:page-size 10
                      :pages-max 10}})
