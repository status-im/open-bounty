ALTER TABLE "public"."pull_requests"
  ADD COLUMN "updated" timestamp without time zone DEFAULT timezone('utc'::text, now()),
  ADD COLUMN "issue_id" integer,
  ADD UNIQUE ("pr_id");
