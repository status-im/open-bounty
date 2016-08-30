ALTER TABLE pull_requests
  ADD COLUMN parents VARCHAR(4099);

-- noinspection SqlResolve
ALTER TABLE pull_requests
  DROP COLUMN commit_id;

-- noinspection SqlResolve
ALTER TABLE pull_requests
  DROP COLUMN issue_number;
