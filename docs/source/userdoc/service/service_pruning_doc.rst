.. _service_pruning:

Topology Pruning Service
========================

This is a support service which is essential to completion of the :ref:`pce_doc`.  The *Pruning Service* temporarily modifies the network topology each time a new circuit request is submitted to the PCE. This will temporarily remove network elements that violate the :ref:`requestspec`, including ports with:

- Insufficient bandwidth availability.
- Insufficient VLAN availability.

The updated topology is then considered during pathfinding since it represents a network which is capable of supporting the desired circuit reservation.

Refer to the figures which describe the :ref:`basic_pce_service` for illustrative examples of how pruning modifies the network and ensures satisfactory pathfinding.  Furthermore, the user may include with the Request Specification a "blacklist" of nodes to avoid during pathfinding. An illustrative example of how the *Pruning Service* is employed in this scenario is offered in :ref:`ero_pce_blacklist`.



Service Details
---------------
**Calls:**

- :ref:`service_vlan`
- :ref:`service_bandwidth`
- :ref:`service_topology`

**Called By:** 

- :ref:`pce_ero`
- :ref:`pce_nonpalindrome`
- :ref:`pce_palindrome`
- :ref:`pce_survivability`
- :ref:`pce_translation`
- :ref:`virtual_topo`
- :ref:`virtual_surv_topo`

**API Specification:**

- :java:ref:`PruningService`



