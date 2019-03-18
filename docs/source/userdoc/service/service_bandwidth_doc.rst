.. _service_bandwidth:

Reserved Bandwidth Service
==========================

This is a support service that retrieves, updates, and maintains bandwidth state information (reserved/available). This service also maps bandwidth partitions to the network elements which consume them, and determines if the bandwidth requirement of the :ref:`requestspec` can be supported on the topology. The :ref:`service_pruning` will filter out those elements of the network with insufficient bandwidth availability during the requested circuit schedule.


Service Details
---------------
**Calls:**

- N/A

**Called By:** 

- :ref:`pce_top`
- :ref:`pce_translation`
- :ref:`service_pruning`

**API Specification:**

- :java:ref:`BandwidthService`
- :java:ref:`BandwidthAvailabilityService`
