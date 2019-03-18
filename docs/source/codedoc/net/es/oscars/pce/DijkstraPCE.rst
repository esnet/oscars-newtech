.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: edu.uci.ics.jung.algorithms.shortestpath DijkstraShortestPath

.. java:import:: edu.uci.ics.jung.graph DirectedSparseMultigraph

.. java:import:: edu.uci.ics.jung.graph Graph

.. java:import:: edu.uci.ics.jung.graph.util EdgeType

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.apache.commons.collections15 Transformer

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util ArrayList

.. java:import:: java.util List

DijkstraPCE
===========

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Component public class DijkstraPCE

   Created by jeremy on 6/15/16.

Methods
-------
computeShortestPathEdges
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<TopoEdge> computeShortestPathEdges(Topology topology, TopoVertex srcVertex, TopoVertex dstVertex)
   :outertype: DijkstraPCE

   Computes Dijkstra's shortest path directed from srcVertex to dstVertex. Input topology is assumed to be pre-pruned based on bandwidth and vlan availability.

   :param topology: - pruned topology
   :param srcVertex: - source URN
   :param dstVertex: - destination URN
   :return: path as List of TopoEdge objects

translatePathEdgesToVertices
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<TopoVertex> translatePathEdgesToVertices(List<TopoEdge> pathEdges)
   :outertype: DijkstraPCE

   Translates a List of TopoEdges representing a path into its corresponding TopoVertices

   :param pathEdges: - List of TopoEdges representing path
   :return: path as list of TopoVertex objects

translatePathVerticesToStrings
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<String> translatePathVerticesToStrings(List<TopoVertex> pathVertices)
   :outertype: DijkstraPCE

   Translates a List of TopoVertices representing a path into its corresponding String URNs

   :param pathVertices: - List of TopoVertices representing path
   :return: path as list of URN Strings

