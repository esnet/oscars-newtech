# oscars-newtech
## Synopsis
Short for "On-demand Secure Circuits and Advance Reservation System," OSCARS is 
a freely available open-source product. As developed by the Department of 
Energy’s high-performance science network ESnet, OSCARS was designed by 
network engineers who specialize in supporting the U.S. national laboratory 
system and its data-intensive collaborations. 

This project is a complete redesign of the original OSCARS to improve performance and maintainability. 


## Building OSCARS

### Preparing Your Environment

Make sure the following are installed on your system:

* [Java](https://www.java.com) 1.8
* [Maven](http://maven.apache.org) 3.1+
* [Postgres](https://www.postgresql.org/) 9.1+

## Running OSCARS

### Building
Run the following commands from the main project directory (oscars-newtech):

```
 mvn -DskipTests package 
```

### Preparation
You will need to be running the PostgreSQL server. One way to do that is as follows:
```bash
pg_ctl -D /usr/local/var/postgres start
```

Before running OSCARS for the first time, set up the database tables by 
executing the following script: 
```bash
cd oscars-newtech/backend
./bin/install_db.sh
```

### Starting OSCARS

You may start the OSCARS services (backend and pss) with the following command from the main project directory (oscars-newtech):

```bash
cd pss
./bin/start.sh

cd ../backend
./bin/start.sh
```

To access the local postgres table:

```bash
psql -d oscars_backend
```

### Accessing the Web User Interface 

OSCARS should now be running on your local machine.
 
The web UI can be accessed at: ``https://localhost:8201`` . 

You will be presented with a login screen. The default username is **admin** with a default password of **oscars**. 


### Accessing REST endpoints

You can see the Swagger REST endpoint documentation at 
``https://localhost:8201/documentation/swagger-ui.html``

We have three classes of endpoints:
 * Public endpoints, under the ``/api/`` prefix
 * Protected endpoints for registered users, under ``/protected``
 * Administrative endpoints, under ``/admin``

The protected and admin use JWT for security; when accessing them you must 
provide a token in the REST request's headers:
```
Authentication: eyJhbGciOiJIUzUxMiJ9.....ziNCEPd753usnwlPwBeA
```

To receive a token, use the login endpoint at ``api/account/login`` .

## Development notes
You should be familiar with the Maven build environment. This project follows its conventions closely.

### Versioning
When creating a new version, make sure to update it in:
- the top-level `pom.xml` as well as all the Java module pom.xml files (backend, nsi, pss, shared, migration)
- the static string version in `net.es.oscars.web.rest.MiscController`


### Testing 
You can run the unit tests with the command:

```bash
mvn test
```

You may also install only if the tests pass by running:

```bash
mvn install
```

### Navigating through some common errors

```bash
./bin/start.sh: line 11: [: too many arguments
  Unable to locate OSCARS pss.jar file
```

- Caused due to multiple JAR files. Can be fixed by deleting them from the target folder either in backend / pss


## Project Structure
The new OSCARS is a [Spring Boot](http://projects.spring.io/spring-boot/) application, made up of three major components: 
 * The main application (the "backend" module), 
 * the path setup subsystem ("pss"),
 * and the web UI; 
   * note: this is a node.js application and exists in a separate Github repo: [oscars-frontend](https://github.com/esnet/oscars-frontend). The backend module pulls this in as a dependency and serves out the packaged web UI javascript application.

The main project directory is structured as follows:

#### bin
Contains script(s) for running and maintaining OSCARS.

#### doc
Auto-generated documentation; use `./bin/generatedocs.sh` at the top-level distribution directory to update.  You will need a working installation of javasphinx.

### backend
The main application. Handles reservation requests, determines which path (if any) is available to satisfy the request, and reserves the network resources. Key modules include:
* **app** - Application-wide entities: startup classes, exceptions, configuration properties, serialization utilities,
* **pce** - The Path Computation Engine. It takes a requested reservation's parameters, evaluates the current topology, determines the (shortest) path, if any, and decides which network resources must be reserved.
* **pss** - Decides router command parameters, contacts the PSS component, and persists generated configuration.
* **resv** - Tracks reservations, and receives user parameters for reservation requests.
* **security** - Authentication and authorization-related classes
* **task** - Tasks that are scheduled to run at certain intervals (e.g. scheduled router configs, topology updates, etc).
* **topo** - Topology-related classes, persistence, loading, and utility libraries.
* **web** - REST endpoints 

#### shared 
A collection of shared classes used by both the PSS and backend modules. Mostly Data Transfer Objects (DTOs).

#### pss
The Path Setup Subsystem. The core sends commands to it, and it generates appropriate config and then commits it to network devices through rancid. 

#### topo
Topology-related scripts and utilities; currently mostly ESnet-specific Python code. 

#### clients
Various clients for the new REST API. 

#### nsi
XML classes for the NSI data types.

#### migration
Scripts and code for migrating OSCARS 0.6 reservations to 1.0. 
