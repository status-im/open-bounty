-- noinspection SqlResolve
ALTER TABLE public.pull_requests
  DROP parents;

ALTER TABLE public.pull_requests
  ADD COLUMN commit_id VARCHAR(40);

ALTER TABLE public.pull_requests
  ADD COLUMN issue_number INTEGER;
