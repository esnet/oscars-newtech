.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.bwavail BandwidthAvailabilityRequest

.. java:import:: net.es.oscars.dto.bwavail BandwidthAvailabilityResponse

.. java:import:: net.es.oscars.dto.bwavail PortBandwidthAvailabilityRequest

.. java:import:: net.es.oscars.dto.bwavail PortBandwidthAvailabilityResponse

.. java:import:: net.es.oscars.dto.spec PalindromicType

.. java:import:: net.es.oscars.dto.spec SurvivabilityType

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.dto.topo.enums UrnType

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.helpers RequestedEntityBuilder

.. java:import:: net.es.oscars.helpers ReservedEntityDecomposer

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.resv.dao ReservedBandwidthRepository

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent BidirectionalPathE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.time Instant

.. java:import:: java.util.stream Collectors

BandwidthAvailabilityService
============================

.. java:package:: net.es.oscars.bwavail.svc
   :noindex:

.. java:type:: @Slf4j @Service public class BandwidthAvailabilityService

Methods
-------
getBandwidthAvailabilityMap
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public BandwidthAvailabilityResponse getBandwidthAvailabilityMap(BandwidthAvailabilityRequest request)
   :outertype: BandwidthAvailabilityService

   Given a bandwidth availability request, return a response that contains a mapping of the minimum available bandwidth between the requested source & destination at different points between the requested start and end times.

   :param request: - The bandwidth availability request
   :return: The matching Bandwidth availability response

getBandwidthAvailabilityOnAllPorts
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public PortBandwidthAvailabilityResponse getBandwidthAvailabilityOnAllPorts(PortBandwidthAvailabilityRequest bwRequest)
   :outertype: BandwidthAvailabilityService

