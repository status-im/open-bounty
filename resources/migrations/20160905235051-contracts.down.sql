-- noinspection SqlResolveForFile
ALTER TABLE public.issues
  DROP COLUMN transaction_hash;
ALTER TABLE public.issues
  DROP COLUMN contract_address;
ALTER TABLE public.issues
  ADD address VARCHAR(256);
