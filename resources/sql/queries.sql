-- Users ---------------------------------------------------------------------------

-- :name create-user! :<! :1
-- :doc creates a new user record
INSERT INTO users
(id, login, name, email, avatar_url, token, address, created)
SELECT
  :id,
  :login,
  :name,
  :email,
  :avatar_url,
  :token,
  :address,
  :created
WHERE NOT exists(SELECT 1
                 FROM users
                 WHERE id = :id)
RETURNING id, login, name, email, avatar_url, token, address, created;

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET login = :login,
name = :name,
email = :email,
token = :token,
address = :address
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
FROM users u, repositories r
WHERE r.repo_id = :repo_id
AND r.user_id = u.id;

-- Repositories --------------------------------------------------------------------

-- :name set-repo-state! :<! :1
-- :doc sets repository to given state
UPDATE repositories
SET state = :state
WHERE repo_id = :repo_id
RETURNING repo_id, login, repo, state, hook_id;


-- :name get-repo :? :1
-- :doc retrieve a repository given login and repo-name
SELECT *
FROM repositories
WHERE login = :login
AND repo = :repo;


-- :name create-repository! :<! :1
-- :doc creates repository if not exists
INSERT INTO repositories (repo_id, user_id, login, repo, state)
  SELECT
    :repo_id,
    :user_id,
    :login,
    :repo,
    :state
  WHERE NOT exists(SELECT 1
                   FROM repositories
                   WHERE repo_id = :repo_id)
RETURNING repo_id, user_id, login, repo, state;

-- :name get-enabled-repositories :? :*
-- :doc returns enabled repositories for a given login
SELECT repo_id
FROM repositories
WHERE user_id = :user_id
AND state = 2;


-- :name update-repo-generic :! :n
/* :require [clojure.string :as string]
            [hugsql.parameters :refer [identifier-param-quote]] */
UPDATE repositories
SET
/*~
(string/join ","
  (for [[field _] (:updates params)]
    (str (identifier-param-quote (name field) options)
      " = :v:updates." (name field))))
~*/
where repo_id = :repo_id;



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

-- :name update-commit-id :<! :1
-- :doc updates issue with commit_id
UPDATE issues
SET commit_id = :commit_id
WHERE issue_id = :issue_id
RETURNING repo_id, issue_id, issue_number, title, commit_id, contract_address;

-- :name update-transaction-hash :! :n
-- :doc updates transaction-hash for a given issue
UPDATE issues
SET transaction_hash = :transaction_hash
WHERE issue_id = :issue_id;


-- TODO: this is terrible
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
    FROM issues i, repositories r
    WHERE r.repo_id = i.repo_id
    AND i.issue_id = :issue_id
)
UPDATE issues i
SET contract_address = :contract_address
FROM t
WHERE i.issue_id = :issue_id
RETURNING t.issue_id, t.issue_number, t.title, t.transaction_hash, i.contract_address, t.login, t.repo, t.repo_id;

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
WHERE contract_address IS NULL
AND issues.transaction_hash IS NOT NULL;

-- Pull Requests -------------------------------------------------------------------

-- :name save-pull-request! :! :n
-- :doc inserts or updates a pull request record
INSERT INTO pull_requests (pr_id,
  repo_id,
  pr_number,
  issue_number,
  issue_id,
  commit_id,
  user_id,
  state)
VALUES(:pr_id,
  :repo_id,
  :pr_number,
  :issue_number,
  :issue_id,
  :commit_id,
  :user_id,
  :state)
ON CONFLICT (pr_id) DO UPDATE
SET
  state = :state,
  updated = timezone('utc'::text, now()),
  commit_id = :commit_id;

-- Bounties ------------------------------------------------------------------------

-- :name pending-bounties-list :? :*
-- :doc lists all recently closed issues awaiting to be signed
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  u.address          AS payout_address
FROM issues i, pull_requests p, users u
WHERE
p.issue_id = i.issue_id
AND p.repo_id = i.repo_id
AND u.id = p.user_id
AND i.execute_hash IS NULL;

-- :name pending-payouts-list :? :*
-- :doc lists all recently closed issues awaiting to be confirmed
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  u.address          AS payout_address,
  i.execute_hash     AS execute_hash
FROM issues i, pull_requests p, users u
WHERE
p.issue_id = i.issue_id
AND p.repo_id = i.repo_id
AND u.id = p.user_id
AND i.confirm_hash IS NULL
AND i.execute_hash IS NOT NULL;

-- :name confirmed-payouts-list :? :*
-- :doc lists all recently confirmed bounty payouts
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  u.address          AS payout_address,
  i.payout_hash     AS payout_hash
FROM issues i, pull_requests p, users u
WHERE
p.issue_id = i.issue_id
AND p.repo_id = i.repo_id
AND u.id = p.user_id
AND i.payout_receipt IS NULL
AND i.payout_hash IS NOT NULL;

-- :name update-confirm-hash :! :n
-- :doc updates issue with confirmation hash
UPDATE issues
SET confirm_hash = :confirm_hash
WHERE issue_id = :issue_id;

