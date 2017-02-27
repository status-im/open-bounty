create view bounties_view as
select
  i.title as issue_title,
  i.issue_number,
  r.repo as repo_name,
  r.login as repo_owner,
  u.name as user_name,
  u.avatar_url as user_avatar_url,
  i.payout_receipt,
  i.balance,
  i.updated as updated
FROM issues i, users u, repositories r
WHERE r.repo_id = i.repo_id
AND r.user_id = u.id
and contract_address is not null
and comment_id is not null
order by updated;

create view claims_view as
select
  i.title as issue_title,
  i.issue_number,
  r.repo as repo_name,
  r.login as repo_owner,
  u.name as user_name,
  u.avatar_url as user_avatar_url,
  i.payout_receipt,
  p.updated as updated,
  i.balance,
  p.state as pr_state
FROM issues i, users u, repositories r, pull_requests p
WHERE r.repo_id = i.repo_id
AND p.issue_id = i.issue_id
AND p.user_id = u.id
and i.contract_address is not null
and i.comment_id is not null
order by p.updated;

create view activity_feed_view as
select 'open-claim' as type,
issue_title,
repo_name,
repo_owner,
issue_number,
user_name,
user_avatar_url,
balance,
updated
from claims_view
where
pr_state=0
and payout_receipt is null
union
select 'claim-payout' as type,
issue_title,
repo_name,
repo_owner,
issue_number,
user_name,
user_avatar_url,
balance,
updated
from claims_view
where
pr_state=1
and payout_receipt is not null
union
select 'new-bounty' as type,
issue_title,
repo_name,
repo_owner,
issue_number,
user_name,
user_avatar_url,
balance,
updated
from bounties_view
where balance=0
and payout_receipt is null
union
select 'balance-update' as type,
issue_title,
repo_name,
repo_owner,
issue_number,
user_name,
user_avatar_url,
balance,
updated
from bounties_view
where balance>0
and payout_receipt is null
order by updated desc;
