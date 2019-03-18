.. _service_topology:

Topology Service
================

This is a support service which maintains and updates Topology information. This includes completed details on network devices, ports, and links. Each of these components has its own set of attributes, such as available bandwidth capacity, device make and model, etc. This service provides connectivity details and supports methods for retrieving the topology as a multi-layer representation. For example, MPLS Routers and their interconnections are represented as a single layer, while Ethernet switches and their connections are represented as another. Furthermore, OSCARS represents ports and devices as connected nodes such that there is a zero-cost link between them. An internal-layer topology contains information about each of these connections.  

The *Topology Service* is used heavily by the :ref:`pce_doc` during pathfinding using the assumptions described in :ref:`topologyref`.


Service Details
---------------
**Calls:**

- N/A

**Called By:** 

- :ref:`pce_dijkstra`
- :ref:`pce_ero`
- :ref:`pce_nonpalindrome`
- :ref:`pce_palindrome`
- :ref:`pce_survivability`
- :ref:`pce_top`
- :ref:`pce_translation`
- :ref:`service_pruning`
- :ref:`service_bandwidth`

**API Specification:**

- :java:ref:`TopoService`



