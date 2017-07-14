# oscars-newtech
## Synopsis
Short for "On-demand Secure Circuits and Advance Reservation System," OSCARS is a freely available open-source product. As developed by the Department of Energy’s high-performance science network ESnet, OSCARS was designed by network engineers who specialize in supporting the U.S. national laboratory system and its data-intensive collaborations. This project is a complete redesign of the original OSCARS to improve performance and maintainability. 


## Building OSCARS

### Preparing Your Environment

Make sure the following are installed on your system:

* [Java](https://www.java.com) 1.8
* [Maven](http://maven.apache.org) 3.1+

### Building using maven

Run the following commands from the main project directory (oscars-newtech):

```bash
mvn -DskipTests install
```

## Testing OSCARS
You can run the unit tests with the command:

```bash
mvn test
```

You may also install only if the tests pass by running:

```bash
mvn install
```
## Running OSCARS

### Preparation
You will need to install and run a PostgreSQL server.

You will also need to, one time, set up the database tables by running the following script from the core directory; 
```bash
./bin/install_db.sh
```

### Starting OSCARS

You may start all OSCARS services (core and webui) with the following command:

```bash
./bin/start.sh
```

If on windows, an alternative python script can be used.
It has the following dependencies:
* [Python](https://www.python.org/) 2.7+ or 3.6+

```bash
 python ./bin/win_start.py
```
### Accessing the Web User Interface (webui)

OSCARS should now be running on your local machine. The webui can be accessed at: https://localhost:8001. You will be presented with a login screen. The admin username is **admin** and the default password is **oscars**. 

## Project Structure
The new OSCARS is a [Springboot](http://projects.spring.io/spring-boot/) application, made up of two major components: The main application ("core"), and the path setup subsystem ("pss"). 
The main project directory is structured as follows:
### bin
Contains script(s) for running OSCARS.
### doc
Auto-generated documentation; use `bin/generatedocs.sh` at the top-level distribution directory to update.  You will need a working installation of javasphinx.

### core
The main application. Handles reservation requests, determines which path (if any) is available to satisfy the request, and reserves the network resources. Key modules include:
* **acct** - Maintains list of customers and handles storing, editing, and retrieving account information.
* **bwavail** - Calculates the minimum amount of bandwidth available between a given source and destination within a given time duration. Returns a series of time points and corresponding changes in bandwidth availability, based on reservations in the system. 
* **authnz** - Tracks permissions/authorization associated with user accounts. 
* **conf** - Retrieves configurations, specified in "oscars-newtech/core/config", for each OSCARS module on startup.
* **helpers** - Functions useful for dealing with Instants and VLAN expression parsing.
* **pce** - The Path Computation Engine. It takes a requested reservation's parameters, evaluates the current topology, determines the (shortest) path, if any, and decides which network resources must be reserved.
* **pss** - Decides router command parameters, contacts the PSS component, and persists generated configuration.
* **resv** - Tracks reservations, and receives user parameters for reservation requests.
* **servicetopo** - Abstracts the network topology to create unique "Service Level" views of the topology for a given request.
* **tasks** - Services which run in the background and perform tasks at certain intervals (e.g. Select a submitted request to begin the reservation process).
* **topo** - Maintain topology information.
* **whatif** - Generate reservation suggestion for users on-demand.

### shared 
A collection of shared classes used by the different modules. 

### pss
The Path Setup Subsystem. The core sends commands to it, and it generates appropriate config and then commits it to network devices through rancid. 

### topo
Topology-related scripts and utilities; currently mostly ESnet-specific Python code. 
