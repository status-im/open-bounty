DROP VIEW "public"."claims_view" CASCADE;

CREATE VIEW "public"."claims_view" AS SELECT i.title AS issue_title,
    i.issue_number,
    r.repo AS repo_name,
    r.owner AS repo_owner,
    COALESCE(u.name, u.login) AS user_name,
    u.avatar_url AS user_avatar_url,
    i.payout_receipt,
    p.updated,
    i.updated AS issue_updated,
    i.balance_eth,
    i.tokens,
    i.value_usd,
    p.state AS pr_state,
    i.is_open AS issue_open,
    (case when u.address IS NULL THEN false ELSE true END) AS user_has_address
   FROM issues i,
    users u,
    repositories r,
    pull_requests p
  WHERE r.repo_id = i.repo_id
  AND p.issue_id = i.issue_id
  AND p.user_id = u.id
  AND i.contract_address IS NOT NULL
  AND i.comment_id IS NOT NULL
  ORDER BY p.updated;


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
    claims_view.user_has_address,
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
    claims_view.user_has_address,
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
    claims_view.user_has_address,
    claims_view.issue_updated AS updated
   FROM claims_view
  WHERE claims_view.pr_state = 1
  AND claims_view.payout_receipt IS NOT NULL
  ORDER BY 12 DESC;
