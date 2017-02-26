ALTER TABLE "public"."issues" ADD COLUMN "updated" timestamp without time zone DEFAULT timezone('utc'::text, now());

ALTER TABLE "public"."issues" RENAME COLUMN "commit_id" TO "commit_sha";

ALTER TABLE "public"."pull_requests" RENAME COLUMN "commit_id" TO "commit_sha";
