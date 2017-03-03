
ALTER TABLE "public"."issue_comment"
ADD COLUMN "comment_hash" varchar(64);

ALTER TABLE "public"."issue_comment"
ADD UNIQUE ("comment_hash");

ALTER TABLE "public"."issue_comment"
DROP CONSTRAINT "issue_comment_issue_id_key";

create unique index idx_issue_comment_issue_id_comment_hash
on issue_comment (issue_id, comment_hash);

ALTER TABLE "public"."repositories"
RENAME COLUMN "login" TO "owner";
