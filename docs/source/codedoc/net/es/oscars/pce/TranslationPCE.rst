.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.pss EthFixtureType

.. java:import:: net.es.oscars.dto.pss EthJunctionType

.. java:import:: net.es.oscars.dto.topo.enums DeviceModel

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums UrnType

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.pce.exc VlanNotFoundException

.. java:import:: net.es.oscars.pss PCEAssistant

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util.stream Collectors

TranslationPCE
==============

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Component public class TranslationPCE

Methods
-------
createFixtureAndResources
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public ReservedVlanFixtureE createFixtureAndResources(String portUrn, EthFixtureType fixtureType, Integer azMbps, Integer zaMbps, Set<Integer> vlanIds, Date start, Date end, String connectionId)
   :outertype: TranslationPCE

   Create a Reserved Fixtures and it's associated resources (VLAN and Bandwidth) using the input.

   :param portUrn: - The URN of the desired fixture
   :param fixtureType: - The typing of the desired fixture
   :param azMbps: - The requested ingress bandwidth
   :param zaMbps: - The requested egress bandwidth
   :param vlanIds: - The assigned VLAN IDs
   :param start: - The requested start date
   :param end: - The requested end date
   :return: The reserved fixture, containing all of its reserved resources

createJunctionAndFixtures
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public ReservedVlanJunctionE createJunctionAndFixtures(TopoVertex device, Map<String, UrnE> urnMap, Map<String, DeviceModel> deviceModels, Set<RequestedVlanJunctionE> requestedJunctions, Map<String, Set<Integer>> vlanMap, Map<String, String> portToDeviceMap, Date start, Date end, String connectionId) throws PSSException
   :outertype: TranslationPCE

   Create a Junction and it's associated fixtures given the input parameters.

   :param device: - The device vertex associated with the junction
   :param urnMap: - A mapping of URN strings to URN objects
   :param deviceModels: - A mapping of URN strings to Device Models
   :param requestedJunctions: - A set of requested junctions - used to determine attributes of junction/fixtures
   :param vlanMap: - Map of assigned VLANs for each fixture
   :param start: - The requested start date
   :param end: - The requested end date
   :throws PSSException:
   :return: A Reserved Vlan Junction with Reserved Fixtures (if contained in a matching requested junction)

createReservedBandwidth
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public ReservedBandwidthE createReservedBandwidth(String urn, Integer inMbps, Integer egMbps, Date start, Date end, String connectionId)
   :outertype: TranslationPCE

   Create the reserved bandwidth given the input parameters.

   :param urn: - The URN associated with this bandwidth
   :param inMbps: - The ingress bandwidth
   :param egMbps: - The egress bandwidth
   :param start: - The requested start
   :param end: - The requested end
   :return: A reserved bandwidth object

createReservedBandwidthForEROs
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Set<ReservedBandwidthE> createReservedBandwidthForEROs(List<TopoVertex> az, List<TopoVertex> za, Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap, Date start, Date end, String connectionId)
   :outertype: TranslationPCE

   Given two lists of EROS in the AZ and ZA direction, a map of URNs, a map of the requested bandwidth at each URN, and the requested schedule, return a combined set of reserved bandwidth objects for the AZ and ZA paths

   :param az: - The AZ vertices
   :param za: - The ZA vertices
   :param requestedBandwidthMap: - A mapping of Vertex objects to "Ingress"/"Egress" requested bandwidth
   :param start: - The requested start date
   :param end: - The requested end date
   :return: A set of all reserved bandwidth for every port (across both paths)

createReservedFixture
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public ReservedVlanFixtureE createReservedFixture(String urn, Set<ReservedPssResourceE> pssResources, Set<ReservedVlanE> rsvVlans, ReservedBandwidthE rsvBw, EthFixtureType fixtureType)
   :outertype: TranslationPCE

   Create a reserved fixture, given the input parameters.

   :param urn: - The fixture's URN
   :param pssResources: - The fixture's PSS Resources
   :param rsvVlans: - The fixture's assigned VLAN IDs
   :param rsvBw: - The fixture's assigned bandwidth
   :param fixtureType: - The fixture's type
   :return: The Reserved VLAN Fixture

