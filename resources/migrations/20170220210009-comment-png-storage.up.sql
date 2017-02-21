
-- this column was never used
ALTER TABLE "repositories" DROP COLUMN IF EXISTS "updated";

-- needde for foreign key
ALTER TABLE "issues" ADD UNIQUE ("issue_id");

-- table for github PNG comment images
CREATE TABLE issue_comment (
id SERIAL PRIMARY KEY,
issue_id INTEGER REFERENCES issues (issue_id),
png_data bytea);
