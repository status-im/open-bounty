-- Users ---------------------------------------------------------------------------

-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, login, name, email, token, address, created)
VALUES (:id, :login, :name, :email, :token, :address, :created);

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET login = :login, name = :name, email = :email, token = :token, address = :address
WHERE id = :id;

-- :name update-user-token! :<! :1
-- :doc updates user token and returns updated user
UPDATE users
SET token = :token
WHERE login = :login
RETURNING id, login, name, email, token, address, created;

-- :name update-user-address! :! :n
UPDATE users
SET address = :address
WHERE login = :login;

-- :name get-user :? :1
-- :doc retrieve a user given the login.
SELECT *
FROM users
WHERE login = :login;

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
WHERE login = :login AND enabled = TRUE;

-- :name update-hook-id :! :n
-- :doc updates hook_id of a specified repository
UPDATE repositories
SET hook_id = :hook_id
WHERE repo_id = :repo_id;