createReservedJunction
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public ReservedVlanJunctionE createReservedJunction(String urn, Set<ReservedPssResourceE> pssResources, Set<ReservedVlanFixtureE> fixtures, EthJunctionType junctionType, Set<ReservedVlanE> reservedVlans)
   :outertype: TranslationPCE

   Create a reserved junction given the input

   :param urn: - The junction's URN
   :param pssResources: - The junction's PSS Resources
   :param fixtures: - The junction's fixtures
   :param junctionType: - The junction's type
   :return: The Reserved VLAN Junction

createReservedVlan
^^^^^^^^^^^^^^^^^^

.. java:method:: public ReservedVlanE createReservedVlan(String urn, Integer vlanId, Date start, Date end)
   :outertype: TranslationPCE

   Create the reserved VLAN ID given the input parameters.

   :param urn: - The URN associated with this VLAN
   :param vlanId: - The ID value for the VLAN tag
   :param start: - The requested start time
   :param end: - The requested end time
   :return: The reserved VLAN objct

createReservedVlanForEROs
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Set<ReservedVlanE> createReservedVlanForEROs(List<TopoVertex> az, List<TopoVertex> za, Map<String, UrnE> urnMap, Map<String, Set<Integer>> vlanMap, Date start, Date end)
   :outertype: TranslationPCE

   Given two lists of EROS in the AZ and ZA direction, a map of URNs, a chosen vlan ID, and the requested schedule, return a combined set of reserved VLAN objects for the AZ and ZA paths

   :param az: - The AZ vertices
   :param za: - The ZA vertices
   :param urnMap: - A mapping of URN string to URN object
   :param vlanMap: - A mapping of URN object to assigned VLAN ID
   :param start: - The requested start date
   :param end: - The requested end date
   :return: A set of all reserved VLAN objects for every port (across both paths)

reserveRequestedPipe
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public TranslationPCEResponse reserveRequestedPipe(RequestedVlanPipeE reqPipe, List<TopoEdge> azERO, List<TopoEdge> zaERO, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> reservedVlans, Map<String, Set<String>> deviceToPortMap, Map<String, String> portToDeviceMap, Date start, Date end, String connectionId) throws PCEException, PSSException
   :outertype: TranslationPCE

   Create a set of reserved pipes/junctions from a requested pipe. A requested pipe can produce: One pipe for each pair of Ethernet devices One pipe for each MPLS segment along the path This function will add to the reservedMplsPipes and reservedEthPipes sets passed in as input

   :param reqPipe: - THe requested pipe, containing details on the requested endpoints/bandwidth/VLANs
   :param azERO: - The physical path taken by the pipe in the A->Z direction
   :param zaERO: - The physical path taken by the pipe in the Z->A direction
   :param bwAvailMap: - Map of "Ingress" and "Egress" bandwidth availability at each URN
   :param reservedVlans: - The list of all VLAN IDs reserved so far
   :param deviceToPortMap: - Map of all ports per device
   :param portToDeviceMap: @throws PCEException
   :throws PSSException:

reserveSimpleJunction
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public ReservedVlanJunctionE reserveSimpleJunction(RequestedVlanJunctionE req_j, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> reservedVlans, Map<String, Set<String>> deviceToPortMap, Map<String, String> portToDeviceMap, Date start, Date end, String connectionId) throws PCEException, PSSException
   :outertype: TranslationPCE

   Creates a ReservedVlanJunctionE given a request for ingress/egress traffic within a device.

   :param req_j: - The requested junction
   :param deviceToPortMap: - Map from each device name to set of port names
   :param portToDeviceMap: - Map from each port name to parent device name
   :param start: - The requested start date
   :param end: - The requested end date
   :throws PSSException:
   :throws PCEException:
   :return: The Reserved Junction

testBandwidthRequirements
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void testBandwidthRequirements(RequestedVlanPipeE reqPipe, List<TopoEdge> azERO, List<TopoEdge> zaERO, Map<String, UrnE> urnMap, Integer azMbps, Integer zaMbps, Map<String, Map<String, Integer>> availBwMap, Map<TopoVertex, Map<String, Integer>> requestedBandwidthMap) throws PCEException
   :outertype: TranslationPCE

