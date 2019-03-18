.. _pce_ero:

Explicit Route Object (ERO) PCE Module
======================================

This module enhances the user's control over specifying the route objects contained within the PCE solution. Specifically, it enables the user to specify either a complete or partial specification of nodes (devices/ports) that *must* be included in the route. 

Complete ERO
------------

If the user requests a complete ERO, the entire path must be available and must satisfy the requested :ref:`plumbing`, otherwise the circuit reservation fails. The user may specify a different ERO for the forward-direction and reverse-direction paths, however **all** Ethernet nodes incorporated in the forward path **must** be present in the return path (thus only segments of the routes consisting entirely of MPLS routers, ports, and the links that connect them may differ).


Partial ERO
-----------

In the case of a partial ERO, the nodes included will become in-order waypoints for the computed route. All nodes in the ERO must be reachable for success.

.. note::

   The current implementation enforces a palindromic return path for partial EROs. That is, the return path is a mirror of the forward path computed by the PCE.

ERO Blacklist
-------------

In addition to providing nodes that must be included in the path, nodes that **must not** be included in the route can be specified as a "blacklist". All links connected to these blacklisted nodes are removed from the topology by the :ref:`service_pruning` prior to pathfinding. If a route cannot be computed given the remaining resources, the circuit request fails. 


This module comprises the majority of the :ref:`ero_pce_services`, however these services can be combined with the :ref:`asymm_pce_service`.



Module Details
--------------
**Calls:**

- :ref:`pce_dijkstra`
- :ref:`service_topology`
- :ref:`service_pruning`

**Called By:** 

- :ref:`pce_top`

**API Specification:**

- :java:ref:`EroPCE`
