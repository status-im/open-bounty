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
                      :pages-max 10}})
