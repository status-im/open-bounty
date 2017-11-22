ALTER TABLE "public"."issues" ADD COLUMN "created_at" timestamp without time zone DEFAULT timezone('utc'::text, now());
