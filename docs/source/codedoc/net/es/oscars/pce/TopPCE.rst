.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.spec SurvivabilityType

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo.enums DeviceType

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.pce.exc InvalidUrnException

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.spec PalindromicType

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent BidirectionalPathE

.. java:import:: net.es.oscars.topo.ent EdgeE

.. java:import:: net.es.oscars.topo.ent ReservableVlanE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.security.access.method P

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.time.temporal ChronoUnit

.. java:import:: java.util.stream Collectors

TopPCE
======

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Component public class TopPCE

Methods
-------
getDuration
^^^^^^^^^^^

.. java:method:: public Long getDuration(Date start, Date end)
   :outertype: TopPCE

makeReserved
^^^^^^^^^^^^

.. java:method:: public Optional<ReservedBlueprintE> makeReserved(RequestedBlueprintE requested, ScheduleSpecificationE schedSpec, List<Date> reservedSched) throws PCEException, PSSException
   :outertype: TopPCE

   Given a requested Blueprint (made up of a VLAN or Layer3 Flow) and a Schedule Specification, attempt to reserve available resources to meet the demand. If it is not possible, return an empty Optional

   :param requested: - Requested blueprint
   :param schedSpec: - Requested schedule
   :throws PSSException:
   :throws PCEException:
   :return: ReservedBlueprint containing the reserved resources, or an empty Optional if the reservation is not possible.

verifyRequested
^^^^^^^^^^^^^^^

.. java:method:: public void verifyRequested(RequestedBlueprintE requested) throws PCEException
   :outertype: TopPCE

   Confirm that the requested blueprint is valid.

   :param requested: The requested blueprint.
   :throws PCEException:

