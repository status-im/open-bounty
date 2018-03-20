CREATE TYPE data_enum AS ENUM (
  'issue', 
  'issue_comment', 
  'repository', 
  'pull_request', 
  'user');

CREATE TABLE archive (
  type data_enum,
  created_at TIMESTAMP DEFAULT now(),
  data JSONB
);

