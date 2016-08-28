-- Users ---------------------------------------------------------------------------

-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, login, name, email, token, address, created)
  SELECT
    :id,
    :login,
    :name,
    :email,
    :token,
    :address,
    :created
  WHERE NOT exists(SELECT 1
                   FROM users
                   WHERE id = :id);

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET login = :login, name = :name, email = :email, token = :token, address = :address
WHERE id = :id;

-- :name update-user-token! :<! :1
-- :doc updates user token and returns updated user
UPDATE users
SET token = :token
WHERE id = :id
RETURNING id, login, name, email, token, address, created;

-- :name update-user-address! :! :n
UPDATE users
SET address = :address
WHERE id = :id;

-- :name get-user :? :1
-- :doc retrieve a user given the login.
SELECT *
FROM users
WHERE id = :id;

-- Repositories --------------------------------------------------------------------

-- :name toggle-repository! :<! :1
-- :doc toggles 'enabled' flag of a given repository
UPDATE repositories
SET enabled = NOT enabled
WHERE repo_id = :repo_id
RETURNING repo_id, login, repo, enabled, hook_id;

-- :name create-repository! :<! :1
-- :doc creates repository if not exists
INSERT INTO repositories (repo_id, login, repo, enabled)
  SELECT
    :repo_id,
    :login,
    :repo,
    :enabled
  WHERE NOT exists(SELECT 1
                   FROM repositories
                   WHERE repo_id = :repo_id)
RETURNING repo_id, login, repo, enabled;

-- :name get-enabled-repositories :? :*
-- :doc returns enabled repositories for a given login
SELECT repo_id
FROM repositories
WHERE user_id = :user_id AND enabled = TRUE;

-- :name update-hook-id :! :n
-- :doc updates hook_id of a specified repository
UPDATE repositories
SET hook_id = :hook_id
WHERE repo_id = :repo_id;

-- Issues --------------------------------------------------------------------------

-- :name create-issue! :! :n
-- :doc creates issue
INSERT INTO issues (repo_id, issue_id, issue_number, address)
  SELECT
    :repo_id,
    :issue_id,
    :issue_number,
    :address
  WHERE NOT exists(SELECT 1
                   FROM issues
                   WHERE repo_id = :repo_id AND issue_id = :issue_id);

-- :name close-issue! :<! :1
-- :doc updates issue with commit id
UPDATE issues
SET commit_id = :commit_id
WHERE issue_id = :issue_id
RETURNING repo_id, issue_id, issue_number, address, commit_id;

-- Pull Requests -------------------------------------------------------------------

-- :name create-pull-request! :! :n
-- :doc creates pull request
INSERT INTO pull_requests (repo_id, pr_id, user_id, parents)
  SELECT
    :repo_id,
    :pr_id,
    :user_id,
    :parents
  WHERE NOT exists(SELECT 1
                   FROM pull_requests
                   WHERE repo_id = :repo_id AND pr_id = :pr_id);

-- Bounties ------------------------------------------------------------------------

-- :name bounties-list :? :*
-- :doc lists fixed issues
SELECT
  i.address AS issue_address,
  i.repo_id AS repo_id,
  p.pr_id   AS pr_id,
  p.user_id AS user_id,
  u.address AS payout_address,
  u.login   AS user_login,
  u.name    AS user_name,
  r.repo    AS repo_name
FROM issues i
  INNER JOIN pull_requests p
    ON p.parents LIKE '%' || i.commit_id || '%'
       AND p.repo_id = i.repo_id
  INNER JOIN users u
    ON u.id = p.user_id
  INNER JOIN repositories r
    ON r.repo_id = i.repo_id
WHERE r.user_id = :owner_id;
