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

-- :name get-repo-owner :? :1
SELECT *
FROM users u
  INNER JOIN repositories r ON r.user_id = u.id
WHERE r.repo_id = :repo_id;

-- Repositories --------------------------------------------------------------------

-- :name toggle-repository! :<! :1
-- :doc toggles 'enabled' flag of a given repository
UPDATE repositories
SET enabled = NOT enabled
WHERE repo_id = :repo_id
RETURNING repo_id, login, repo, enabled, hook_id;

-- :name create-repository! :<! :1
-- :doc creates repository if not exists
INSERT INTO repositories (repo_id, user_id, login, repo, enabled)
  SELECT
    :repo_id,
    :user_id,
    :login,
    :repo,
    :enabled
  WHERE NOT exists(SELECT 1
                   FROM repositories
                   WHERE repo_id = :repo_id)
RETURNING repo_id, user_id, login, repo, enabled;

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
INSERT INTO issues (repo_id, issue_id, issue_number, title)
  SELECT
    :repo_id,
    :issue_id,
    :issue_number,
    :title
  WHERE NOT exists(SELECT 1
                   FROM issues
                   WHERE repo_id = :repo_id AND issue_id = :issue_id);

-- :name close-issue! :<! :1
-- :doc updates issue with commit id
UPDATE issues
SET commit_id = :commit_id
WHERE issue_id = :issue_id
RETURNING repo_id, issue_id, issue_number, title, commit_id, contract_address;

-- :name update-transaction-hash :! :n
-- :doc updates transaction-hash for a given issue
UPDATE issues
SET transaction_hash = :transaction_hash
WHERE issue_id = :issue_id;

-- :name update-contract-address :<! :1
-- :doc updates contract-address for a given issue
WITH t AS (
    SELECT
      i.issue_id         AS issue_id,
      i.issue_number     AS issue_number,
      i.title            AS title,
      i.transaction_hash AS transaction_hash,
      i.contract_address AS contract_address,
      i.repo_id          AS repo_id,
      r.login            AS login,
      r.repo             AS repo
    FROM issues i
      INNER JOIN repositories r ON r.repo_id = i.repo_id
    WHERE i.issue_id = :issue_id
)
UPDATE issues i
SET contract_address = :contract_address
FROM t
RETURNING t.issue_id, t.issue_number, t.title, t.transaction_hash, t.contract_address, t.login, t.repo, t.repo_id;

-- :name update-comment-id :! :n
-- :doc updates comment-id for a given issue
UPDATE issues
SET comment_id = :comment_id
WHERE issue_id = :issue_id;

-- :name list-pending-deployments :? :*
-- :doc retrieves pending transaction ids
SELECT
  issue_id,
  transaction_hash
FROM issues
WHERE contract_address IS NULL;

-- Pull Requests -------------------------------------------------------------------

-- :name create-pull-request! :! :n
-- :doc creates pull request
INSERT INTO pull_requests (repo_id, pr_id, pr_number, issue_number, commit_id, user_id)
  SELECT
    :repo_id,
    :pr_id,
    :pr_number,
    :issue_number,
    :commit_id,
    :user_id
  WHERE NOT exists(SELECT 1
                   FROM pull_requests
                   WHERE repo_id = :repo_id AND pr_id = :pr_id);

-- Bounties ------------------------------------------------------------------------

-- :name pending-bounties-list :? :*
-- :doc lists all recently closed issues awaiting to be signed
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  u.address          AS payout_address
FROM issues i
  INNER JOIN pull_requests p
    ON (p.commit_id = i.commit_id OR coalesce(p.issue_number, -1) = i.issue_number)
       AND p.repo_id = i.repo_id
  INNER JOIN users u
    ON u.id = p.user_id
WHERE i.confirm_hash IS NULL;

-- :name update-confirm-hash :! :n
-- :doc updates issue with transaction hash
UPDATE issues
SET confirm_hash = :confirm_hash
WHERE issue_id = :issue_id;

-- :name bounties-list :? :*
-- :doc lists fixed issues
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  i.issue_number     AS issue_number,
  i.title            AS issue_title,
  i.repo_id          AS repo_id,
  p.pr_id            AS pr_id,
  p.user_id          AS user_id,
  p.pr_number        AS pr_number,
  u.address          AS payout_address,
  u.login            AS user_login,
  u.name             AS user_name,
  r.login            AS owner_name,
  r.repo             AS repo_name
FROM issues i
  INNER JOIN pull_requests p
    ON (p.commit_id = i.commit_id OR coalesce(p.issue_number, -1) = i.issue_number)
       AND p.repo_id = i.repo_id
  INNER JOIN users u
    ON u.id = p.user_id
  INNER JOIN repositories r
    ON r.repo_id = i.repo_id
WHERE r.user_id = :owner_id;

-- :name owner-issues-list :? :*
-- :doc lists all not yet fixed issues in a given owner's repository
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  i.issue_number     AS issue_number,
  i.title            AS issue_title,
  i.repo_id          AS repo_id,
  r.login            AS owner_name,
  r.repo             AS repo_name
FROM issues i
  INNER JOIN repositories r
    ON r.repo_id = i.repo_id
WHERE r.user_id = :owner_id
      AND i.commit_id IS NULL
      AND NOT exists(SELECT 1
                     FROM pull_requests
                     WHERE issue_number = i.issue_number);

-- :name wallets-list :? :*
-- :doc lists all contract ids
SELECT
  i.contract_address AS contract_address,
  r.login            AS login,
  r.repo             AS repo,
  i.comment_id       AS comment_id,
  i.issue_number     AS issue_number
FROM issues i
  INNER JOIN repositories r ON r.repo_id = i.repo_id
WHERE contract_address IS NOT NULL;

-- :name get-bounty-address :? :1
SELECT
  i.contract_address AS contract_address,
  i.issue_number     AS issue_number,
  r.login            AS login,
  r.repo             AS repo
FROM issues i
  INNER JOIN repositories r ON r.repo_id = i.repo_id
WHERE i.issue_number = :issue_number
      AND r.login = :login AND r.repo = :repo;

-- :name get-balance :? :1
-- :doc gets current balance of a wallet attached to a given issue
SELECT balance
FROM issues
WHERE contract_address = :contract_address;

-- :name update-banlance :! :n
-- :doc updates balance of a wallet attached to a given issue
UPDATE issues
SET balance = :balance
WHERE contract_address = :contract_address;
