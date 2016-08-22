-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, login, name, email, token, address, created)
VALUES (:id, :login, :name, :email, :token, :address, :created);

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET login = :login, name = :name, email = :email, token = :token, address = :address
WHERE id = :id;

-- :name update-user-address! :! :n
UPDATE users
SET address = :address
WHERE login = :login;

-- :name get-user :? :1
-- :doc retrieve a user given the login.
SELECT *
FROM users
WHERE login = :login;

