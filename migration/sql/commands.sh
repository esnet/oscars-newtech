#!/usr/bin/env bash


dump_cmd = "pg_dump -f oscars.sql.bak -C -d oscars_backend"

restore_command = "psql < oscars.sql.bak"

drop_everything = "dropdb oscars_backend"

import = "psql -d oscars_backend -f timestamps.sql"