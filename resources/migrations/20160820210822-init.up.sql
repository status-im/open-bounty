CREATE TABLE users
(
  id      INTEGER PRIMARY KEY  NOT NULL,
  login   VARCHAR(64)          NOT NULL,
  name    VARCHAR(128),
  email   VARCHAR(128),
  token   VARCHAR(40),
  address VARCHAR(42),
  created TIME
);

CREATE TABLE repositories
(
  repo_id INTEGER PRIMARY KEY  NOT NULL,
  user_id INTEGER,
  login   VARCHAR(64)          NOT NULL,
  repo    VARCHAR(64)          NOT NULL,
  updated TIME,
  enabled BOOLEAN DEFAULT TRUE,
  hook_id INTEGER
);

CREATE TABLE issues
(
  repo_id      INTEGER NOT NULL,
  issue_id     INTEGER NOT NULL,
  issue_number INTEGER,
  title        VARCHAR(256),
  address      VARCHAR(256),
  commit_id    VARCHAR(40),
  CONSTRAINT issues_repo_id_issue_id_pk PRIMARY KEY (repo_id, issue_id)
);

CREATE TABLE pull_requests
(
  pr_id     INTEGER PRIMARY KEY  NOT NULL,
  pr_number INTEGER,
  repo_id   INTEGER,
  user_id   INTEGER,
  parents   VARCHAR(4099) -- 100 commit SHAs + 99 commas
);
