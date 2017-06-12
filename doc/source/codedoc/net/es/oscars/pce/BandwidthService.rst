.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.resv.dao ReservedBandwidthRepository

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent ReservableBandwidthE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.time Instant

.. java:import:: java.util.stream Collectors

BandwidthService
================

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Service @Component public class BandwidthService

Methods
-------
amendBandwidthAvailabilityMap
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, Map<String, Integer>> amendBandwidthAvailabilityMap(Map<String, Map<String, Integer>> availBwMap, List<ReservedBandwidthE> newBandwidths)
   :outertype: BandwidthService

   Update a current bandwidth availability map with the new reserved bandwidth additions.

   :param availBwMap: - Current map of "Ingress" and "Egress" bandwidth available for each/specific URNs
   :param newBandwidths: - A list of reserved bandwidths that will be added to this map
   :return: The updated map

buildBandwidthAvailabilityMapForUrn
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, Integer> buildBandwidthAvailabilityMapForUrn(String urn, ReservableBandwidthE capacity, Map<String, List<ReservedBandwidthE>> resvBwMap)
   :outertype: BandwidthService

   Determine how much Ingress/Egress bandwidth is still available at a URN. If the list of reserved bandwidths is empty, then all of the Reservable Bandwidth at that URN is available. Otherwise, subtract the sum Ingress/Egress bandwidth from the maximum reservable bandwidth at that URN.

   :param capacity: - ReservableBandwidthE object, which contains the maximum Ingress/Egress bandwidth for a given URN
   :param resvBwMap: - A Mapping from a URN to a list of Reserved Bandwidths at that URN.
   :return: A mapping Ingress/Egress bandwidth availability {Ingress: num, Egress: num}

buildBandwidthAvailabilityMapFromUrnList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, Map<String, Integer>> buildBandwidthAvailabilityMapFromUrnList(List<ReservedBandwidthE> rsvBandwidths, List<UrnE> urns)
   :outertype: BandwidthService

   Build a map of the available bandwidth at each URN. For each URN, there is a map of "Ingress" and "Egress" bandwidth available. Only port URNs can be found in this map. URNs are passed in.

   :param rsvBandwidths: - A list of all bandwidth reserved so far
   :param urns: - A list of UrnE objects
   :return: A mapping of URN name to Ingress/Egress bandwidth availability {urn = {Ingress: num, Egress: num}}

buildBandwidthAvailabilityMapFromUrnRepo
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, Map<String, Integer>> buildBandwidthAvailabilityMapFromUrnRepo(List<ReservedBandwidthE> rsvBandwidths)
   :outertype: BandwidthService

   Build a map of the available bandwidth at each URN. For each URN, there is a map of "Ingress" and "Egress" bandwidth available. Only port URNs can be found in this map. Retrieves URNs from the repository.

   :param rsvBandwidths: - A list of all bandwidth reserved so far
   :return: A mapping of URN name to Ingress/Egress bandwidth availability {urn = {Ingress: num, Egress: num}}

buildRequestedBandwidthMap
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<TopoVertex, Map<String, Integer>> buildRequestedBandwidthMap(List<List<TopoEdge>> EROs, List<Integer> bandwidths, RequestedVlanPipeE reqPipe)
   :outertype: BandwidthService

   Build a map of the requested bandwidth at each port TopoVertex contained within the passed in EROs

   :param EROs: - List of paths
   :param bandwidths: - List of bandwidths
   :return: A mapping from TopoVertex (ports only) to requested "Ingress" and "Egress" bandwidth

buildRequestedFixtureBandwidthMap
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, Map<String, Integer>> buildRequestedFixtureBandwidthMap(Set<RequestedVlanFixtureE> fixtures)
   :outertype: BandwidthService

buildReservedBandwidthMap
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, List<ReservedBandwidthE>> buildReservedBandwidthMap(List<ReservedBandwidthE> rsvBwList)
   :outertype: BandwidthService

   Build a mapping of UrnE objects to ReservedBandwidthE objects.

   :param rsvBwList: A list of all bandwidth reserved.
   :return: A map of UrnE to ReservedBandwidthE objects

