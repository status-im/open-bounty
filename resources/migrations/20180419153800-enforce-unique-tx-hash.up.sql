ALTER TABLE issues ADD CONSTRAINT transaction_hash_uniq UNIQUE (transaction_hash);
