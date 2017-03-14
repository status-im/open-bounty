CREATE OR REPLACE VIEW "public"."activity_feed_view" AS SELECT 'open-claim'::text AS type,
    claims_view.issue_title,
    claims_view.repo_name,
    claims_view.repo_owner,
    claims_view.issue_number,
    claims_view.user_name,
    claims_view.user_avatar_url,
    claims_view.balance,
    claims_view.updated
   FROM claims_view
  WHERE claims_view.pr_state in (0,1) AND claims_view.payout_receipt IS NULL
UNION
 SELECT 'claim-payout'::text AS type,
    claims_view.issue_title,
    claims_view.repo_name,
    claims_view.repo_owner,
    claims_view.issue_number,
    claims_view.user_name,
    claims_view.user_avatar_url,
    claims_view.balance,
    claims_view.updated
   FROM claims_view
  WHERE claims_view.pr_state = 1 AND claims_view.payout_receipt IS NOT NULL
UNION
 SELECT 'new-bounty'::text AS type,
    bounties_view.issue_title,
    bounties_view.repo_name,
    bounties_view.repo_owner,
    bounties_view.issue_number,
    bounties_view.user_name,
    bounties_view.user_avatar_url,
    bounties_view.balance,
    bounties_view.updated
   FROM bounties_view
  WHERE bounties_view.balance = 0::numeric AND bounties_view.payout_receipt IS NULL
UNION
 SELECT 'balance-update'::text AS type,
    bounties_view.issue_title,
    bounties_view.repo_name,
    bounties_view.repo_owner,
    bounties_view.issue_number,
    bounties_view.user_name,
    bounties_view.user_avatar_url,
    bounties_view.balance,
    bounties_view.updated
   FROM bounties_view
  WHERE bounties_view.balance > 0::numeric AND bounties_view.payout_receipt IS NULL
  ORDER BY updated DESC;
