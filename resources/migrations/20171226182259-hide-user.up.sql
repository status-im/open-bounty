BEGIN;

ALTER TABLE users
    ADD COLUMN is_hidden BOOLEAN NOT NULL DEFAULT FALSE;

CREATE OR REPLACE VIEW "public"."claims_view" AS
SELECT
    i.title AS issue_title,
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
  AND NOT u.is_hidden -- added
  ORDER BY p.updated;

COMMIT;
