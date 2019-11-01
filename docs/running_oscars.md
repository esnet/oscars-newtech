# Running OSCARS

## Preparing Your Environment

Make sure the following are installed on your system:

* [Java](https://www.java.com) 1.8
* [Maven](http://maven.apache.org) 3.1+
* [Postgres](https://www.postgresql.org/) 9.1+

## Building

Run the following commands from the main project directory (oscars):

```
 mvn -DskipTests package 
```

## Preparation

You will need to be running the PostgreSQL server. One way to do that is as follows:

```bash
pg_ctl -D /usr/local/var/postgres start
```

Before running OSCARS for the first time, set up the database tables by executing the following script:

```bash
cd oscars/backend
./bin/install_db.sh
```

## Starting OSCARS

You may start the OSCARS services (backend and pss) with the following command from the main project directory (oscars):

```bash
cd pss
./bin/start.sh

cd ../backend
./bin/start.sh
```

To access the local postgres table:

```bash
psql -U oscars -d oscars_backend
```

## Accessing the Web User Interface 

OSCARS should now be running on your local machine.
 
The web UI can be accessed at: ``https://localhost:8181`` . 

You will be presented with a login screen. The default username is **admin** with a default password of **oscars**. 

## Accessing REST endpoints

You can see the Swagger REST endpoint documentation at ``https://localhost:8201/documentation/swagger-ui.html``

We have three classes of endpoints:
 * Public endpoints, under the ``/api/`` prefix
 * Protected endpoints for registered users, under ``/protected``
 * Administrative endpoints, under ``/admin``

The protected and admin use JWT for security; when accessing them you must provide a token in the REST request's headers:

```
Authentication: eyJhbGciOiJIUzUxMiJ9.....ziNCEPd753usnwlPwBeA
```

To receive a token, use the login endpoint at ``api/account/login`` .
