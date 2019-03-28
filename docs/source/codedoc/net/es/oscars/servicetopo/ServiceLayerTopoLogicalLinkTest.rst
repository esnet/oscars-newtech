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

.. java:import:: net.es.oscars.pce PruningService

.. java:import:: net.es.oscars.pce.helpers TopologyBuilder

.. java:import:: net.es.oscars.topo.ent IntRangeE

.. java:import:: net.es.oscars.topo.ent ReservableBandwidthE

.. java:import:: net.es.oscars.topo.ent ReservableVlanE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.junit Test

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.time Instant

.. java:import:: java.time.temporal ChronoUnit

.. java:import:: java.util.stream Collectors

ServiceLayerTopoLogicalLinkTest
===============================

.. java:package:: net.es.oscars.servicetopo
   :noindex:

.. java:type:: @Slf4j @Transactional public class ServiceLayerTopoLogicalLinkTest extends AbstractCoreTest

   Created by jeremy on 6/15/16. Primarily tests correctness of Logical Link construction and end-point assignment in service-layer topology

Methods
-------
verifyLogicalLinksAllMPLS
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyLogicalLinksAllMPLS()
   :outertype: ServiceLayerTopoLogicalLinkTest

verifyLogicalLinksAsymmetric
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyLogicalLinksAsymmetric()
   :outertype: ServiceLayerTopoLogicalLinkTest

verifyLogicalLinksDisjointMpls
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyLogicalLinksDisjointMpls()
   :outertype: ServiceLayerTopoLogicalLinkTest

verifyLogicalLinksDstMPLS
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyLogicalLinksDstMPLS()
   :outertype: ServiceLayerTopoLogicalLinkTest

verifyLogicalLinksEthPortsOnRouters
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyLogicalLinksEthPortsOnRouters()
   :outertype: ServiceLayerTopoLogicalLinkTest

verifyLogicalLinksLinear
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyLogicalLinksLinear()
   :outertype: ServiceLayerTopoLogicalLinkTest

verifyLogicalLinksLongerPath
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyLogicalLinksLongerPath()
   :outertype: ServiceLayerTopoLogicalLinkTest

verifyLogicalLinksMultipath
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyLogicalLinksMultipath()
   :outertype: ServiceLayerTopoLogicalLinkTest

verifyLogicalLinksSrcMPLS
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void verifyLogicalLinksSrcMPLS()
   :outertype: ServiceLayerTopoLogicalLinkTest