createReservedBandwidthList
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedBandwidthE> createReservedBandwidthList(Set<ReservedVlanJunctionE> reservedJunctions, Set<ReservedMplsPipeE> reservedMplsPipes, Set<ReservedEthPipeE> reservedEthPipes, Date start, Date end)
   :outertype: BandwidthService

   Given a set of reserved junctions and reserved MPLS / ETHERNET pipes, extract the reserved bandwidth objects which fall within the requested schedule period and return them all together as a list

   :param reservedJunctions: - Set of reserved ethernet junctions
   :param reservedMplsPipes: - Set of reserved MPLS pipes
   :param reservedEthPipes: - Set of reserved Ethernet pipes
   :param start: - The requested start date
   :param end: - The requested end date
   :return: List of all reserved bandwidth contained within reserved pipes and the reserved repository.

evaluateBandwidthEROBi
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean evaluateBandwidthEROBi(Map<String, UrnE> urnMap, Integer azMbps, Integer zaMbps, List<TopoEdge> azERO, List<TopoEdge> zaERO, Map<String, Map<String, Integer>> availBwMap)
   :outertype: BandwidthService

   Examine all AZ and ZA edges, confirm that the requested bandwidth can be supported given the available bandwidth.

   :param urnMap: - A map of URN string to URN objects
   :param azERO: - The path in the A->Z direction
   :param zaERO: - The path in the Z->A direction
   :param availBwMap: - A map of bandwidth availability
   :return: True, if there is sufficient bandwidth across all edges. False, otherwise.

evaluateBandwidthEROUni
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean evaluateBandwidthEROUni(List<TopoEdge> ERO, Map<String, UrnE> urnMap, Map<String, Map<String, Integer>> availBwMap, Integer bwMbps)
   :outertype: BandwidthService

   Given a list of edges, confirm that there is sufficient bandwidth available in the one direction

   :param ERO: - A series of edges
   :param urnMap: - A mapping of URN strings to URN objects
   :param availBwMap: - A mapping of URN objects to lists of reserved bandwidth for that URN
   :param bwMbps: - The bandwidth in the specified direction
   :return: True, if the segment can support the requested bandwidth. False, otherwise.

evaluateBandwidthEdge
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean evaluateBandwidthEdge(TopoEdge edge, Integer azBw, Integer zaBw, Map<String, UrnE> urnMap, Map<String, Map<String, Integer>> availBwMap, Map<String, Map<String, Integer>> requestedFixtureBwMap)
   :outertype: BandwidthService

   Evaluate an edge to determine if the nodes on either end of the edge support the requested az and za bandwidths. An edge will only fail the test if one or both URNs (corresponding to the nodes): (1) have valid reservable bandwidth fields, and (2) the URN(s) do not have sufficient available bandwidth available in both the az and za directions (egress and ingress).

   :param edge: - The edge to be evaluated.
   :param azBw: - The requested bandwidth in one direction.
   :param zaBw: - The requested bandwidth in the other direction.
   :param urnMap: - Map of URN name to UrnE object.
   :param availBwMap: - Map of UrnE objects to "Ingress" and "Egress" Available Bandwidth
   :param requestedFixtureBwMap: - Map of requested bandwidth for each fixture. Can be different from az and za azbw.
   :return: True if there is sufficient reservable bandwidth, False otherwise.

evaluateBandwidthEdgeUni
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean evaluateBandwidthEdgeUni(TopoEdge edge, Integer theBw, Map<String, UrnE> urnMap, Map<String, Map<String, Integer>> availBwMap, Map<String, Map<String, Integer>> requestedFixtureBwMap)
   :outertype: BandwidthService

   Evaluate an edge to determine if the nodes on either end of the edge support the requested unidirectional bandwidth. An edge will only fail the test if one or both URNs (corresponding to the nodes): (1) have valid reservable bandwidth fields, and (2) the URN(s) do not have sufficient available bandwidth available in the unique direction.

   :param edge: - The edge to be evaluated.
   :param theBw: - The requested bandwidth in one direction.
   :param urnMap: - Map of URN name to UrnE object.
   :param availBwMap: - Map of UrnE objects to "Ingress" and "Egress" Available Bandwidth
   :param requestedFixtureBwMap: - Map of requested bandwidth for each fixture. Can be different from az and za azbw.
   :return: True if there is sufficient reservable bandwidth, False otherwise.

