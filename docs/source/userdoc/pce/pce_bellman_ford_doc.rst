.. _pce_bellman_ford:

Bellman-Ford PCE Module
=======================

This module is one of the fundamental pathfinding elements of the PCE, used to compute shortest (least-cost) path routing on a constrained network. The Bellman-Ford Algorithm_ is one of the most widely adopted routing algorithms because it optimally computes its path, and is able to incorporate negatively weighted edges into its graph model. This module serves as the underlying pathfinding mechanism for the :ref:`pce_bhandari`, which requires negative edge weights. The module is not incorporated into any service-specific PCE modules at this time.

.. _Algorithm: https://en.wikipedia.org/wiki/Bellman%E2%80%93Ford_algorithm


Module Details
--------------
**Calls:**

- N/A

**Called By:** 

- :ref:`pce_bhandari`

**API Specification:**

- :java:ref:`BellmanFordPCE`
