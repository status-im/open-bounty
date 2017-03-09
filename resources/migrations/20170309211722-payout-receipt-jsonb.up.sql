drop view if exists activity_feed_view cascade;
drop view if exists claims_view cascade;
drop view if exists bounties_view cascade;

ALTER TABLE "public"."issues" ALTER COLUMN "payout_receipt" TYPE jsonb USING "payout_receipt"::jsonb;

create view claims_view as
SELECT i.title AS issue_title,
    i.issue_number,
    r.repo AS repo_name,
    r.owner AS repo_owner,
    u.name AS user_name,
    u.avatar_url AS user_avatar_url,
    i.payout_receipt,
    p.updated,
    i.balance,
    p.state AS pr_state
   FROM issues i,
    users u,
    repositories r,
    pull_requests p
  WHERE r.repo_id = i.repo_id AND p.issue_id = i.issue_id AND p.user_id = u.id AND i.contract_address IS NOT NULL AND i.comment_id IS NOT NULL
  ORDER BY p.updated;


create view bounties_view as
SELECT i.title AS issue_title,
    i.issue_number,
    r.repo AS repo_name,
    r.owner AS repo_owner,
    u.name AS user_name,
    u.avatar_url AS user_avatar_url,
    i.payout_receipt,
    i.balance,
    i.updated
   FROM issues i,
    users u,
    repositories r
  WHERE r.repo_id = i.repo_id AND r.user_id = u.id AND i.contract_address IS NOT NULL AND i.comment_id IS NOT NULL
  ORDER BY i.updated;


create view activity_feed_view as
SELECT 'open-claim'::text AS type,
    claims_view.issue_title,
    claims_view.repo_name,
    claims_view.repo_owner,
    claims_view.issue_number,
    claims_view.user_name,
    claims_view.user_avatar_url,
    claims_view.balance,
    claims_view.updated
   FROM claims_view
  WHERE claims_view.pr_state = 0 AND claims_view.payout_receipt IS NULL
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
