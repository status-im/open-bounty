begin;

alter table users
    drop column is_hidden;

commit;
