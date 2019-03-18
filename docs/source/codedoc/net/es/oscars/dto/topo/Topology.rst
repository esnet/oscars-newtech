.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Builder

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: java.util HashSet

.. java:import:: java.util Optional

.. java:import:: java.util Set

Topology
========

.. java:package:: net.es.oscars.dto.topo
   :noindex:

.. java:type:: @Data @Builder @AllArgsConstructor @NoArgsConstructor public class Topology

Fields
------
layer
^^^^^

.. java:field:: public Layer layer
   :outertype: Topology

Methods
-------
getVertexByUrn
^^^^^^^^^^^^^^

.. java:method:: public Optional<TopoVertex> getVertexByUrn(String urn)
   :outertype: Topology

