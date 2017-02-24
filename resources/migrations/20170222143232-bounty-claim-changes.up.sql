
ALTER TABLE "public"."issue_comment"
ADD COLUMN "updated" timestamp DEFAULT timezone('utc'::text, now());


ALTER TABLE "public"."pull_requests" ADD COLUMN "state" integer DEFAULT '0';

CREATE TABLE "public"."pull_request_state" (
    "id" integer,
    "description" character varying(32),
    PRIMARY KEY ("id")
);

INSERT INTO pull_request_state (id, description)
VALUES(0, 'opened');

INSERT INTO pull_request_state (id, description)
VALUES(1, 'merged');

INSERT INTO pull_request_state (id, description)
VALUES(2, 'closed');
