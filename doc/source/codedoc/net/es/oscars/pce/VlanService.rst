.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto IntRange

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.helpers IntRangeParsing

.. java:import:: net.es.oscars.resv.dao ReservedVlanRepository

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent IntRangeE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.dto.topo.enums DeviceType

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums UrnType

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.util.stream Collectors

VlanService
===========

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Service @Component public class VlanService

Methods
-------
addToSetOverlap
^^^^^^^^^^^^^^^

.. java:method:: public Set<Integer> addToSetOverlap(Set<Integer> overlap, Set<Integer> other)
   :outertype: VlanService

   Iterate through the set of overlapping VLAN tags, keep the elements in that set which are also contained in every IntRange passed in.

   :param overlap: - The set of overlapping VLAN tags.
   :param other: - Another set of VLAN tags, to be overlapped with the current overlapping set.
   :return: The (possibly reduced) set of overlapping VLAN tags.

buildAvailableVlanIdMap
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, Set<Integer>> buildAvailableVlanIdMap(Map<String, UrnE> urnMap, List<ReservedVlanE> reservedVlans, Map<String, String> portToDeviceMap)
   :outertype: VlanService

createReservedVlanList
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedVlanE> createReservedVlanList(Set<ReservedVlanJunctionE> reservedJunctions, Set<ReservedEthPipeE> reservedEthPipes)
   :outertype: VlanService

   Given a set of reserved junctions and reserved ethernet pipes, retrieve all reserved VLAN objects within the specified schedule period.

   :param reservedJunctions: - Set of reserved junctions
   :param reservedEthPipes: - Set of reserved ethernet pipes

createReservedVlanListFromJunctionsPipesRepo
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedVlanE> createReservedVlanListFromJunctionsPipesRepo(Set<ReservedVlanJunctionE> reservedJunctions, Set<ReservedEthPipeE> reservedEthPipes, Date start, Date end)
   :outertype: VlanService

   Given a set of reserved junctions and reserved ethernet pipes, retrieve all reserved VLAN objects from Repo within the specified schedule period and from the reserved junctions/pipes.

   :param reservedJunctions: - Set of reserved junctions
   :param reservedEthPipes: - Set of reserved ethernet pipes

findEdgeSetPerVlanID
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<Integer, Set<TopoEdge>> findEdgeSetPerVlanID(Set<TopoEdge> edges, Map<String, UrnE> urnMap, Map<String, Set<Integer>> availVlanMap)
   :outertype: VlanService

   Traverse the set of edges, and create a map of VLAN ID to the edges where that ID is available

   :param edges: - The set of edges.
   :param urnMap: - Map of URN name to UrnE object.
   :return: A (possibly empty) set of VLAN tags that are available across every edge.

findMaxValidEdgeSet
^^^^^^^^^^^^^^^^^^^

.. java:method:: public Set<TopoEdge> findMaxValidEdgeSet(Set<TopoEdge> availableEdges, Map<String, UrnE> urnMap, List<IntRange> vlans, Map<String, Set<Integer>> availVlanMap)
   :outertype: VlanService

   Return a pruned set of edges where the nodes on either end of the edge support at least one of the specified VLANs. A map of URNs are used to retrive the reservable VLAN sets from nodes (where applicable). Builds a mapping from each available VLAN id to the set of edges that support that ID. Using this mapping, the largest set of edges that supports a requested VLAN id (or any VLAN id if none are specified) is returned.

   :param availableEdges: - The set of currently available edges, which will be pruned further using VLAN tags.
   :param urnMap: - Map of URN name to UrnE object.
   :param vlans: - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
   :param availVlanMap: - Map of UrnE objects to a List<> of available VLAN tags.
   :return: The input edges, pruned using the input set of VLAN tags.

findSetCover
^^^^^^^^^^^^

.. java:method:: public Set<Integer> findSetCover(Map<String, Set<Integer>> validVlanMap)
   :outertype: VlanService

getAvailableVlanIds
^^^^^^^^^^^^^^^^^^^

.. java:method:: public Set<Integer> getAvailableVlanIds(String urn, Map<String, Set<ReservedVlanE>> resvVlanMap, Map<String, Set<String>> deviceToPortMap, Map<String, UrnE> urnMap)
   :outertype: VlanService

   Get all VLAN IDs available at a URN based on the possible reservable ranges and the currently reserved IDs.

   :param urn: - The currently considered URN string
   :param resvVlanMap: - A mapping representing the Reserved VLANs at a URN
   :return: Set of VLAN IDs that are both supported and not reserved at a URN

