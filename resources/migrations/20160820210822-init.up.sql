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
  issue_id     INTEGER PRIMARY KEY  NOT NULL,
  repo_id      INTEGER              NOT NULL,
  address      VARCHAR(256),
  issue_number INTEGER,
  commit_id    VARCHAR(40)
);

CREATE TABLE pull_requests
(
  pr_id   INTEGER PRIMARY KEY  NOT NULL,
  repo_id INTEGER,
  user_id INTEGER,
  parents VARCHAR(4099) -- 100 commit SHAs + 99 commas
);
