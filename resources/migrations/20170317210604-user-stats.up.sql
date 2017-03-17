
drop table if exists usage_metrics;
CREATE TABLE usage_metrics (
    registered_users int,
    users_with_address int,
    change_timestamp timestamp without time zone
 DEFAULT timezone('utc'::text, now()));

drop function if exists store_usage_metrics() cascade;
CREATE OR REPLACE FUNCTION store_usage_metrics() RETURNS TRIGGER AS $usage_metrics$
BEGIN
  insert into usage_metrics (registered_users, users_with_address)
  values ((select count(*) from users),
          (select count(*) from users
          where address is not null));

  return null;
END;
$usage_metrics$ LANGUAGE plpgsql;

CREATE TRIGGER usage_metrics
AFTER INSERT OR UPDATE OR DELETE ON users
execute PROCEDURE store_usage_metrics();
