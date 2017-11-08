CREATE OR REPLACE VIEW "public"."activity_feed_view" AS
 SELECT 'open-claim'::text AS type,
    claims_view.issue_title,
    claims_view.repo_name,
    claims_view.repo_owner,
    claims_view.issue_number,
    claims_view.user_name,
    claims_view.user_avatar_url,
    claims_view.balance_eth,
    claims_view.tokens,
    claims_view.value_usd,
    claims_view.updated
   FROM claims_view
  WHERE claims_view.pr_state = 0
  AND claims_view.payout_receipt IS NULL
  AND claims_view.issue_open IS TRUE
UNION
 SELECT 'claim-pending'::text AS type,
    claims_view.issue_title,
    claims_view.repo_name,
    claims_view.repo_owner,
    claims_view.issue_number,
    claims_view.user_name,
    claims_view.user_avatar_url,
    claims_view.balance_eth,
    claims_view.tokens,
    claims_view.value_usd,
    claims_view.issue_updated AS updated
   FROM claims_view
  WHERE claims_view.pr_state = 1
  AND claims_view.payout_receipt IS NULL
UNION
 SELECT 'claim-payout'::text AS type,
    claims_view.issue_title,
    claims_view.repo_name,
    claims_view.repo_owner,
    claims_view.issue_number,
    claims_view.user_name,
    claims_view.user_avatar_url,
    claims_view.balance_eth,
    claims_view.tokens,
    claims_view.value_usd,
    claims_view.issue_updated AS updated
   FROM claims_view
  WHERE claims_view.pr_state = 1
  AND claims_view.payout_receipt IS NOT NULL
  ORDER BY 11 DESC;
