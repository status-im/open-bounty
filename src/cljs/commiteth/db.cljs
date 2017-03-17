(ns commiteth.db)

(def default-db
  {:page             :activity
   :user             nil
   :repos-loading?   false
   :repos            {}
   :open-bounties [{:confirm_hash nil,
                    :issue_id 207799767,
                    :issue_number 19,
                    :repo_name "commiteth",
                    :updated #inst "2017-03-16T18:32:43.113-00:00",
                    :issue_title "Estimate gas instead of hard-coding",
                    :repo_owner "status-im",
                    :repo_owner_avatar_url "https://avatars2.githubusercontent.com/u/11767950?v=3"
                    :balance 0.0M,
                    :payout_receipt nil,
                    :repo_id 65593470,
                    :contract_address "0xaf77604e06169b210158f00ad4d3ef028aecded9",
                    :payout_hash nil}]
   :owner-bounties   {}
   :top-hunters []
   :activity-feed []})
