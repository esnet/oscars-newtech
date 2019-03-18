.. _ero_pce_services:

Explicit Routing Services
=========================

OSCARS 1.0 supports the specification of circuits with prescribed route solutions. That is, the :ref:`pce_doc` does not necessarily need compute a route without help form the user. The user can in fact designate an *Explicit Route Object (ERO)* which serves as input to the PCE during pathfinding. The ERO may specify a complete route (in both the forward- and reverse-directions), a partial set of waypoints which must be reach along the route, or a set of devices and ports which must *not* be included in the route. This service enhances the user's ability to predict certain qualities of the espected solution.

.. _ero_pce_complete:

Complete Route Specification
----------------------------

The user may specify the complete ERO for either the forward- or reverse-direction. If a complete ERO is specified, the :ref:`service_pruning` will remove all other elements from the topology to ensure that only the expected solution may be computed by the PCE, as shown in the figure below (requested ERO elements with subscripts represent ports on a node that connect to the device matching the subscript label).

.. figure:: ../../.static/ero_complete.png
    :scale: 75%
    :alt: Complete ERO
    :align: center

    *A complete ERO may specify a particular route through the network not necessarily matching the most efficient path.*


If the expected route is not reservable (due to insufficient bandwidth or VLAN availability), the circuit reservation fails.  

.. figure:: ../../.static/ero_fail.gif
    :scale: 75%
    :alt: Complete ERO Failure
    :align: center

    *If even a single port along the specified ERO has insufficient resource availability, the reservation fails.*

The user may also specify Non-Palindromic forward- and reverse-direction routes such that they do not need to mirror one another. In this case, *both* EROs must be reservable or the circuit fails. If only the forward ERO is specified, the reverse-path will be computed as its Palindrome.

.. note::

	If Non-Palindromic EROs are requested, then all Ethernet devices/ports/links used in the forward path, **must** be represented in the return path. Only MPLS devices/ports/links may differ in forward-/reverse-direction paths.

.. figure:: ../../.static/ero_nonpal.png
    :scale: 75%
    :alt: Non-Palindromic ERO
    :align: center

    *The return path may be specified with a different ERO than the forward-direction path.*

Service Details
^^^^^^^^^^^^^^^

**Can be combined with:**

- :ref:`asymm_pce_service`
- :ref:`nonpalindromic_pce_service`
- :ref:`multipoint_pce_service`


**Relevant PCE Module(s)**

- :ref:`pce_ero`


.. _ero_pce_partial:

Partial Route Specification
---------------------------
    
An ERO may alternatively be specified as a partial solution. For example, a user may desire that certain segments of the routing solution pass through specific waypoints. As such, the ERO may be requested as a specific list of network nodes (devices or ports) that must be included in the associated forward and return path. This list represents an in-order mandatory subset of the nodes along the final ERO returned by the PCE. A route is established between each pair of waypoints in the sequence. 

.. figure:: ../../.static/ero_partial.gif
    :scale: 75%
    :alt: Non-Palindromic ERO
    :align: center

    *Partial ERO specification will establish a sequence of routes between each pair of waypoints in the sequence.*

All network elements not explicitly included in the Partial ERO are still candidates for inclusion in the final routing solution returned by the PCE. Consider requesting a partial ERO of simply the set of ports {A, Z}; the PCE would treat it identically to the :ref:`basic_pce_service`, and the appropriate solution would traverse a good portion of the network in the above example. However, if even a single specified subpath cannot be computed due to resource availability restrictions, the circuit reservation fails.

.. note::

	Unlike the Complete ERO specification described above, all Partial EROs are required to be palindromic.

.. warning::

	Since each segment of the final route is computed individually, it's possible that waypoints prescribed later in the path may be included in an earlier segment. OSCARS does not permit any cycles in its routing solutions, and if such a scenario were to occur, the Partial ERO would not be valid, and the reservation would fail.

Service Details
^^^^^^^^^^^^^^^

**Can be combined with:**

- :ref:`asymm_pce_service`
- :ref:`multipoint_pce_service`


**Relevant PCE Module(s)**

- :ref:`pce_ero`


.. _ero_pce_blacklist:

“Blacklist” Avoidance Routing
-----------------------------

In addition (or as an alternative) to providing nodes that must be included in the path as in the Complete and Partial ERO Specifications, the user may also find it desirable to be able to *avoid* a particular node or set of nodes comprising a section of the network topology. This desire is likely application-/user-specific. The user may specify a collection of devices and ports which they deem to be undesirable for the given circuit request. All of these "blacklisted" nodes, as well as the network links connected to included ports will be explicitly pruned from the network prior to pathfinding. If the remaining network components do not support a working route solution, the reservation fails. Since this ERO service is handled be the :ref:`service_pruning`, it can be combined with all other existing PCE services.
    


.. figure:: ../../.static/ero_blacklist.gif
    :scale: 75%
    :alt: Blacklist
    :align: center

    *Blacklisted elements are explicitly pruned out of the topology before PCE execution commences.*


**Can be combined with:**

- :ref:`basic_pce_service`
- :ref:`asymm_pce_service`
- :ref:`nonpalindromic_pce_service`
- :ref:`multipoint_pce_service`
- :ref:`ero_pce_complete`
- :ref:`ero_pce_partial`
- :ref:`surv_pce_complete`
- :ref:`surv_pce_mpls`
- :ref:`surv_pce_kpath`


**Relevant Topology/PCE Service(s)**

- :ref:`service_pruning`
