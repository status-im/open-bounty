
CREATE TABLE "public"."repo_state" (
    "id" int,
    "description" character varying(32) NOT NULL,
    PRIMARY KEY ("id")
);

INSERT INTO "public"."repo_state"("id", "description") VALUES(0, 'Not enabled');
INSERT INTO "public"."repo_state"("id", "description") VALUES(1, 'Creating hook');
INSERT INTO "public"."repo_state"("id", "description") VALUES(2, 'Enabled');
INSERT INTO "public"."repo_state"("id", "description") VALUES(-1, 'Failed to create hook');

ALTER TABLE "public"."repositories" DROP COLUMN "enabled";
ALTER TABLE "public"."repositories"
  ADD COLUMN "state" int NOT NULL DEFAULT 0;