getIntRangesFromString
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<IntRange> getIntRangesFromString(String vlans)
   :outertype: VlanService

   Given a list of strings, convert all valid strings into IntRanges.

   :param vlans: - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
   :return: A list of IntRanges, each representing a range of VLAN ID values parsed from a string.

getIntegersFromRanges
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Set<Integer> getIntegersFromRanges(List<IntRange> vlans)
   :outertype: VlanService

   Return all of the VLAN ids contained within the list of VLAN ranges.

   :param vlans: - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
   :return: The set of VLAN ids making up the input ranges.

getReservedVlansFromEthernetPipes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedVlanE> getReservedVlansFromEthernetPipes(Set<ReservedEthPipeE> reservedPipes)
   :outertype: VlanService

   Retrieve all Reserved VLAN IDs from a set of reserved pipes.

   :param reservedPipes: - Set of reserved pipes
   :return: A list of all reserved VLAN IDs within the set of reserved pipes.

getReservedVlansFromJunctions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedVlanE> getReservedVlansFromJunctions(Set<ReservedVlanJunctionE> junctions)
   :outertype: VlanService

   Retrieve all reserved VLAN IDs from a set of reserved junctions

   :param junctions: - Set of reserved junctions.
   :return: A list of all VLAN IDs reserved at those junctions.

getReservedVlansFromRepo
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedVlanE> getReservedVlansFromRepo(Date start, Date end)
   :outertype: VlanService

   Get a list of all VLAN IDs reserved between a start and end date/time.

   :param start: - The start of the time range
   :param end: - The end of the time range
   :return: A list of reserved VLAN IDs

getSetOfNumbersInRange
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Set<Integer> getSetOfNumbersInRange(IntRange aRange)
   :outertype: VlanService

   Retrieve a set of all Integers that fall within the specified IntRange.

   :param aRange: - The specified IntRange
   :return: The set of Integers contained within the range.

getVlanRangesFromUrnString
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<IntRange> getVlanRangesFromUrnString(Map<String, UrnE> urnMap, String matchingUrn)
   :outertype: VlanService

   Using the list of URNs and the URN string, find the matching UrnE object and retrieve all of its reservable IntRanges.

   :param urnMap: - Map of URN name to UrnE object.
   :param matchingUrn: - String representation of the desired URN.
   :return: - All IntRanges supported at the URN.

getVlansFromJunction
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<IntRange> getVlansFromJunction(RequestedVlanJunctionE junction)
   :outertype: VlanService

   Get the requested set of VLAN tags from a junction by streaming through the fixtures in the junction.

   :param junction: - The requested VLAN junction.
   :return: The set of VLAN tags (Integers) requested for fixtures at that junction.

selectVLANsForJunction
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, Set<Integer>> selectVLANsForJunction(RequestedVlanJunctionE req_j, List<ReservedVlanE> rsvVlans, Map<String, Set<String>> deviceToPortMap, Map<String, String> portToDeviceMap, Map<String, UrnE> urnMap)
   :outertype: VlanService

   Select a VLAN ID for a junction. All fixtures on the junction must use their requested VLAN tag.

   :param req_j: - The requested junction.
   :param deviceToPortMap:
   :param portToDeviceMap: @return A valid VLAN iD for each URN on this junction.

selectVlansForPipe
^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, Set<Integer>> selectVlansForPipe(RequestedVlanPipeE reqPipe, Map<String, UrnE> urnMap, List<ReservedVlanE> reservedVlans, List<TopoEdge> azERO, List<TopoEdge> zaERO, Map<String, Set<String>> deviceToPortMap, Map<String, String> portToDeviceMap)
   :outertype: VlanService

   Return a mapping of URN entity to VLAN Integer ID. This map will contain the URNs of all fixtures, junctions, and ports in-between the junction.

   :param reqPipe: - The requested pipe.
   :param urnMap: - A mapping of URN string to URN entity
   :param reservedVlans: - List of all reserved VLANs
   :param azERO: - The AZ edges
   :param zaERO: - The ZA edges
   :param deviceToPortMap:
   :param portToDeviceMap: @return Map from URN entity to VLAN ID chosen for that entity.

stringifyVlanMap
^^^^^^^^^^^^^^^^

.. java:method:: public String stringifyVlanMap(Map<String, Set<Integer>> input)
   :outertype: VlanService

