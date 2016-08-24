CREATE TABLE users (
  id      VARCHAR(40) PRIMARY KEY, -- user id
  login   VARCHAR(64) UNIQUE NOT NULL, -- github login
  name    VARCHAR(128), -- user name
  email   VARCHAR(128), -- user email, if present
  token   VARCHAR(40)        NOT NULL, -- github oauth token
  address VARCHAR(42), -- ETH address
  created TIME -- user created date
);
CREATE UNIQUE INDEX users_login_key ON users (login);

CREATE TABLE repositories
(
  login   VARCHAR(64) NOT NULL, -- github user
  repo    VARCHAR(64) NOT NULL, -- github repo
  updated TIME, -- date of the last crawl
  repo_id INTEGER     NOT NULL, -- github repository id
  enabled BOOLEAN DEFAULT TRUE
);
CREATE UNIQUE INDEX repositories_user_repo_pk ON repositories (login, repo);
CREATE UNIQUE INDEX repositories_repo_id_pk ON repositories (repo_id);
CREATE INDEX repositories_login_repo_index ON repositories (login, repo);
CREATE INDEX repositories_repo_id_index ON repositories (repo_id);
