.. _pce_top:

Top PCE Module
==============

The Top PCE represents the PCE's API through which circuits are submitted for request, and are returned updated upon successful (or unsuccessful) reservation. Additionally, this module serves as the OSCARS Reservation Workflow Control mechanism which decomposes a :ref:`requestspec` into its :ref:`plumbing` containers and determines how to process each pipe. Note that these pipes are specified independently, meaning that the Top PCE may prescribe different flows through the remainder of the PCE for a single circuit request. The Top PCE feeds each pipe through one or more service-specific PCE submodules in order to perform the desired constrained pathfinding. The desired services are triggered during request specification, and the Top PCE will determine which of the appropriate PCE submodules to engage for supporting them. 

Module Details
--------------
**Calls:**

- :ref:`pce_ero`
- :ref:`pce_layer3`
- :ref:`pce_nonpalindrome`
- :ref:`pce_palindrome`
- :ref:`pce_survivability`
- :ref:`pce_translation`
- :ref:`service_topology`
- :ref:`service_pruning`
- :ref:`service_bandwidth`
- :ref:`service_vlan`

**Called By:** 

- N/A

**API Specification:**

- :java:ref:`TopPCE`


