ALTER TABLE "public"."repositories" ADD COLUMN "owner_avatar_url" character varying(255);

create or replace view bounties_view as
select
  i.title as issue_title,
  i.issue_number,
  r.repo as repo_name,
  r.owner as repo_owner,
  concat(r.owner, '/', r.repo)::varchar(128) as user_name,
  r.owner_avatar_url as user_avatar_url,
  i.payout_receipt,
  i.balance,
  i.updated as updated
FROM issues i, repositories r
WHERE r.repo_id = i.repo_id
and contract_address is not null
and comment_id is not null
order by updated;
