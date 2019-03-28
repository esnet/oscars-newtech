.. _resvspec:

Circuit Reservation Specification
=================================

An OSCARS circuit is decomposed roughly into three components: The :ref:`requestspec`, the :ref:`schedspec`, and the *Circuit Reservation Specification*. The Reservation Specification describes the :ref:`pce_doc` output upon completion of successful or unsuccessful pathfinding execution. This represented the response to the user from OSCARS.

The Circuit Reservation Specification is a collection of Pipes, Junctions, and Fixtures, as described by the :ref:`plumbing`.

Relevant API Specification
--------------------------

- :java:ref:`ReservedBlueprintE`
- :java:ref:`ReservedVlanFlowE`
- :java:ref:`ReservedVlanJunctionE`
- :java:ref:`ReservedVlanFixtureE`
- :java:ref:`ReservedEthPipeE`
- :java:ref:`ReservedMplsPipeE`
- :java:ref:`ReservedBandwidthE`
- :java:ref:`ReservedVlanE`
- :java:ref:`ReservedPssResourceE`

