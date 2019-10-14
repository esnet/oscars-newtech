# OSCARS

## Synopsis
Short for *On-demand Secure Circuits and Advance Reservation System*, OSCARS is  a freely available open-source product. As developed by the Department of Energy’s high-performance science network ESnet, OSCARS was designed by network engineers who specialize in supporting the U.S. national laboratory system and its data-intensive collaborations. 

This project is a complete redesign of the original OSCARS to improve performance and maintainability. 

## Project Structure
The new OSCARS is a [Spring Boot](http://projects.spring.io/spring-boot/) application, made up of three major components: 
 * The main application (the *backend* module), 
 * the path setup subsystem (*pss*),
 * and the web UI

The main project directory is structured as follows:

### bin
Contains script(s) for running and maintaining OSCARS.

### doc
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

### frontend
This is a node.js application. The backend module pulls this in as a dependency and serves out the packaged web UI javascript application.

### shared 
A collection of shared classes used by both the PSS and backend modules. Mostly Data Transfer Objects (DTOs).

### pss
The Path Setup Subsystem. The core sends commands to it, and it generates appropriate config and then commits it to network devices through rancid. 

### topo
Topology-related scripts and utilities; currently mostly ESnet-specific Python code. 

### clients
Various clients for the new REST API. 

### nsi
XML classes for the NSI data types.

### migration
Scripts and code for migrating OSCARS 0.6 reservations to 1.0

## Development

* [Running OSCARS](./docs/running_oscars.md)
* [Development Notes](./docs/development_notes.md)
* [API Docs](./docs/API.md)
* [Syslog](./docs/syslog.md)

## Release Notes

Release Notes can be accessed [here](./CHANGES.md)
