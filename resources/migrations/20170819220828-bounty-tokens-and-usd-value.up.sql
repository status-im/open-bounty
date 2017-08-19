ALTER TABLE "public"."issues" RENAME COLUMN "balance" TO "balance_eth";
ALTER TABLE "public"."issues"
  ADD COLUMN "value_usd" numeric DEFAULT '0.0',
  ADD COLUMN "tokens" jsonb;

CREATE OR REPLACE VIEW "public"."bounties_view" AS SELECT i.title AS issue_title,
    i.issue_number,
    r.repo AS repo_name,
    r.owner AS repo_owner,
    concat(r.owner, '/', r.repo)::character varying(128) AS user_name,
    r.owner_avatar_url AS user_avatar_url,
    i.payout_receipt,
    i.balance_eth as balance,
    i.updated
   FROM issues i,
    repositories r
  WHERE r.repo_id = i.repo_id AND i.contract_address IS NOT NULL AND i.comment_id IS NOT NULL
  ORDER BY i.updated;

CREATE OR REPLACE VIEW "public"."claims_view" AS SELECT i.title AS issue_title,
    i.issue_number,
    r.repo AS repo_name,
    r.owner AS repo_owner,
    COALESCE(u.name, u.login) AS user_name,
    u.avatar_url AS user_avatar_url,
    i.payout_receipt,
    p.updated,
    i.balance_eth as balance,
    p.state AS pr_state
   FROM issues i,
    users u,
    repositories r,
    pull_requests p
  WHERE r.repo_id = i.repo_id AND p.issue_id = i.issue_id AND p.user_id = u.id AND i.contract_address IS NOT NULL AND i.comment_id IS NOT NULL
  ORDER BY p.updated;
