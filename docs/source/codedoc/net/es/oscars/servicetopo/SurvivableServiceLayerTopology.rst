.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Builder

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums PortLayer

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.pce BhandariPCE

.. java:import:: net.es.oscars.pce PruningService

.. java:import:: net.es.oscars.resv.ent RequestedVlanPipeE

.. java:import:: net.es.oscars.resv.ent ReservedVlanE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util.stream Collectors

SurvivableServiceLayerTopology
==============================

.. java:package:: net.es.oscars.servicetopo
   :noindex:

.. java:type:: @Slf4j @Data @Builder @Component @AllArgsConstructor @NoArgsConstructor public class SurvivableServiceLayerTopology

Methods
-------
buildLogicalLayerDstNodes
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void buildLogicalLayerDstNodes(TopoVertex dstDevice, TopoVertex dstOutPort)
   :outertype: SurvivableServiceLayerTopology

   Adds a VIRTUAL port onto the Service-layer to represent a request's terminating node which is on the MPLS-layer. This is necessary since if the request is destined on the MPLS-layer, it has no foothold on the service-layer; VIRTUAL nodes are dummy hooks. A bidirectional zero-cost link is added between the VIRTUAL port and MPLS-layer dstOutPort. If the specified topology nodes are already on the Service-layer, this method does nothing to modify the Service-layer topology.

   :param dstDevice: - Request's destination device
   :param dstOutPort: - Request's destination port

buildLogicalLayerSrcNodes
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void buildLogicalLayerSrcNodes(TopoVertex srcDevice, TopoVertex srcInPort)
   :outertype: SurvivableServiceLayerTopology

   Adds a VIRTUAL port onto the Service-layer to represent a request's starting node which is on the MPLS-layer. This is necessary since if the request is sourced on the MPLS-layer, it has no foothold on the service-layer; VIRTUAL nodes are dummy hooks. A bidirectional zero-cost link is added between the VIRTUAL port and MPLS-layer srcInPort. If the specified topology nodes are already on the Service-layer, this method does nothing to modify the Service-layer topology.

   :param srcDevice: - Request's source device
   :param srcInPort: - Request's source port

calculateLogicalLinkWeights
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void calculateLogicalLinkWeights(RequestedVlanPipeE requestedVlanPipe, List<UrnE> urnList, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList, int numDisjoint)
   :outertype: SurvivableServiceLayerTopology

   Calls PruningService and DijkstraPCE methods to compute shortest MPLS-layer path-pair, calculates the combined weight of each path, and maps them to the appropriate logical links.

   :param requestedVlanPipe: - Request pipe
   :param urnList: - List of URNs in the network; Necessary for passing to PruningService methods
   :param bwAvailMap: - A map of available "Ingress" and 'Egress" bandwidth for each URN.
   :param rsvVlanList: - List of currently reserved VLAN elements (during request schedule)

createMultilayerTopology
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void createMultilayerTopology()
   :outertype: SurvivableServiceLayerTopology

   Managing method in charge of constructing the multi-layer service-topology. Divides physical topology into two layers: MPLS-only layer, and Service-layer: MPLS-only layer contains: all MPLS devices, adjacent ports, INTERNAL links between MPLSdevices-MPLSports, links between MPLS-ports.

getActualEroList
^^^^^^^^^^^^^^^^

.. java:method:: public List<List<TopoEdge>> getActualEroList(List<TopoEdge> serviceLayerERO, int numDisjoint)
   :outertype: SurvivableServiceLayerTopology

   Gets MPLS-Layer A-Z ERO given a Service-layer ERO, which possibly contains LOGICAL edges

   :param serviceLayerERO: - Service-layer ERO; may contain LOGICAL edges
   :param numDisjoint: - Request's K-disjoint path requirement
   :return: Corresponding physical ERO

getSLTopology
^^^^^^^^^^^^^

.. java:method:: public Topology getSLTopology()
   :outertype: SurvivableServiceLayerTopology

   Get the service-layer topology, including: ETHERNET devices, VIRTUAL devices, ETHERNET ports, VIRTUAL ports, INTERNAL links, ETHERNET links, LOGICAL links

   :return: Service-Layer topology as a combined Topology object

getVirtualNode
^^^^^^^^^^^^^^

.. java:method:: public TopoVertex getVirtualNode(TopoVertex realNode)
   :outertype: SurvivableServiceLayerTopology

   Looks up a given MPLS-Layer node to find the corresponding VIRTUAL Service-layer node.

   :param realNode: - Physical node for which to find corresponding VIRTUAL node.
   :return: the appropriate VIRTUAL node, or null is no such node exists.

resetLogicalLinks
^^^^^^^^^^^^^^^^^

.. java:method:: public void resetLogicalLinks()
   :outertype: SurvivableServiceLayerTopology

   Doesn't destroy logical links, but resets cost metrics to 0, and clears the corresponding phyical TopoEdges (MPLS-ERO) lists. This needs to be done, for example, prior to every call to calculateLogicalLinkWeights().

setTopology
^^^^^^^^^^^

.. java:method:: public void setTopology(Topology topology)
   :outertype: SurvivableServiceLayerTopology

   Assigns the passed in topology to the appropriate layer's global class variable

   :param topology: - Single-layer topology; Pre-managed and altered if necessary

