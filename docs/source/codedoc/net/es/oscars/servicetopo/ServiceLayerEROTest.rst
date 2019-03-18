.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars AbstractCoreTest

.. java:import:: net.es.oscars.dto.pss EthFixtureType

.. java:import:: net.es.oscars.dto.pss EthJunctionType

.. java:import:: net.es.oscars.dto.pss EthPipeType

.. java:import:: net.es.oscars.dto.spec PalindromicType

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums PortLayer

.. java:import:: net.es.oscars.dto.topo.enums UrnType

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.pce BandwidthService

.. java:import:: net.es.oscars.pce DijkstraPCE

.. java:import:: net.es.oscars.pce PruningService

.. java:import:: net.es.oscars.topo.ent IntRangeE

.. java:import:: net.es.oscars.topo.ent ReservableBandwidthE

.. java:import:: net.es.oscars.topo.ent ReservableVlanE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: org.junit Test

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.time Instant

.. java:import:: java.time.temporal ChronoUnit

.. java:import:: java.util.stream Collectors

ServiceLayerEROTest
===================

.. java:package:: net.es.oscars.servicetopo
   :noindex:

.. java:type:: @Slf4j @Transactional public class ServiceLayerEROTest extends AbstractCoreTest

   Created by jeremy on 6/24/16. Primarily tests correctness of service-layer topology logical edge construction, initialization, and population during MPLS-layer routing

Methods
-------
verifyCorrectEROsNoLogicalLinks
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyCorrectEROsNoLogicalLinks()
   :outertype: ServiceLayerEROTest

verifyCorrectEROsNonLinearAssymetric
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyCorrectEROsNonLinearAssymetric()
   :outertype: ServiceLayerEROTest

verifyCorrectEROsSimpleLogicalLinks
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyCorrectEROsSimpleLogicalLinks()
   :outertype: ServiceLayerEROTest

verifyCorrectEROsWithVirtualNodes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyCorrectEROsWithVirtualNodes()
   :outertype: ServiceLayerEROTest

verifyNoVirtualSrcDest
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyNoVirtualSrcDest()
   :outertype: ServiceLayerEROTest

verifyVirtualDestOnly
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyVirtualDestOnly()
   :outertype: ServiceLayerEROTest

verifyVirtualSrcDest
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyVirtualSrcDest()
   :outertype: ServiceLayerEROTest

verifyVirtualSrcOnly
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyVirtualSrcOnly()
   :outertype: ServiceLayerEROTest

