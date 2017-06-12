.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.resv Connection

.. java:import:: net.es.oscars.dto.rsrc ReservableBandwidth

.. java:import:: net.es.oscars.dto.spec ReservedEthPipe

.. java:import:: net.es.oscars.dto.spec ReservedMplsPipe

.. java:import:: net.es.oscars.dto.spec ReservedVlanFlow

.. java:import:: net.es.oscars.dto.spec ReservedVlanJunction

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.dto.viz Position

.. java:import:: net.es.oscars.dto.viz VizEdge

.. java:import:: net.es.oscars.dto.viz VizGraph

.. java:import:: net.es.oscars.dto.viz VizNode

.. java:import:: net.es.oscars.webui.cont TopologyProvider

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.math BigDecimal

.. java:import:: java.math MathContext

.. java:import:: java.util List

.. java:import:: java.util.stream Collectors

VizExporter
===========

.. java:package:: net.es.oscars.webui.viz
   :noindex:

.. java:type:: @Component @Slf4j public class VizExporter

Constructors
------------
VizExporter
^^^^^^^^^^^

.. java:constructor:: @Autowired public VizExporter(TopologyProvider topologyProvider)
   :outertype: VizExporter

Methods
-------
connection
^^^^^^^^^^

.. java:method:: public VizGraph connection(Connection c)
   :outertype: VizExporter

listTopologyPorts
^^^^^^^^^^^^^^^^^

.. java:method:: public List<String> listTopologyPorts()
   :outertype: VizExporter

multilayerGraph
^^^^^^^^^^^^^^^

.. java:method:: public VizGraph multilayerGraph()
   :outertype: VizExporter

multilayerGraphUnidirectional
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public VizGraph multilayerGraphUnidirectional()
   :outertype: VizExporter

