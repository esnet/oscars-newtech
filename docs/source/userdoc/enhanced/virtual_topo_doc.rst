.. _virtual_topo:

Service-Layer Topology
======================

The :ref:`nonpalindromic_pce_service` introduced in OSCARS 1.0 provides intelligent and flexible routing capabilities supported by the heterogeneity of physical devices used throughout ESnet. In particular, device classes include: Ethernet Switches, and MPLS Routers. These services consider the distribution of these devices and decompose the physical network topology into two distinct layers:

The Service-Layer Topology
^^^^^^^^^^^^^^^^^^^^^^^^^^

- Ethernet switches and connected ports.
- Network links which are adjacent to an ethernet switch/port.

The MPLS-Layer Topology
^^^^^^^^^^^^^^^^^^^^^^^

- MPLS routers and connected ports.
- Network links connecting a pair of MPLS routers.

All circuit service begins and terminates are devices and ports on the Service-Layer. The MPLS-Layer, meanwhile, is abstracted out of the Service-Layer such that no MPLS elements are included. For each pair of elements on the border between the two layers, there exist two abstract edges as shown in the figure below (refer to the :ref:`topologyref` for an introduction to the topology element illustrations used throughout this document). These edges represent a logical connection through the MPLS-Layer.

.. figure:: ../../.static/service_topo.gif
    :width: 45%
    :alt: Service-Layer Topology
    :align: center

    *All MPLS-Layer components are abstracted out of the Service-Layer to be replaced by abstract edges connecting pairs of Service-Layer components.*

The Service-Layer topology can then be submitted in place of the true physical topology to the :ref:`pce_doc`. The abstract links are treated identically to physical links by the PCE components. That is, they are pruned from the Service-Layer topology and then included in the set of links passed into the :ref:`pathfinding_algorithms`. Once the PCE has completed execution on the Service-Layer topology, any abstract links included in the solution are translated back into their corresponding physical counterparts. The figure below shows a possible PCE solution consisting of a physical link and an abstract link. No further processing by the PCE is performed in order to compute the physical paths. The process of assigning translational values and weights is described below.


.. figure:: ../../.static/service_topo_effect.png
    :width: 50%
    :alt: Difference between abstract and physical links
    :align: center

    *All* physical *connections between Service-Layer components are bidirectional links. However, all* abstract *connections are a pair of unidirectional* paths *through the MPLS-Layer.*


Computing Abstract Links
------------------------

As described above, every pair of Service-Layer components adjacent to the MPLS-Layer is connected by a pair of abstract links representing physical routes through the MPLS-Layer. Since the abstract links are unidirectional, they are computed individually and there is no guarantee that they will use any of the same intermediate MPLS-Layer links. The computation of abstract links is performed as shown in the following figures.  

.. figure:: ../../.static/mpls_routing.gif
    :scale: 85%
    :alt: MPLS-Layer Routing
    :align: center

    *Routing is performed between each pair of MPLS-Layer ports.*

First, a route is computed between every pair of MPLS-Layer ports. This procedure is conducted for every circuit reservation requiring this abstraction because the network state is dynamic and changes with each subsequent circuit reservation or release. Then, those MPLS-Layer routes beginning and terminating at the end-points of an abstract link are saved as a translational list mapping the physical path to the appropriate abstract links. The weight of an abstract link is exactly identical to the sum of the weights of all physical links it contains. 

.. figure:: ../../.static/mpls_route_map.png
    :width: 50%
    :alt: MPLS-Layer Route Map
    :align: center

    *The weight of the computed abstract links corresponds to the total weight of the physical links it traverses. Abstract link pairs need not correspond to idential physical routes nor weights.*

.. note::

	The necessity to map the physical path to an abstract link requires an additional pass through the PCE's pathfinding algorithms. In this case, the topology used for the path computation is the MPLS-Layer topology. This enables each abstract Service-Layer link to correspond to the shortest (least-cost) route through the MPLS-Layer.



