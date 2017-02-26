ALTER TABLE "public"."issues"
  ALTER COLUMN "balance" DROP DEFAULT,
  ALTER COLUMN "balance" SET DATA TYPE decimal USING (balance::numeric),
  ALTER COLUMN "balance" SET DEFAULT 0.0;
