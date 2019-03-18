.. _virtual_surv_topo:

Service-Layer Topology with Survivable MPLS-Layer
=================================================

The :ref:`surv_pce_mpls` introduced in OSCARS 1.0 provides intelligent, flexible, and survivable routing capabilities supported by the heterogeneity of physical devices used throughout ESnet. In particular, device classes include: Ethernet Switches, and MPLS Routers. These services consider the distribution of these devices and decompose the physical network topology into two distinct layers:

The Service-Layer Topology 
^^^^^^^^^^^^^^^^^^^^^^^^^^

- Ethernet switches and connected ports.
- Network links which are adjacent to an ethernet switch/port.

The MPLS-Layer Topology
^^^^^^^^^^^^^^^^^^^^^^^

- MPLS routers and connected ports.
- Network links connecting a pair of MPLS routers.

All circuit service begins and terminates are devices and ports on the Service-Layer. The MPLS-Layer, meanwhile, is abstracted out of the Service-Layer such that no MPLS elements are included. For each pair of elements on the border between the two layers, there exist two abstract edges as shown in the figure below (refer to the :ref:`topologyref` for an introduction to the topology element illustrations used throughout this document). These edges represent a logical pair of link-disjoint connections through the MPLS-Layer.

.. figure:: ../../.static/service_topo.gif
    :width: 45%
    :alt: Service-Layer Topology
    :align: center

    *All MPLS-Layer components are abstracted out of the Service-Layer to be replaced by abstract edges connecting pairs of Service-Layer components.*

The Service-Layer topology can then be submitted in place of the true physical topology to the :ref:`pce_doc`. The abstract links are treated identically to physical links by the PCE components. That is, they are pruned from the Service-Layer topology and then included in the set of links passed into the :ref:`pathfinding_algorithms`. Once the PCE has completed execution on the Service-Layer topology, any abstract links included in the solution are translated back into their corresponding physical counterparts. No further processing by the PCE is performed in order to compute the physical paths. The process of assigning translational values and weights is described below.


Computing Survivable Abstract Links
-----------------------------------

Unlike the :ref:`virtual_topo` in which each abstract link corresponds to a single route through the MPLS-Layer, the *Service-Layer Topology with Survivable MPLS-Layer* maps a single abstract link to a **pair** of routes through the MPLS-Layer. Not only is there a 2:1 route-to-link mapping from MPLS-Layer to Service-Layer, but the pair of routes is also necessarily link-disjoint. An abstract link exists on the Service-Layer topology if and only if two paths beginning and ending at the appropriate border ports of the MPLS-Layer can be established such that they do not have any ports or links in common with each other (except for the source and destination end-points). Since the abstract links are unidirectional, these survivable route-pairs are computed individually and there is no guarantee that they will use any of the same intermediate MPLS-Layer links or provide survivability in the same way. The only guarantee to the user is that each unidirecitonal abstract link will provide a primary and backup path solution through the MPLS-Layer. The computation of abstract links is performed as shown in the following figures.  

.. figure:: ../../.static/mpls_routing_survivable.gif
    :scale: 85%
    :alt: MPLS-Layer Survivable Routing
    :align: center

    *Survivable link-disjoint routing is performed between each pair of MPLS-Layer ports.*

First, a pair of link-disjoint routes is computed between every pair of MPLS-Layer ports using the :ref:`pce_bhandari`. This procedure is conducted for every circuit reservation requiring this abstraction because the network state is dynamic and changes with each subsequent circuit reservation or release. Then, those MPLS-Layer routes beginning and terminating at the end-points of an abstract link are saved as a translational list mapping the physical path-pair to the appropriate abstract links. The weight of an abstract link is exactly identical to the sum of the weights of all physical links contained within the **primary** MPLS-Layer path. 

.. figure:: ../../.static/mpls_route_map_survivable.gif
    :width: 75%
    :alt: MPLS-Layer Survivable Route Map
    :align: center

    *The weight of the computed abstract links corresponds to the total weight of the physical links traversed by the* primary path *in the MPLS-Layer pair. Abstract link pairs need not correspond to idential survivable route pairs nor weights.*

.. note::

	The necessity to map the physical survivable path-pair to an abstract link requires an additional pass through the PCE's pathfinding algorithms. In this case, the topology used for the path computation is the MPLS-Layer topology. This enables each abstract Service-Layer link to correspond to the least-cost pair of link-disjoint routes through the MPLS-Layer.



