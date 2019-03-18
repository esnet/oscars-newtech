.. _service_vlan:

VLAN Service
============

Provides methods that handle retrieving reserved VLAN IDs from the repository, mapping reserved/available/requested VLAN IDs to network elements, and determining if requested VLAN IDs can be supported on a topology.

.. _vlan_translation:

VLAN Translation and Swapping
-----------------------------

VLAN Translation (or Swapping), is a procedure used by the :ref:`pce_translation` to assign VLAN tags to a reserved path. VLAN translation allows one VLAN tag, possibly different than the tag(s) requested, to be used for the intermediate ports in a path. The VLANs available at each port (or at each switch) are encapsulated in a set of *Range* objects, each of which represents a contiguous range of VLAN IDs. For example, *Ranges [5, 10]* and *[15, 20]* could be available at a specific port, while IDs outside of that range are unavailable. To perform VLAN swapping, this service takes the path(s) returned from pathfinding in the :ref:`pce_doc` and retrieve the available ranges from each port involved in the path (along with each port on each switch in the path, if those ports aren't already in the path). The problem becomes similar to the *set cover problem*: choose the minimum set of VLANs that will cover all involved ports. The following restrictions apply: fixtures(ingress/egress points) must use one of the requested VLAN tags (may be different for distinct fixtures), all intermediate ports along the path must use the same VLAN tag, and all ports on any switches involved must use the same VLAN tag.

Major steps
^^^^^^^^^^^
1. Check if there are any VLAN tags available across the path (including fixtures). If so, use one of those.
2. If Step 1 was unsuccessful: For each fixture, assign a requested & available VLAN tag. Check if any of those tags are available across the intermediate ports of the path. If so, assign it to those ports as well (if they have not have VLANs assigned yet). If any fixture cannot have an assignment, then the request cannot be provisioned and circuit reservation fails.
3. If the intermediate ports are still not assigned, determine if there are any tags that are available across the entire intermediate path. If so, assign them. If not, the request cannot be provisioned and circuit reservation fails.


.. figure:: ../../.static/vlan_trans.png
    :scale: 65%
    :alt: VLAN Translation
    :align: center

    *VLAN translation example where a different VLAN tag can be assigned to intermediate ports if a requested tag is not available.*

.. note:: 

   Due to the above requirement that all ports on a switch are assigned a tag if that tag is assigned to any one of the ports, multiple VLANs may be assigned to a port throughout this process.

Service Details
---------------
**Calls:**

- N/A

**Called By:** 

- :ref:`pce_top`
- :ref:`pce_translation`
- :ref:`service_pruning`

**API Specification:**

- :java:ref:`VlanService`
