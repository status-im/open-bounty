ALTER TABLE "public"."issues"
  ADD COLUMN "is_open" boolean DEFAULT 'true';


DROP VIEW "public"."bounties_view" CASCADE;

CREATE VIEW "public"."bounties_view" AS
SELECT i.title AS issue_title,
    i.issue_number,
    r.repo AS repo_name,
    r.owner AS repo_owner,
    concat(r.owner, '/', r.repo)::character varying(128) AS user_name,
    r.owner_avatar_url AS user_avatar_url,
    i.payout_receipt,
    i.balance_eth as balance_eth,
    i.updated,
    i.tokens,
    i.value_usd,
    i.is_open AS issue_open
   FROM issues i,
    repositories r
  WHERE r.repo_id = i.repo_id
  AND i.contract_address IS NOT NULL
  AND i.comment_id IS NOT NULL
  ORDER BY i.updated;


DROP VIEW "public"."claims_view" CASCADE;

CREATE VIEW "public"."claims_view" AS
SELECT i.title AS issue_title,
    i.issue_number,
    r.repo AS repo_name,
    r.owner AS repo_owner,
    COALESCE(u.name, u.login) AS user_name,
    u.avatar_url AS user_avatar_url,
    i.payout_receipt,
    p.updated,
    i.balance_eth as balance_eth,
    i.tokens,
    i.value_usd,
    p.state AS pr_state,
    i.is_open AS issue_open
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
    claims_view.updated
   FROM claims_view
  WHERE claims_view.pr_state = 0
  AND claims_view.payout_receipt IS NULL
  AND claims_view.issue_open is true
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
    claims_view.updated
   FROM claims_view
  WHERE claims_view.pr_state = 1
  AND claims_view.payout_receipt IS NOT NULL
UNION
 SELECT 'new-bounty'::text AS type,
    bounties_view.issue_title,
    bounties_view.repo_name,
    bounties_view.repo_owner,
    bounties_view.issue_number,
    bounties_view.user_name,
    bounties_view.user_avatar_url,
    bounties_view.balance_eth,
    bounties_view.tokens,
    bounties_view.value_usd,
    bounties_view.updated
   FROM bounties_view
  WHERE bounties_view.value_usd = 0::numeric
  AND bounties_view.payout_receipt IS NULL
  AND bounties_view.issue_open is true
UNION
 SELECT 'balance-update'::text AS type,
    bounties_view.issue_title,
    bounties_view.repo_name,
    bounties_view.repo_owner,
    bounties_view.issue_number,
    bounties_view.user_name,
    bounties_view.user_avatar_url,
    bounties_view.balance_eth,
    bounties_view.tokens,
    bounties_view.value_usd,
    bounties_view.updated
   FROM bounties_view
  WHERE bounties_view.value_usd > 0::numeric
  AND bounties_view.payout_receipt IS NULL
  AND bounties_view.issue_open is true
  ORDER BY 11 DESC;
