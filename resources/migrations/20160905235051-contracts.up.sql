ALTER TABLE public.issues
  ADD transaction_hash VARCHAR(128) NULL;
ALTER TABLE public.issues
  ADD contract_address VARCHAR(42) NULL;
ALTER TABLE public.issues
  ADD confirm_hash VARCHAR(128) NULL;
ALTER TABLE public.issues
  ADD comment_id INTEGER NULL;
ALTER TABLE public.issues
  ADD execute_hash VARCHAR(128) NULL;
ALTER TABLE public.issues
  ADD payout_hash VARCHAR(128) NULL;
ALTER TABLE public.issues
  ADD payout_receipt VARCHAR NULL;
-- noinspection SqlResolve
ALTER TABLE public.issues
  DROP COLUMN address;
