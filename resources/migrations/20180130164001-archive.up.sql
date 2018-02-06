CREATE TABLE data_types (
  name TEXT UNIQUE
);

INSERT INTO data_types(name)
  VALUES('issue'), 
        ('issue_comment'), 
        ('repository'), 
        ('pull_request'), 
        ('user');

CREATE TABLE archive (
  type TEXT REFERENCES data_types(name),
  created_at TIMESTAMP DEFAULT now(),
  data JSONB
);

