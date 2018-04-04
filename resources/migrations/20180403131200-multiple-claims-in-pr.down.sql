ALTER TABLE pull_requests DROP CONSTRAINT pull_requests_pkey;
ALTER TABLE pull_requests DROP CONSTRAINT pull_requests_pr_id_key;
ALTER TABLE pull_requests DROP CONSTRAINT pull_requests_fkey;


ALTER TABLE pull_requests ADD CONSTRAINT pull_requests_pkey PRIMARY KEY (pr_id);
ALTER TABLE pull_requests ADD CONSTRAINT pull_requests_pr_id_key UNIQUE (pr_id);
