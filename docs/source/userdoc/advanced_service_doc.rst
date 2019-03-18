.. _advanced_service:

Path Computation Service Enhancements
=====================================

.. image:: ../.static/construction.png
   :scale: 50%
   :align: center

OSCARS 1.0 supports a number of all-new composable routing capabilities which provide an unprecedented amount of user control over how the :ref:`pce_doc` identifies working solutions. All of these services may be triggered without alteration to the underlying code, and without the need for modifying configuration parameters when launching OSCARS. This document discusses the PCE service enhancement suite, while :ref:`requestspec` provides details on how the user can exert control over the flow through the PCE to engage and toggle each of the described services. The following table offers a listing of the services discussed throughout the remainder of the document. Note: Forward-direction circuit route is denoted as **A-Z**, while reverse is denoted as **Z-A**.


==========================================   	============
Service Enhancement   			  	Description
==========================================	============
:ref:`asymm_pce_service`			A-Z and Z-A circuits may exhibit/explicitly require different bandwidth quantities.
:ref:`nonpalindromic_pce_service`		A-Z and Z-A circuits may not mirror one another. Only portions of the route passing through an MPLS segment may differ.
:ref:`multipoint_pce_service`			Provide more than one destination device for a circuit, or create a cyclic transfer among a set of devices.
:ref:`ero_pce_services`				Force the PCE to use or avoid specific portions of the network during pathfinding.	  
- :ref:`ero_pce_complete`			Specify exact A-Z and Z-A circuits in which the specified path must be traversed in order. Pathfinding fails if exact match cannot be reserved.
- :ref:`ero_pce_partial`			Specify a partial A-Z or Z-A circuit in which all specified nodes/ports must be reached in relative order, but they serve as waypoints along the computed path.
- :ref:`ero_pce_blacklist`			Specify a set of nodes/ports which are to be pruned from the network topology prior to pathfinding to ensure avoidance.
:ref:`surv_pce_services`			Establish multiple routes along disjoint-paths within the same requested pipe in case of physical network failure. If survivable routing is impossible, the entire reservation fails.
- :ref:`surv_pce_complete`			*Only* the souce node/port and destination source/port are shared among paths. **Exactly two paths computed.**
- :ref:`surv_pce_mpls`				Provides survivability *only*	within an MPLS segment. All Ethernet segments are identical among paths. **Exactly two paths computed.**
- :ref:`surv_pce_kpath`				Specify a number (K) of disjoint paths desired. All paths will be set-wise disjoint.
==========================================	============

.. toctree::
   :hidden:

   enhanced/asymm_service_doc
   enhanced/nonpalindromic_service_doc
   enhanced/multipoint_service_doc
   enhanced/ero_service_doc
   enhanced/survivability_service_doc



