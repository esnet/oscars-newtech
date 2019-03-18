.. _requestspec:

Circuit Request Specification
=============================

An OSCARS circuit is decomposed roughly into three components: The *Circuit Request Specification*, the :ref:`schedspec`, and the :ref:`resvspec`.  The Request Specification describes the user's input parameters and models the desired logical connection from the user.

The request is composed of a collection of pipes, junctions, and fixtures, which are described in detail by the :ref:`plumbing`. Each request must consist of at least one pipe or one junction and at least two fixtures.  A user may request a connection across a single junction, which would represent a direct connection between two access ports on a single device.  Alternatively, users may request a pipe, which must begin and end at different junctions, each of which must have at least one port. The given plumbing specification will be passed as input to the :ref:`pce_doc`, which is the heart of OSCARS and performs all of its routing and resource allocation duties.  Each device and port has a topology-specific junction. The end-points of a request may be specified using those IDs.

The user may trigger either a :ref:`basic_pce_service` or :ref:`advanced_service` simply by manipulating the input plumbing specified in the request. Each requested pipe is handled by the PCE individually, and service enhancements are activated per-pipe. In other words, by manipulating the requested parameters of one or more pipes, OSCARS services may be toggled and/or combined where possible.

The table below details the various components of the requested pipe, and their appropriate values and parameters.

============================ ============================ ============================= ==================
Parameter Name				 Data Type		  Preset Values				Description
============================ ============================ ============================= ==================
aJunction 		     Requested Junction 	  N/A 					    Source-side end-point device.
zJunction 		     Requested Junction 	  N/A 					    Destination-side end-point device.
azMbps	 		     Integer			  > 0 					    A->Z direction bandwidth requirement.
zaMbps	 		     Integer			  > 0 					    Z->A direction bandwidth requirement. If *zaMbps* is not equal to *azMbps*, :ref:`asymm_pce_service` is toggled
azERO 		     	     List<String>		  (Empty)				    Explicit (in-order) Route Object in the A->Z direction. :ref:`ero_pce_complete` or :ref:`ero_pce_partial`
zaERO 		     	     List<String>		  (Empty)				    Explicit (in-order) Route Object in the Z->A direction. Enables :ref:`ero_pce_complete`
urnBlacklist		     Set<String>		  (Empty)				    Devices/Ports to explicitly avoid during pathfinding. Enables :ref:`ero_pce_blacklist`
eroPalindromic		     Enumerated			  PALINDROME (Default)			    Toggles :ref:`nonpalindromic_pce_service`
							  NON_PALINDROME		
eroSurvivability	     Enumerated		  	  SURVIVABILITY_NONE (Default)	Toggles :ref:`surv_pce_services`
		  	  				  SURVIVABILITY_TOTAL			    
							  SURVIVABILITY_PARTIAL
============================ ============================ ============================= ==================

.. warning::

	In the current implementation, there is an order of precedence for these values. For example, specifying values for *azERO* and *zaERO* override the ability to adaptively compute paths according to the default behavior of the :ref:`basic_pce_service`. If these values are set, the value in *eroPalindromic* is also ignored by the system. Similarly, if *eroSurvivability* is set to anything except *SURVIVABILITY_NONE*, the *eroPalindromic* value will be forced to *PALINDROME*, since non-palindromic survivability is not currently supported.


Relevant API Specification
--------------------------

- :java:ref:`SpecificationE`
- :java:ref:`RequestedBlueprintE`
- :java:ref:`RequestedVlanFlowE`
- :java:ref:`RequestedVlanJunctionE`
- :java:ref:`RequestedVlanFixtureE`
- :java:ref:`RequestedVlanPipeE`