-- :name update-execute-hash :! :n
-- :doc updates issue with execute transaction hash
UPDATE issues
SET execute_hash = :execute_hash
WHERE issue_id = :issue_id;

-- :name update-payout-hash :! :n
-- :doc updates issue with payout transaction hash
UPDATE issues
SET payout_hash = :payout_hash
WHERE issue_id = :issue_id;

-- :name update-payout-receipt :! :n
-- :doc updates issue with payout transaction receipt
UPDATE issues
SET payout_receipt = :payout_receipt
WHERE issue_id = :issue_id;

-- :name all-bounties-list :? :*
-- :doc open (not merged) bounty issues
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  i.issue_number     AS issue_number,
  i.title            AS issue_title,
  i.repo_id          AS repo_id,
  i.balance          AS issue_balance,
  r.login            AS owner_name,
  r.repo             AS repo_name
FROM issues i, repositories r
WHERE
r.repo_id = i.repo_id
AND i.commit_id IS NULL;

-- :name owner-bounties-list :? :*
-- :doc all bounty issues for given owner
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  i.issue_number     AS issue_number,
  i.title            AS issue_title,
  i.repo_id          AS repo_id,
  i.balance          AS balance,
  i.confirm_hash     AS confirm_hash,
  i.payout_hash      AS payout_hash,
  i.payout_receipt   AS payout_receipt,
  r.repo             AS repo_name,
  o.address          AS owner_address
FROM issues i, users o, repositories r
WHERE
r.repo_id = i.repo_id
AND r.user_id = o.id
AND r.user_id = :owner_id;
-- AND i.confirm_hash IS NOT NULL;


-- :name bounty-claims :? :*
-- :doc open, merged and closed PRs referencing given bounty issue
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  i.issue_number     AS issue_number,
  i.title            AS issue_title,
  i.repo_id          AS repo_id,
  i.balance          AS balance,
  i.confirm_hash     AS confirm_hash,
  i.payout_hash      AS payout_hash,
  i.payout_receipt   AS payout_receipt,
  p.state            AS pr_state,
  p.pr_id            AS pr_id,
  p.user_id          AS user_id,
  p.pr_number        AS pr_number,
  u.address          AS payout_address,
  u.login            AS user_login,
  u.name             AS user_name,
  u.avatar_url       AS user_avatar_url,
  r.login            AS owner_login,
  r.repo             AS repo_name,
  o.address          AS owner_address
FROM issues i, pull_requests p, users u, users o, repositories r
WHERE
p.issue_id = i.issue_id
AND p.repo_id = i.repo_id
AND u.id = p.user_id
AND r.repo_id = i.repo_id
AND r.user_id = o.id
AND i.issue_id = :issue_id;
--AND i.confirm_hash IS NOT NULL;



-- :name owner-issues-list :? :*
-- :doc owner's bounty issues with no merged PR
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  i.issue_number     AS issue_number,
  i.title            AS issue_title,
  i.repo_id          AS repo_id,
  r.login            AS owner_name,
  r.repo             AS repo_name
FROM issues i, repositories r
WHERE r.repo_id = i.repo_id
AND r.user_id = :owner_id
AND i.commit_id IS NULL
AND NOT exists(SELECT 1
               FROM pull_requests
               WHERE issue_number = i.issue_number
               AND state = 1);


-- :name open-bounty-contracts :? :*
-- :doc bounty issues with mined bounty contracts
SELECT
  i.contract_address AS contract_address,
  r.login            AS login,
  r.repo             AS repo,
  i.comment_id       AS comment_id,
  i.issue_number     AS issue_number,
  i.issue_id         AS issue_id,
  i.balance          AS balance
FROM issues i, repositories r
WHERE r.repo_id = i.repo_id
AND contract_address IS NOT NULL
AND i.payout_hash IS NULL;

-- :name get-bounty :? :1
-- :doc details for a bounty issue given owner, repo and issue nunber
SELECT
  i.contract_address AS contract_address,
  i.issue_id         AS issue_id,
  i.issue_number     AS issue_number,
  i.balance          AS balance,
  r.login            AS login,
  r.repo             AS repo
FROM issues i, repositories r
WHERE i.issue_number = :issue_number
AND r.repo_id = i.repo_id
AND r.login = :login
AND r.repo = :repo;

-- :name get-balance :? :1
-- :doc gets current balance of a wallet attached to a given issue
SELECT balance
FROM issues
WHERE contract_address = :contract_address;

-- :name update-balance :! :n
-- :doc updates balance of a wallet attached to a given issue
UPDATE issues
SET balance = :balance
WHERE contract_address = :contract_address;


-- :name save-issue-comment-image! :<! :1
-- :doc insert or update image data for a given issue's github comment
INSERT INTO issue_comment (issue_id, png_data)
VALUES (:issue_id, :png_data)
ON CONFLICT (issue_id) DO UPDATE
SET png_data = :png_data
RETURNING id;


-- :name get-issue-comment-image :? :1
-- :doc retrieve image data for given issue's github comment
SELECT png_data
FROM issue_comment
WHERE issue_id = :issue_id;
