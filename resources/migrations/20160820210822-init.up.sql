CREATE TABLE users (
  id      VARCHAR(20) PRIMARY KEY, -- user id
  login   VARCHAR(64) UNIQUE NOT NULL, -- github login
  name    VARCHAR(128), -- user name
  email   VARCHAR(128), -- user email, if present
  token   VARCHAR(40)        NOT NULL, -- github oauth token
  address VARCHAR(42), -- ETH address
  created TIME -- user created date
);

CREATE TABLE repositories (
  login   VARCHAR(64) NOT NULL, -- github user
  repo    VARCHAR(64) NOT NULL, -- github repo
  updated TIME, -- date of the last crawl
  CONSTRAINT repositories_user_repo_pk PRIMARY KEY (login, repo) -- composite primary key
);
