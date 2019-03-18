
Getting Started
===============

Here you'll find instructions on obtaining, installing, and initializing OSCARS 1.0.

Download the sourcecode
-----------------------
The latest version of OSCARS can be downloaded via the project Github_.

.. _Github: https://github.com/esnet/oscars-newtech


Preparing Your Environment
--------------------------

Make sure the following are installed on your system:

* Java_ 1.8
* The latest version of Maven_ 

.. _Java: https://www.java.com
.. _Maven: http://maven.apache.org

Building with Maven
-------------------

To build the project, run the following commands from the main project directory:

.. code-block:: bash

   mvn install

This will require all unit tests included with the project to pass as a condition of a successful build. This is the recommended command.

Alternatively, you may skip these tests:

.. code-block:: bash

   mvn install -DskipTests

The unit tests can be run separately if desired:

.. code-block:: bash

   mvn test


Starting OSCARS Modules
-----------------------

OSCARS is pre-packaged with a start script, which initializes all modules of the system easily and conveniently. From the main project directory, run the command:

.. code-block:: bash

   ./bin/start.sh

At this point, OSCARS should be fully installed, built, and running on your local machine.  The following instructions will enable you to use the system, submit circuit reservations, etc.



Accessing the Web User Interface
--------------------------------

The Web UI serves as a visual portal through which the user can interact with the underlying OSCARS system.

The webui can be accessed (only once the system is running) at: https://localhost:8001. 

The user will be prompted for login credentials. By default, the following credentials will provide admin access priveleges:
 
   **Username: admin**

   **Password: oscars**


Project Structure
=================

The role of OSCARS is to schedule :ref:`virtualcircuit` with guaranteed bandwidth and service offerings.

OSCARS 1.0 has been developed as a Springboot application, made up of two major components: 
- The main application ("core"). 
- The web user interface ("webui"). 

The main project directory is structured as follows:

- **/bin**: Contains scripts and executables for running OSCARS.
- **/check**: Integration tests.
- **/core**: The main OSCARS application. The :ref:`core` handles circuit requests, determines which path (if any) is available to satisfy the request, and submits them for pathfinding based on their specifications. If successful, the Core also reserves the appropriate network resources in an automated fashion. Key modules include:
	- **acct**: Maintains list of customers and handles storing, editing, and retrieving account information.
	- **authnz**: Tracks permissions/authorization associated with user accounts.
	- **bwavail**: Calculates the minimum amount of bandwidth available between a given source and destination within a given time duration. Returns a series of time points and corresponding changes in 		availability, based on reservations in the system. 
	- **conf**: Retrieves configurations, specified in "oscars-newtech/core/config", for each OSCARS module on startup.
	- **helpers**: Functions useful for dealing with Instants and VLAN expression parsing.
	- **pce**: The :ref:`pce_doc`. It takes a requested reservation's parameters, evaluates the current topology, determines the (shortest) path, if any, and decides which network resources must be reserved.
	- **pss**: The :ref:`pss` sets up, tears down, modifies and verifies network paths. Handles templates for physical network devices.
	- **resv**: Tracks reservations, and receives user parameters for reservation requests. This module describes :ref:`requestspec`, :ref:`schedspec`, and :ref:`resvspec` for a circuit.
	- **servicetopo**: Abstracts the network topology to create unique :ref:`virtual_topo` views of the topology for a given request.
	- **tasks**: Services which run in the background and perform tasks at certain intervals (e.g. Select a submitted request to begin the reservation process).
	- **topo**: The :ref:`service_topology` maintains topology information.
- **/doc**: User and Code documentation.
- **/shared**: A collection of shared class definitions used by the different modules.
- **/webui**: The :ref:`webui` through which users can view their current and past reservations, and submit new reservation requests. The WebUI is built using the Thymeleaf template engine. The WebUI is a portal through which a user communicates with the Core API through REST calls.
- **/whatif**: Classes to facilitate flexible circuit provisioning. Services include translating incomplete or loosely-defined input parameters into optional or alternative solutions which may have different characteristics, service enhancements, and costs.



