.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars AbstractCoreTest

.. java:import:: net.es.oscars.dto.spec PalindromicType

.. java:import:: net.es.oscars.dto.spec SurvivabilityType

.. java:import:: net.es.oscars.helpers RequestedEntityBuilder

.. java:import:: net.es.oscars.pce.helpers TopologyBuilder

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.junit Test

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.time Instant

.. java:import:: java.time.temporal ChronoUnit

.. java:import:: java.util.stream Collectors

BlacklistPruningTest
====================

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Transactional public class BlacklistPruningTest extends AbstractCoreTest

Methods
-------
blacklistAllTest
^^^^^^^^^^^^^^^^

.. java:method:: @Test public void blacklistAllTest()
   :outertype: BlacklistPruningTest

blacklistEthMplsTest
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void blacklistEthMplsTest()
   :outertype: BlacklistPruningTest

blacklistEthTest
^^^^^^^^^^^^^^^^

.. java:method:: @Test public void blacklistEthTest()
   :outertype: BlacklistPruningTest

blacklistMplsTest
^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void blacklistMplsTest()
   :outertype: BlacklistPruningTest

pceSubmitBlacklistAllTest
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void pceSubmitBlacklistAllTest()
   :outertype: BlacklistPruningTest

pceSubmitBlacklistIntermediateTest
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void pceSubmitBlacklistIntermediateTest()
   :outertype: BlacklistPruningTest

pruneTest
^^^^^^^^^

.. java:method:: public void pruneTest(RequestedBlueprintE requestedBlueprint, Topology topo, ScheduleSpecificationE requestedSched, Set<TopoEdge> origEdges, Set<TopoVertex> origVerts, Set<String> blacklist)
   :outertype: BlacklistPruningTest

setupTest
^^^^^^^^^

.. java:method:: public RequestedBlueprintE setupTest(String srcPort, String srcDevice, String dstPort, String dstDevice, Set<String> blacklist)
   :outertype: BlacklistPruningTest

