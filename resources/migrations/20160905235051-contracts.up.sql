ALTER TABLE public.issues
  ADD transaction_hash VARCHAR(128) NULL;
ALTER TABLE public.issues
  ADD contract_address VARCHAR(42) NULL;
ALTER TABLE public.issues
  ADD confirm_hash VARCHAR(128) NULL;
ALTER TABLE public.issues
  ADD comment_id INTEGER NULL;
-- noinspection SqlResolve
ALTER TABLE public.issues
  DROP COLUMN address;
