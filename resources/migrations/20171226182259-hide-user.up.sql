begin;

alter table users
    add column is_hidden boolean not null default false;

commit;
