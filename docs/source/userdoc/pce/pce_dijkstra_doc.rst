.. _pce_dijkstra:

Dijkstra PCE Module
===================

This module is one of the fundamental pathfinding elements of the PCE, used to compute shortest (least-cost) path routing on a constrained network. Dijsktra's Algorithm_ is one of the most widely adopted routing algorithms because it optimally computes its path. Most of the service-specific PCE modules rely on the Dijkstra PCE for the low-level path computation after constraining the topology for their specified services.

.. _Algorithm: https://en.wikipedia.org/wiki/Dijkstra's_algorithm

Module Details
--------------
**Calls:**

- :ref:`service_topology`

**Called By:** 

- :ref:`pce_ero`
- :ref:`pce_nonpalindrome`
- :ref:`pce_palindrome`
- :ref:`pce_survivability`
- :ref:`service_bandwidth`
- :ref:`virtual_topo`

**API Specification:**

- :java:ref:`DijkstraPCE`