evaluateBandwidthFixtures
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean evaluateBandwidthFixtures(RequestedVlanPipeE reqPipe, Map<String, Map<String, Integer>> availBwMap)
   :outertype: BandwidthService

evaluateBandwidthJunction
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean evaluateBandwidthJunction(RequestedVlanJunctionE req_j, Map<String, Map<String, Integer>> bwAvailMap)
   :outertype: BandwidthService

   Confirm that a requested VLAN junction supports the requested bandwidth. Checks each fixture of the junction.

   :param req_j: - The requested junction.
   :param bwAvailMap: - Map of available bandwidth at each URN
   :return: True, if there is enough bandwidth at every fixture. False, otherwise.

evaluateBandwidthSharedURN
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean evaluateBandwidthSharedURN(String urn, Map<String, Map<String, Integer>> availBwMap, Integer inMbpsAZ, Integer egMbpsAZ, Integer inMbpsZA, Integer egMbpsZA)
   :outertype: BandwidthService

   Given a specific URN, which is traversed in the same direction by the forward and reverse path, determine if there is enough bandwidth available to support the requested bandwidth

   :param urn: - The URN
   :param availBwMap: - Map of URNs to available Bandwidth
   :param inMbpsAZ: - Requested ingress Mbps in the A->Z direction
   :param egMbpsAZ: - Requested egress Mbps in the A->Z direction
   :param inMbpsZA: - Requested ingress Mbps in the Z->A direction
   :param egMbpsZA: - Requested egress Mbps in the Z->A direction
   :return: True, if there is enough available bandwidth at the URN. False, otherwise

evaluateBandwidthURN
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean evaluateBandwidthURN(String urn, Map<String, Map<String, Integer>> availBwMap, Integer inMbps, Integer egMbps)
   :outertype: BandwidthService

   Given a specific URN, determine if there is enough bandwidth available to support the requested bandwidth

   :param urn: - The URN
   :param availBwMap: - Map of URNs to available Bandwidth
   :param inMbps: - Requested ingress Mbps
   :param egMbps: - Requested egress Mbps
   :return: True, if there is enough available bandwidth at the URN. False, otherwise

getReservedBandwidthFromRepo
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedBandwidthE> getReservedBandwidthFromRepo(Date start, Date end)
   :outertype: BandwidthService

   Get a list of all bandwidth reserved between a start and end date/time.

   :param start: - The start of the time range
   :param end: - The end of the time range
   :return: A list of reserved bandwidth

getReservedBandwidthsFromEthPipes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedBandwidthE> getReservedBandwidthsFromEthPipes(Set<ReservedEthPipeE> reservedPipes)
   :outertype: BandwidthService

   Retrieve all Reserved Bandwidth from a set of reserved Ethernet pipes.

   :param reservedPipes: - Set of reserved pipes
   :return: A list of all reserved bandwidth within the set of reserved Ethernet pipes.

getReservedBandwidthsFromJunctions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedBandwidthE> getReservedBandwidthsFromJunctions(Set<ReservedVlanJunctionE> junctions)
   :outertype: BandwidthService

   Retrieve all reserved bandwidths from a set of reserved junctions.

   :param junctions: - Set of reserved junctions.
   :return: A list of all bandwidth reserved at those junctions.

getReservedBandwidthsFromMplsPipes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedBandwidthE> getReservedBandwidthsFromMplsPipes(Set<ReservedMplsPipeE> reservedPipes)
   :outertype: BandwidthService

   Retrieve all Reserved Bandwidth from a set of reserved MPLS pipes.

   :param reservedPipes: - Set of reserved pipes
   :return: A list of all reserved bandwidth within the set of reserved MPLS pipes.

