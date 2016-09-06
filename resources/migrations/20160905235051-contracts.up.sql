ALTER TABLE public.issues
  ADD transaction_hash VARCHAR(128) NULL;
ALTER TABLE public.issues
  ADD contract_address VARCHAR(42) NULL;
-- noinspection SqlResolve
ALTER TABLE public.issues
  DROP COLUMN address;
