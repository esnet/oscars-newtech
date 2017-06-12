.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.dto.topo.enums PortLayer

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util.stream Collectors

BellmanFordPCE
==============

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Component public class BellmanFordPCE

Methods
-------
allShortestPaths
^^^^^^^^^^^^^^^^

.. java:method:: public Map<TopoVertex, List<TopoEdge>> allShortestPaths(Topology topo, TopoVertex source)
   :outertype: BellmanFordPCE

shortestPath
^^^^^^^^^^^^

.. java:method:: public List<TopoEdge> shortestPath(Topology topo, TopoVertex source, TopoVertex dest)
   :outertype: BellmanFordPCE

