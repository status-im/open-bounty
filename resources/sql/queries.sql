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
-- :doc retrieve a user given the user-id.
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
RETURNING repo_id, owner, repo, state, hook_id;


-- :name get-repo :? :1
-- :doc retrieve a repository given owner and repo-name
SELECT *
FROM repositories
WHERE owner = :owner
AND repo = :repo;


-- :name create-repository! :<! :1
-- :doc creates repository if not exists
INSERT INTO repositories (repo_id, user_id, owner, repo, state, owner_avatar_url)
  SELECT
    :repo_id,
    :user_id,
    :owner,
    :repo,
    :state,
    :owner_avatar_url
  WHERE NOT exists(SELECT 1
                   FROM repositories
                   WHERE repo_id = :repo_id)
RETURNING repo_id, user_id, owner, repo, state, owner_avatar_url;

-- :name get-enabled-repositories :? :*
-- :doc returns enabled repositories for a given user-id
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

-- :name update-commit-sha :<! :1
-- :doc updates issue with commit_sha
UPDATE issues
SET commit_sha = :commit_sha,
updated = timezone('utc'::text, now())
WHERE issue_id = :issue_id
RETURNING repo_id, issue_id, issue_number, title, commit_sha, contract_address;

-- :name update-transaction-hash :! :n
-- :doc updates transaction-hash for a given issue
UPDATE issues
SET transaction_hash = :transaction_hash,
updated = timezone('utc'::text, now())
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
      i.comment_id       AS comment_id,
      i.repo_id          AS repo_id,
      r.owner            AS owner,
      r.repo             AS repo
    FROM issues i, repositories r
    WHERE r.repo_id = i.repo_id
    AND i.issue_id = :issue_id
)
UPDATE issues i
SET contract_address = :contract_address,
updated = timezone('utc'::text, now())
FROM t
WHERE i.issue_id = :issue_id
RETURNING t.issue_id, t.issue_number, t.title, t.transaction_hash, t.comment_id, i.contract_address, t.owner, t.repo, t.repo_id;

-- :name update-comment-id :! :n
-- :doc updates comment-id for a given issue
UPDATE issues
SET comment_id = :comment_id,
updated = timezone('utc'::text, now())
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
  commit_sha,
  user_id,
  state)
VALUES(:pr_id,
  :repo_id,
  :pr_number,
  :issue_number,
  :issue_id,
  :commit_sha,
  :user_id,
  :state)
ON CONFLICT (pr_id) DO UPDATE
SET
  state = :state,
  updated = timezone('utc'::text, now()),
  commit_sha = :commit_sha;

-- Bounties ------------------------------------------------------------------------


-- :name pending-contracts :? :*
-- :doc bounty issues where deploy contract has failed
SELECT
  i.issue_id         AS issue_id,
  u.address          AS owner_address
FROM issues i, users u, repositories r
WHERE
r.user_id = u.id
AND i.repo_id = r.repo_id
AND i.transaction_hash IS NULL
AND i.contract_address IS NULL;


-- :name pending-bounties :? :*
-- :doc bounties with merged pull-requests awaiting to be signed
SELECT
  i.contract_address AS contract_address,
  r.owner            AS owner,
  r.repo             AS repo,
  i.comment_id       AS comment_id,
  i.issue_number     AS issue_number,
  i.issue_id         AS issue_id,
  i.balance          AS balance,
  u.login            AS winner_login,
  u.address          AS payout_address
FROM issues i, pull_requests p, users u, repositories r
WHERE
p.issue_id = i.issue_id
AND p.repo_id = i.repo_id
AND p.commit_sha = i.commit_sha
AND r.repo_id = i.repo_id
AND u.id = p.user_id
AND i.execute_hash IS NULL;

-- :name pending-payouts :? :*
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

-- :name confirmed-payouts :? :*
-- :doc lists all recently confirmed bounty payouts
SELECT
  i.contract_address AS contract_address,
  r.owner            AS owner,
  r.repo             AS repo,
  i.comment_id       AS comment_id,
  i.issue_number     AS issue_number,
  i.issue_id         AS issue_id,
  i.balance          AS balance,
  u.address          AS payout_address,
  u.login           AS payee_login,
  i.payout_hash     AS payout_hash
