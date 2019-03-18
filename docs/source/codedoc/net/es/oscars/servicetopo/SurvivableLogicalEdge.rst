.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Data

.. java:import:: lombok EqualsAndHashCode

.. java:import:: lombok NoArgsConstructor

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: java.util List

SurvivableLogicalEdge
=====================

.. java:package:: net.es.oscars.servicetopo
   :noindex:

.. java:type:: @Data @EqualsAndHashCode @AllArgsConstructor @NoArgsConstructor public class SurvivableLogicalEdge extends TopoEdge

   Created by jeremy on 7/28/16. Class to represent a non-physical edge on the service-layer topology. Identical to LogicalEdge, however it also includes both a primary and backup list of physical TopoEdges that comprise this SurvivableLogicalEdge.

