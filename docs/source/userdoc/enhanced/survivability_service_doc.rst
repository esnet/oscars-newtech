.. _surv_pce_services:

Survivable Routing Services
===========================



.. _surv_pce_complete:

Complete Path Survivability
---------------------------

Network elements, particularly links, experience failures almost regularly. Any traffic traversing a link at the time of failure can be lost, and service may be interrupted. To mitigate this loss, a backup path can be established alongside the primary path requested by a user. If the user indicates that a pipe must be provisioned using complete end-to-end survivability, a link-disjoint path pair will be found via :ref:`pce_bhandari` between the requested pipe’s end-points junctions. No links will be shared between these primary and backup paths. Each path in the pair will be palindromic; in other words, each will have both a forward- and reverse-direction flow. If a disjoint path pair through the network cannot be found, then the request fails. 

.. note::

	The end-point ports and devices will necessarily be shared among both paths in a survivable pair. As such, twice as much bandwidth will be consumed at these shared points.


.. figure:: ../../.static/surv_complete.png
    :scale: 65%
    :alt: Complete Survivability
    :align: center

    *Complete link-disjoint path survivability from end-to-end.*

Service Details
^^^^^^^^^^^^^^^

**Can be combined with:**

- :ref:`multipoint_pce_service`

**Relevant PCE Module(s)**

- :ref:`pce_survivability`
- :ref:`pce_bhandari`



.. _surv_pce_mpls:

MPLS-Layer Survivability
------------------------
    
Alternatively the user may desire a singular service path, but may still make use of additional routing flexibility through MPLS routers in order to incorporate partial circuit survivability. Paths through routers will be survivable, but Ethernet devices will support a single connection. In other terms, a :ref:`virtual_surv_topo` is created for each *MPLS-Layer Survivable* request. This Service-Layer topology abstracts out the MPLS-Layer devices, and replaces them with a pair of abstract links between each pair of Service-Layer elements adjacent to the MPLS-Layer. These abstract links are computed independently such they they each correspond to the least-cost pair of link-disjoint MPLS-Layer paths between the end-points. Therefore, the forward and return paths *may* contain different routing elements and may not produce the same survivable solutions as one another. If no link-disjoint path pairs through the MPLS layer can be found when requested, and it is not possible to establish a route without circumventing the MPLS-layer entirely, then circuit reservation fails. The figure below shows a possible circuit reservation with *MPLS-Layer Survivability*.

.. figure:: ../../.static/surv_partial.gif
    :scale: 70%
    :alt: Partial Survivability
    :align: center

    *MPLS-Layer survivability.*

Service Details
^^^^^^^^^^^^^^^

**Can be combined with:**

- :ref:`multipoint_pce_service`

**Relevant PCE Module(s)**

- :ref:`pce_survivability`
- :ref:`pce_bhandari`



.. _surv_pce_kpath:

K-Disjoint Path Survivability
-----------------------------
    
The user can request any number of link-disjoint paths for either Complet or Partial survivability. This is implemented through an iterative version of Bhandari’s Algorithm, which will find the specified number of link-disjoint paths K (or the maximum number of disjoint paths less than K if the physical network restricts the ability to establish so many link-disjoint routes). A simple illustration where *K = 3* is shown below:

.. figure:: ../../.static/surv_kpath.png
    :scale: 70%
    :alt: K-Path Survivability
    :align: center

    *K-Path link-disjoint survivability, for a value of K=3.*

Service Details
^^^^^^^^^^^^^^^

**Can be combined with:**

- :ref:`multipoint_pce_service`

**Relevant PCE Module(s)**

- :ref:`pce_survivability`
- :ref:`pce_bhandari`


.. toctree::
   :hidden:

   virtual_surv_topo_doc
