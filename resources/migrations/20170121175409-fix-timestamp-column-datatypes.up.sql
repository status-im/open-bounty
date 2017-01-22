ALTER TABLE public.users ALTER COLUMN created SET DATA TYPE timestamp without time zone using date('20170120') + created;
ALTER TABLE public.repositories ALTER COLUMN updated SET DATA TYPE timestamp without time zone using date('20170120') + updated;
