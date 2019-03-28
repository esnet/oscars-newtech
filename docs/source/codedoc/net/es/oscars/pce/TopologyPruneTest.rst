.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars AbstractCoreTest

.. java:import:: net.es.oscars CoreUnitTestConfiguration

.. java:import:: net.es.oscars.pce.helpers TopologyBuilder

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.junit Test

.. java:import:: org.junit.runner RunWith

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.boot.test.context SpringBootTest

.. java:import:: org.springframework.test.context.junit4 SpringJUnit4ClassRunner

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.util.stream Collectors

TopologyPruneTest
=================

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Transactional public class TopologyPruneTest extends AbstractCoreTest

   Created by jeremy on 6/30/16. Tests correctness of how the NonPalindromicalPCE removes nodes/ports/links from different topology layers returned by TopoService and prior to passing them into ServiceLayerTopology.

Fields
------
ethTopo
^^^^^^^

.. java:field::  Topology ethTopo
   :outertype: TopologyPruneTest

intTopo
^^^^^^^

.. java:field::  Topology intTopo
   :outertype: TopologyPruneTest

mplsTopo
^^^^^^^^

.. java:field::  Topology mplsTopo
   :outertype: TopologyPruneTest

Methods
-------
NonPalTopoLayering1
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering1()
   :outertype: TopologyPruneTest

NonPalTopoLayering10
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering10()
   :outertype: TopologyPruneTest

NonPalTopoLayering11
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering11()
   :outertype: TopologyPruneTest

NonPalTopoLayering12
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering12()
   :outertype: TopologyPruneTest

NonPalTopoLayering2
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering2()
   :outertype: TopologyPruneTest

NonPalTopoLayering3
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering3()
   :outertype: TopologyPruneTest

NonPalTopoLayering4
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering4()
   :outertype: TopologyPruneTest

NonPalTopoLayering5
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering5()
   :outertype: TopologyPruneTest

NonPalTopoLayering6
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering6()
   :outertype: TopologyPruneTest

NonPalTopoLayering7
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering7()
   :outertype: TopologyPruneTest

NonPalTopoLayering8
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering8()
   :outertype: TopologyPruneTest

NonPalTopoLayering9
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void NonPalTopoLayering9()
   :outertype: TopologyPruneTest

