# Updating OSCARS
This file contains instructions for updating an existing installation of OSCARS from a previous version to a newer version. 

Instructions include config file changes, database schema changes, etc.


## 1.0.39 to 1.0.40
### Config file changes
Logging configuration has been changed. 

#### For backend:
Ensure the RPM has deployed the `config/logback.xml` file.

Then, at the `application.properties` file, remove all `logging.xxxx` and `syslog.xxxx` statements.

Replace those with the following stanza:
```$xslt
logging.config=config/logback.xml
logging.file=/usr/local/esnet/oscars-backend/logs/backend.log
syslog.enable=false
```

To actually enable the syslog logging, set the `syslog.enable` property to `true` and, at `logback.xml`, configure the `syslogHost` 
elements with the correct IP addresses. 

#### For PSS:
Ensure the RPM has deployed the `config/logback.xml` file.

Then, at the `application.properties` file, remove all `logging.xxxx` and `syslog.xxxx` statements.

Replace those with the following stanza:

```$xslt
logging.config=config/logback.xml
logging.file=/usr/local/esnet/oscars-pss/logs/pss.log
```



## 1.0.36 to 1.0.37

### Config file changes
Router config templates have changed; they now include a `@version` tag. 
They will need to be deployed from source dir:

`backend/config/templates/` to

`/usr/local/esnet/oscars-backend/config/templates/`

### Database schema changes:
- For the `oscars_backend` database:

```
psql -d oscars_backend < backend/sql/1_0_36_to_1_0_37.sql
```

## 1.0.35 to 1.0.36
No further changes needed

## 1.0.34 to 1.0.35

### Config file changes
The PSS config gen templates have been moved to `backend/config/templates/`.
The  `pss.template-dirs` directive has been moved to the backend `config/application.properties` file.
For an existing installation, you will need to:
* `mv /usr/local/esnet/oscars-pss/config/templates /usr/local/esnet/oscars-backend/config/templates`
*  Edit `/usr/local/esnet/oscars-pss/config/application.properties` and remove the `pss.template-dirs` line,
*  Edit `/usr/local/esnet/oscars-backend/config/application.properties` and add the same line:
```
pss.template-dirs=./config/templates/
```


## 1.0.33 to 1.0.34
N/A

## 1.0.32 to 1.0.33

### Database schema changes:

- For the `oscars_backend` database:

```
psql -d oscars_backend < backend/sql/1_0_32_to_1_0_33.sql
```

### Config file changes
- Backend:
New supported entry:
```
application.properties:

tags.categories=
```

May be left empty or be a comma-separated list of files to load as tag category specifications.


### Topology update changes:
The new topo.py script will need to be installed and configured where required.


## 1.0.26 to 1.0.32


### SQL schema upgrade procedure
This need to be applied wherever oscars-backend is running.
* Prepare migration app.
```
$ scp oscars-web.es.net:/usr/local/esnet/migration.tgz ./migration.tgz
# it is a build of the `migration` app, v1.0.30-migration
$ tar xfz migration.tgz; cd migration
$ vi config/application.properties
# update spring.datasource.password=XXX to the correct one
```
* Verify migration app runs:
```
$ ./bin/start.sh
2019-04-12 10:21:15,523 INFO  n.e.o.MigrationApp                  - Starting MigrationApp
...

# Once the app finishes its run, you should have its output file:

$ ls -al timestamps.sql
 -rw-r--r-- 1 root root 355485 Apr 12 10:21 timestamps.sql
```
* Shut down service
```
$ sudo systemctl stop oscars-backend
```
* Backup current database (note the backup file will be in the `postgres` home directory)
```
$ sudo su - postgres
$ pg_dump -f oscars-bak.sql -C oscars_backend
$ exit
```
* Run the migration app with the application stopped
```
$ ./bin/start.sh
$ ls -al timestamps.sql
 -rw-r--r-- 1 root root 355485 Apr 12 10:21 timestamps.sql

```
* Apply schema change SQL and import new timestamps 
```
$ sudo su - postgres
$ cd migration # this should be the unpacked migration directory
$ psql -d oscars_backend < newschema.sql
$ psql -d oscars_backend < timestamps.sql
$ exit
```
At this point the new DB schema is installed and data has been migrated.

* Install the new RPM (v.1.0.31 or other appropriate version) 
```
% wget https://downloads.es.net/pub/oscars/oscars-backend-1.0.31-1.noarch.rpm
% sudo rpm -U oscars-backend-1.0.31-1.noarch.rpm
```

* Perform the config file changes described in the next section.

* Start the oscars-backup service 
```
% sudo systemctl start oscars-backend
```

### Config file changes
- backend:
```
application.properties:
# delete line:
security.basic.enabled=false

```
- pss
```
application.properties:
# delete line:
security.basic.enabled=true

# add line:
spring.security.user.roles=USER

# transform:
security.user.name=oscars   => spring.security.user.name=oscars
security.user.password=XXX  => spring.security.user.password=XXX

```

### Recovery
ONLY IF SOMETHING WENT WRONG: The following steps will restore the
database backup and restart the service.  These steps are NOT a part
of the update procedure.
```
$ sudo su - postgres
$ psql -d oscars_backend
# dropdb oscars_backend
# createdb -O oscars oscars_backend
# \q
$ psql -U oscars -d oscars_backend < oscars-bak.sql
$ exit
$ sudo systemctl start oscars-backend
```

## 1.0.25 to 1.0.26:

### Database schema changes:

- In the `oscars_backend` database:

```
alter table connection add column connection_mtu int;
update connection set connection_mtu = 9000;
```


## 1.0.24 to 1.0.25:

### Config file changes

- Topology devices.json file now supports these fields per device entry:

```
  "location": "a string",
  "location_id": integer,
  "longitude": double,
  "latitude": double
```

### Database schema changes:

- In the `oscars_backend` database:

```
alter table device add column location varchar(255);
alter table device add column location_id int;
alter table device add column latitude double precision;
alter table device add column longitude double precision;

update device set location = '', location_id = 0, latitude = 0, longitude = 0;
```
