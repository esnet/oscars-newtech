.. _core:

Core Module
===========

The OSCARS Core is responsible for all circuit setup, reservation, and management operations. The figure below shows the primary submodules contained within the core. The role of each submodule is briefly described below. In addition to these descriptions, the Core is responsible for handling application configuration, as well as AuthN and AuthZ responsibilities.

.. figure:: ../.static/architecture.png
    :scale: 50%
    :alt: Core module architecture
    :align: center

    *High-level depiction of the OSCARS core module's components.*


Request Specification
---------------------
Responsible for describing the formats of new incoming circuit request objects. Includes source node/port, destination node/port, start-time, end-time, bandwidth requirement, as well as additional optional values which alter the level and type of pathfinding service that OSCARS will perform in trying to satisfy the request. Specifics are offered in Section V of this document. Additional details: :ref:`requestspec`


Reservation Specification
-------------------------
Responsible for describing the formats of successful and unsuccessful reservation objects. Additional details: :ref:`resvspec`


Plumbing Specification
----------------------
Internal representation of Request/Reservation Specifications. Manipulation of requested "plumbing" details allows OSCARS to enable a number of cooperating service enhancements during circuit setup.
Additional details: :ref:`plumbing`


Reservation Workflow Control
----------------------------
Interprets the Request and Plumbing Specifications to determine which enhanced service capabilities should be considered and incorporated during the reservation process. This submodule determines how various path computation submodules should be accessed and in what order or combinations. In the implmentation, this component is called the :ref:`pce_top`


Path Computation Engine
-----------------------
Provides all of the logic for computing a path given the current topology state (as maintained by the Topology Service), and the Request Specification. An extensive range of PCE services is newly available as of OSCARS 1.0, which can be applied independently for each request. The PCE evaluates the Request Specification, and dynamically traverses a set of non-linear submodules in a manner which best services the given request. The result is either a working path, or a notice of failure. The PCE is detailed extensively in: :ref:`pce_doc`


Pruning Service
---------------
Responsible for temporarily updating the network topology given the incoming Request Specification. If any network elements cannot support the requested bandwidth, or VLAN requirements throughout the reservation’s prospective duration window, for example, they will be temporarily pruned from the topology and considered unusable during path computation for the given circuit. 


Topology Resource Manager
-------------------------
Maintains, manages, and updates real-time topology information, including bandwidth and VLAN availability at each port throughout a schedule, as well as existing connectivity information throughout the network.


Bandwidth Resource Manager
--------------------------
Maintains information about bandwidth consumption on every network link over time.


VLAN Resource Manager
---------------------
Maintains information about VLAN consumption on every network port over time.


Path Setup Service
------------------
Automates the process of network configuration based on the path resulting from PCE execution. The Path Setup Service is responsible for tracking network devices (including details such as manufacturer, model, configuration, supported protocols, authentication methods, etc.).  It also monitors configured service health over the service lifecycle.


Reservation Database
--------------------
Maintains encapsulated state information for each and every circuit reservation (past, present, and future) as well state information of requested but ultimately unsuccessful circuit requests.



Additional Documentation
------------------------
.. toctree::
   :titlesonly:
   :includehidden:

   topologyref_doc
   pce_doc
   advanced_service_doc
   pss_doc



