.. _pce_survivability:

Survivability PCE Module
========================

This module finds a survivable pair or set of physically (node, port, and link) disjoint routes through the network for a given requested :ref:`plumbing` pipe. In the case where a pair of paths is required, the Survivability PCE relies upon the :ref:`pce_bhandari`, which performs Bhandari's algorithm to minimize the total cost of both the primary working path and the secondary backup path. This differs from some heuristics which aim to minimize the cost of the primary path at the possible expense of a more costly secondary path.  Thus, both paths are computed simultaneously.  If an appropriate pair cannot be identified which meets the constraints in requested specification, the entire circuit reservation fails. If the user requests a set of *K* disjoint paths, all *K* must be computed or the reservation fails. 

This module provides the primary implementation for the :ref:`surv_pce_services`:

- :ref:`surv_pce_complete`
- :ref:`surv_pce_mpls`
- :ref:`surv_pce_kpath`


Module Details
--------------
**Calls:**

- :ref:`pce_bhandari`
- :ref:`pce_dijkstra`
- :ref:`service_topology`
- :ref:`service_pruning`
- :ref:`virtual_surv_topo`

**Called By:** 

- :ref:`pce_top`

**API Specification:**

- :java:ref:`SurvivabilityPCE`

