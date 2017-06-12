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

.. java:import:: net.es.oscars.pce DijkstraPCE

.. java:import:: net.es.oscars.pce PruningService

.. java:import:: net.es.oscars.resv.ent RequestedVlanPipeE

.. java:import:: net.es.oscars.resv.ent ReservedVlanE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util.stream Collectors

ServiceLayerTopology
====================

.. java:package:: net.es.oscars.servicetopo
   :noindex:

.. java:type:: @Slf4j @Data @Builder @Component @AllArgsConstructor @NoArgsConstructor public class ServiceLayerTopology

Methods
-------
buildLogicalLayerDstNodes
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void buildLogicalLayerDstNodes(TopoVertex dstDevice, TopoVertex dstOutPort)
   :outertype: ServiceLayerTopology

   Adds a VIRTUAL port onto the Service-layer to represent a request's terminating node which is on the MPLS-layer. This is necessary since if the request is destined on the MPLS-layer, it has no foothold on the service-layer; VIRTUAL nodes are dummy hooks. A bidirectional zero-cost link is added between the VIRTUAL port and MPLS-layer dstOutPort. If the specified topology nodes are already on the Service-layer, this method does nothing to modify the Service-layer topology.

   :param dstDevice: - Request's destination device
   :param dstOutPort: - Request's destination port

buildLogicalLayerSrcNodes
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void buildLogicalLayerSrcNodes(TopoVertex srcDevice, TopoVertex srcInPort)
   :outertype: ServiceLayerTopology

   Adds a VIRTUAL port onto the Service-layer to represent a request's starting node which is on the MPLS-layer. This is necessary since if the request is sourced on the MPLS-layer, it has no foothold on the service-layer; VIRTUAL nodes are dummy hooks. A bidirectional zero-cost link is added between the VIRTUAL port and MPLS-layer srcInPort. If the specified topology nodes are already on the Service-layer, this method does nothing to modify the Service-layer topology.

   :param srcDevice: - Request's source device
   :param srcInPort: - Request's source port

calculateLogicalLinkWeights
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void calculateLogicalLinkWeights(RequestedVlanPipeE requestedVlanPipe, List<UrnE> urnList, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList)
   :outertype: ServiceLayerTopology

   Managing method - Determines whether to perform logical edge weight computation Symmetrically or Asymmetrically. This method may no longer be necessary, since the Symmetric subroutine was too naive to work in general cases.

   :param requestedVlanPipe: - Request pipe
   :param urnList: - List of URNs in the network; Necessary for passing to PruningService methods
   :param bwAvailMap: - Map of available bandwidth for "Ingress" and "Egress" direction for each URN.
   :param rsvVlanList: - List of currently reserved VLAN elements (during request schedule)

createMultilayerTopology
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void createMultilayerTopology()
   :outertype: ServiceLayerTopology

   Managing method in charge of constructing the multi-layer service-topology. Divides physical topology into two layers: MPLS-only layer, and Service-layer: MPLS-only layer contains: all MPLS devices, adjacent ports, INTERNAL links between MPLSdevices-MPLSports, links between MPLS-ports.

getActualERO
^^^^^^^^^^^^

.. java:method:: public List<TopoEdge> getActualERO(List<TopoEdge> serviceLayerERO)
   :outertype: ServiceLayerTopology

   Gets MPLS-Layer ERO given a Service-layer ERO, which possibly contains LOGICAL edges

   :param serviceLayerERO: - Service-layer ERO; may contain LOGICAL edges
   :return: Corresponding physical ERO

getActualEROAZ
^^^^^^^^^^^^^^

.. java:method:: public List<TopoEdge> getActualEROAZ(List<TopoEdge> serviceLayerERO)
   :outertype: ServiceLayerTopology

   Same as getActualERO, except returns only the physical ERO as constrained by the requested A->Z b/w.

   :param serviceLayerERO: - Service-layer ERO; may contain LOGICAL edges
   :return: Corresponding physical ERO, constrained by requested A->Z b/w.

getActualEROZA
^^^^^^^^^^^^^^

.. java:method:: public List<TopoEdge> getActualEROZA(List<TopoEdge> serviceLayerERO)
   :outertype: ServiceLayerTopology

   Same as getActualERO, except returns only the physical ERO as constrained by the requested Z->A b/w.

   :param serviceLayerERO: - Service-layer ERO; may contain LOGICAL edges
   :return: Corresponding physical ERO, constrained by requested Z->A b/w.

getSLTopology
^^^^^^^^^^^^^

.. java:method:: public Topology getSLTopology()
   :outertype: ServiceLayerTopology

   Get the service-layer topology, including: ETHERNET devices, VIRTUAL devices, ETHERNET ports, VIRTUAL ports, INTERNAL links, ETHERNET links, LOGICAL links

   :return: Service-Layer topology as a combined Topology object

getSLTopologyAZ
^^^^^^^^^^^^^^^

.. java:method:: public Topology getSLTopologyAZ()
   :outertype: ServiceLayerTopology

   Same as getSLTopology, except the logical link metrics and corresponding MPLS-layer EROs are assigned using the requested A->Z b/w constraint.

   :return: Service-Layer topology as a combined Topology object

getSLTopologyZA
^^^^^^^^^^^^^^^

.. java:method:: public Topology getSLTopologyZA()
   :outertype: ServiceLayerTopology

   Same as getSLTopology, except the logical link metrics and corresponding MPLS-layer EROs are assigned using the requested Z->A b/w constraint.

   :return: Service-Layer topology as a combined Topology object

getVirtualNode
^^^^^^^^^^^^^^

.. java:method:: public TopoVertex getVirtualNode(TopoVertex realNode)
   :outertype: ServiceLayerTopology

   Looks up a given MPLS-Layer node to find the corresponding VIRTUAL Service-layer node.

   :param realNode: - Physical node for which to find corresponding VIRTUAL node.
   :return: the appropriate VIRTUAL node, or null is no such node exists.

resetLogicalLinks
^^^^^^^^^^^^^^^^^

.. java:method:: public void resetLogicalLinks()
   :outertype: ServiceLayerTopology

   Doesn't destroy logical links, but resets cost metrics to 0, and clears the corresponding phyical TopoEdges (MPLS-ERO) lists. This needs to be done, for example, prior to every call to calculateLogicalLinkWeights().

setTopology
^^^^^^^^^^^

.. java:method:: public void setTopology(Topology topology)
   :outertype: ServiceLayerTopology

   Assigns the passed in topology to the appropriate layer's global class variable

   :param topology: - Single-layer topology; Pre-managed and altered if necessary

