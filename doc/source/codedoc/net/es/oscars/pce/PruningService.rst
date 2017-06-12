.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto IntRange

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

PruningService
==============

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Service @Component public class PruningService

   Pruning Service. This class can take in a variety of inputs (Bidrectional bandwidth, a pair of unidirectional bandwidths (AZ/ZA), specified set of desired VLAN tags, or a logical pipe (containing those other elements)). With any/all of those inputs, edges are removed from the passed in topology that do not meet the specified bandwidth/VLAN requirements.

Methods
-------
pruneBlacklist
^^^^^^^^^^^^^^

.. java:method:: public Set<TopoEdge> pruneBlacklist(Topology topology, Set<String> urnBlacklist)
   :outertype: PruningService

   Identifies topology edges that correspond to a given set of topology URNs, for removal from the topology.

   :param topology: - Full un-pruned topology containing all edges and vertices in the network
   :param urnBlacklist: - Set of URN strings corresponding to blacklisted devices/ports.
   :return: Set of blacklisted edges to be pruned from the topology.

pruneTopologyOfEdgePortsExcept
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void pruneTopologyOfEdgePortsExcept(Topology topo, Set<String> portsToKeep)
   :outertype: PruningService

pruneWithPipe
^^^^^^^^^^^^^

.. java:method:: public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList)
   :outertype: PruningService

   Prune the topology using a logical pipe. The pipe contains the requested bandwidth and VLANs (through querying the attached junctions/fixtures). The URNs are pulled from the URN repository.

   :param topo: - The topology to be pruned.
   :param pipe: - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
   :param bwAvailMap: - A map of available "Ingress" and 'Egress" bandwidth for each URN
   :param rsvVlanList: - A list of Reserved VLAN tags to be considered when pruning (along with VLANs in the Repo)
   :return: The topology with ineligible edges removed.

pruneWithPipe
^^^^^^^^^^^^^

.. java:method:: public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe, List<UrnE> urns, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList)
   :outertype: PruningService

   Prune the topology using a logical pipe. The pipe contains the requested bandwidth and VLANs (through querying the attached junctions/fixtures). A list of URNs is passed into match devices/interfaces to topology elements.

   :param topo: - The topology to be pruned.
   :param pipe: - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
   :param urns: - The URNs that will be used to match available resources with elements of the topology.
   :return: The topology with ineligible edges removed.

pruneWithPipeAZ
^^^^^^^^^^^^^^^

.. java:method:: public Topology pruneWithPipeAZ(Topology topo, RequestedVlanPipeE pipe, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList)
   :outertype: PruningService

   Prune the topology based on A->Z bandwidth using a logical pipe. The pipe contains the requested bandwidth and VLANs (through querying the attached junctions/fixtures). The URNs are pulled from the URN repository.

   :param topo: - The topology to be pruned.
   :param pipe: - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
   :param bwAvailMap: - A map of available "Ingress" and 'Egress" bandwidth for each URN.
   :param rsvVlanList: - A list of Reserved VLAN tags to be considered when pruning (along with VLANs in the Repo)
   :return: The topology with ineligible edges removed.

pruneWithPipeAZ
^^^^^^^^^^^^^^^

.. java:method:: public Topology pruneWithPipeAZ(Topology topo, RequestedVlanPipeE pipe, List<UrnE> urns, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList)
   :outertype: PruningService

   Prune the topology based on A->Z bandwidth using a logical pipe. The pipe contains the requested bandwidth and VLANs (through querying the attached junctions/fixtures). A list of URNs is passed into match devices/interfaces to topology elements.

   :param topo: - The topology to be pruned.
   :param pipe: - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
   :param urns: - The URNs that will be used to match available resources with elements of the topology.
   :return: The topology with ineligible edges removed.

pruneWithPipeZA
^^^^^^^^^^^^^^^

.. java:method:: public Topology pruneWithPipeZA(Topology topo, RequestedVlanPipeE pipe, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList)
   :outertype: PruningService

   Prune the topology based on Z->A bandwidth using a logical pipe. The pipe contains the requested bandwidth and VLANs (through querying the attached junctions/fixtures). The URNs are pulled from the URN repository.

   :param topo: - The topology to be pruned.
   :param pipe: - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
   :param bwAvailMap: - A map of available "Ingress" and 'Egress" bandwidth for each URN.
   :param rsvVlanList: - A list of Reserved VLAN tags to be considered when pruning (along with VLANs in the Repo)
   :return: The topology with ineligible edges removed.

pruneWithPipeZA
^^^^^^^^^^^^^^^

.. java:method:: public Topology pruneWithPipeZA(Topology topo, RequestedVlanPipeE pipe, List<UrnE> urns, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList)
   :outertype: PruningService

   Prune the topology based on Z->A bandwidth using a logical pipe. The pipe contains the requested bandwidth and VLANs (through querying the attached junctions/fixtures). A list of URNs is passed into match devices/interfaces to topology elements.

   :param topo: - The topology to be pruned.
   :param pipe: - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
   :param urns: - The URNs that will be used to match available resources with elements of the topology.
   :return: The topology with ineligible edges removed.

