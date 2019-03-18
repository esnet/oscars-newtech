.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok NoArgsConstructor

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: java.util List

LogicalEdge
===========

.. java:package:: net.es.oscars.servicetopo
   :noindex:

.. java:type:: @Data @EqualsAndHashCode @AllArgsConstructor @NoArgsConstructor public class LogicalEdge extends TopoEdge

   Created by jeremy on 6/15/16. Class to represent a non-physical edge on the service-layer topology. Identical to TopoEdge, however it also includes a list of physical TopoEdges that comprise this LogicalEdge.

