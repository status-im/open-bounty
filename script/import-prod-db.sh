#!/usr/bin/env bash

set -eou pipefail

dump_location="commiteth:db-backups/2018-03-28/voe0Eepeid.sql.gz"
local_modified="$(mktemp -t proddb-commiteth.sql)"

if createdb commiteth; then
  echo "Downloading DB dump from $dump_location"
  scp "$dump_location" /dev/stdout \
    | gzip -d \
    | sed 's/$PRODDB/commiteth/g' \
    | sed -E 's/0x[a-z0-9]{40,40}/0xREDACTED/g' \
    > $local_modified

  psql commiteth < $local_modified
  echo -e "\nImported DB dump from $local_modified"
else
  echo -e "\ncommiteth database exists already. Please delete first."
fi
