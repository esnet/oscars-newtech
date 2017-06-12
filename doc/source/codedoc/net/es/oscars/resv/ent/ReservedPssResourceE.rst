.. java:import:: net.es.oscars.dto.resv ResourceType

.. java:import:: java.time Instant

ReservedPssResourceE
====================

.. java:package:: net.es.oscars.resv.ent
   :noindex:

.. java:type:: @Data @Builder @NoArgsConstructor @AllArgsConstructor @Entity public class ReservedPssResourceE

Methods
-------
makeQosIdResource
^^^^^^^^^^^^^^^^^

.. java:method:: public static ReservedPssResourceE makeQosIdResource(String deviceUrn, Integer qosId, ResourceType rt, Instant beginning, Instant ending)
   :outertype: ReservedPssResourceE

makeSdpIdResource
^^^^^^^^^^^^^^^^^

.. java:method:: public static ReservedPssResourceE makeSdpIdResource(String deviceUrn, Integer sdpId, Instant beginning, Instant ending)
   :outertype: ReservedPssResourceE

makeSvcIdResource
^^^^^^^^^^^^^^^^^

.. java:method:: public static ReservedPssResourceE makeSvcIdResource(String deviceUrn, Integer svcId, Instant beginning, Instant ending)
   :outertype: ReservedPssResourceE

makeVcIdResource
^^^^^^^^^^^^^^^^

.. java:method:: public static ReservedPssResourceE makeVcIdResource(Integer vcId, Instant beginning, Instant ending)
   :outertype: ReservedPssResourceE

