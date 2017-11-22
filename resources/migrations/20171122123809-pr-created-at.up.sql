ALTER TABLE "public"."pull_requests" ADD COLUMN "created_at" timestamp without time zone DEFAULT timezone('utc'::text, now());