FROM issues i, pull_requests p, users u, repositories r
WHERE
p.issue_id = i.issue_id
AND p.repo_id = i.repo_id
AND r.repo_id = i.repo_id
AND u.id = p.user_id
AND i.payout_receipt IS NULL
AND i.payout_hash IS NOT NULL;

-- :name update-confirm-hash :! :n
-- :doc updates issue with confirmation hash
UPDATE issues
SET confirm_hash = :confirm_hash,
updated = timezone('utc'::text, now())
WHERE issue_id = :issue_id;

-- :name update-execute-hash :! :n
-- :doc updates issue with execute transaction hash
UPDATE issues
SET execute_hash = :execute_hash,
updated = timezone('utc'::text, now())
WHERE issue_id = :issue_id;

-- :name update-payout-hash :! :n
-- :doc updates issue with payout transaction hash
UPDATE issues
SET payout_hash = :payout_hash,
updated = timezone('utc'::text, now())
WHERE issue_id = :issue_id;

-- :name update-payout-receipt :! :n
-- :doc updates issue with payout transaction receipt
UPDATE issues
SET payout_receipt = :payout_receipt::jsonb,
updated = timezone('utc'::text, now())
WHERE issue_id = :issue_id;


-- :name open-bounties :? :*
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
  i.updated          AS updated,
  r.owner            AS repo_owner,
  r.owner_avatar_url AS repo_owner_avatar_url,
  r.repo             AS repo_name
FROM issues i, repositories r
WHERE
r.repo_id = i.repo_id
AND i.confirm_hash is null
ORDER BY balance desc, updated desc;



-- :name owner-bounties :? :*
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
  i.updated          AS updated,
  r.repo             AS repo_name,
  o.address          AS owner_address
FROM issues i, users o, repositories r
WHERE
r.repo_id = i.repo_id
AND r.user_id = o.id
AND r.user_id = :owner_id;


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
  r.owner            AS repo_owner,
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


-- :name open-bounty-contracts :? :*
-- :doc bounty issues with mined bounty contracts
SELECT
  i.contract_address AS contract_address,
  r.owner            AS owner,
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
  r.owner            AS owner,
  r.repo             AS repo
FROM issues i, repositories r
WHERE i.issue_number = :issue_number
AND r.repo_id = i.repo_id
AND r.owner = :owner
AND r.repo = :repo;

-- :name get-balance :? :1
-- :doc gets current balance of a wallet attached to a given issue
SELECT balance
FROM issues
WHERE contract_address = :contract_address;

-- :name update-balance :! :n
-- :doc updates balance of a wallet attached to a given issue
UPDATE issues
SET balance = :balance,
updated = timezone('utc'::text, now())
WHERE contract_address = :contract_address;


-- :name save-issue-comment-image! :<! :1
-- :doc insert or update image data for a given issue's github comment
INSERT INTO issue_comment (issue_id, comment_hash, png_data)
VALUES (:issue_id, :hash, :png_data)
ON CONFLICT (issue_id, comment_hash) DO UPDATE
SET png_data = :png_data
RETURNING id;


-- :name get-issue-comment-image :? :1
-- :doc retrieve image data for given issue's github comment
SELECT png_data
FROM issue_comment
WHERE issue_id = :issue_id
AND comment_hash = :hash;


-- :name top-hunters :? :*
-- :doc list of user that have reveived bounty payouts with sum of
-- earnings
SELECT
u.id AS user_id,
u.login AS login,
u.name AS user_name,
u.avatar_url AS avatar_url,
SUM(i.balance) AS total_eth
FROM issues i, users u, pull_requests pr
WHERE
pr.commit_sha = i.commit_sha
AND u.id = pr.user_id
AND i.payout_receipt IS NOT NULL
GROUP BY u.id
ORDER BY total_eth DESC;


-- :name bounties-activity :? :*
-- :doc data for bounty activity feed
SELECT
  type,
  issue_title,
  repo_name,
  repo_owner,
  issue_number,
  user_name,
  user_avatar_url,
  balance,
  updated
FROM activity_feed_view
ORDER BY updated DESC
LIMIT 100;
