.. _schedspec:

Circuit Schedule Specification
==============================

An OSCARS circuit is decomposed roughly into three components: The :ref:`requestspec`, the *Circuit Schedule Specification*, and the :ref:`resvspec`, and the *Circuit Reservation Specification*.  The Schedule Specification describes the timing attributes of the circuit. The requested circuit may be composed of multiple elemnts which combine into a :ref:`plumbing`. Each of these pipes is handled by the :ref:`pce_doc` for independent pathfinding solutions and various :ref:`advanced_service` as appropriate.  However, all plumbing elements correspond to a single set of scheduling parameters as described by this specification component. Relevant parameters are described in the following table.

============== ========= ============ ==================
Parameter Name Data Type Value Set by Description
============== ========= ============ ==================
StartDate      Date      User         Date/Time describing initialization of intended circuit reservation
EndDate        Date      User         Date/Time describing completion of intended circuit reservation
Duration       Long      User         Total duration of requested circuit in minutes
Submitted      Date      System       Date/Time of submitted :ref:`requestspec`
Setup          Date      System       Date/Time of actual circuit establishment on the network
Teardown       Date      System       Date/Time of actual circuit termination on the network
============== ========= ============ ==================

The network resources available to a given circuit are time-dependent. For example, if the user requests a circuit during a period of high traffic activity, there is a greater likelihood that some overlapping elephant flows may have already been guaranteed reservation of elements requested for the new circuit. In such a case, OSCARS will perform its pathfinding efforts in order to identify a working solution that meets the requested criteria, but if there is no suitable set of resources, the request will fail. Alternatively, if the new request coincides with a time of relatively low traffic load, efficient solutions are more likely to be computed by the system.

.. warning::

	If necessary network resources, such as bandwidth or VLAN tags are unavailable throughout the **entire** circuit schedule, reservation will fail. 


Relevant API Specification
--------------------------


- :java:ref:`SpecificationE`
- :java:ref:`ScheduleSpecificationE`
- :java:ref:`ScheduleE`
