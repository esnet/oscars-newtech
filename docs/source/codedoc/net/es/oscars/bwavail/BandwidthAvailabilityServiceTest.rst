.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars AbstractCoreTest

.. java:import:: net.es.oscars.bwavail.svc BandwidthAvailabilityService

.. java:import:: net.es.oscars.dto.bwavail BandwidthAvailabilityRequest

.. java:import:: net.es.oscars.dto.bwavail BandwidthAvailabilityResponse

.. java:import:: net.es.oscars.pce.helpers RepoEntityBuilder

.. java:import:: net.es.oscars.pce.helpers TopologyBuilder

.. java:import:: net.es.oscars.resv.dao ReservedBandwidthRepository

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.junit Test

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.time Instant

.. java:import:: java.time.temporal ChronoUnit

.. java:import:: java.util.stream Collectors

BandwidthAvailabilityServiceTest
================================

.. java:package:: net.es.oscars.bwavail
   :noindex:

.. java:type:: @Slf4j @Transactional public class BandwidthAvailabilityServiceTest extends AbstractCoreTest

Methods
-------
noPathIntermediateNodesFullyReserved
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void noPathIntermediateNodesFullyReserved()
   :outertype: BandwidthAvailabilityServiceTest

noPathStartNodeFullyReserved
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void noPathStartNodeFullyReserved()
   :outertype: BandwidthAvailabilityServiceTest

noReservationsTest
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void noReservationsTest()
   :outertype: BandwidthAvailabilityServiceTest

noReservationsTestJunction
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void noReservationsTestJunction()
   :outertype: BandwidthAvailabilityServiceTest

noReservationsTestJunctionPath
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void noReservationsTestJunctionPath()
   :outertype: BandwidthAvailabilityServiceTest

onePathAvailable
^^^^^^^^^^^^^^^^

.. java:method:: @Test public void onePathAvailable()
   :outertype: BandwidthAvailabilityServiceTest

reservationsDifferentBandwidthValuesDifferentNodes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsDifferentBandwidthValuesDifferentNodes()
   :outertype: BandwidthAvailabilityServiceTest

reservationsDifferentBandwidthValuesSameNode
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsDifferentBandwidthValuesSameNode()
   :outertype: BandwidthAvailabilityServiceTest

reservationsDifferentBandwidthValuesSameNodeDifferentTimes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsDifferentBandwidthValuesSameNodeDifferentTimes()
   :outertype: BandwidthAvailabilityServiceTest

reservationsDifferentInEgBandwidthValuesDifferentNodesDifferentTimes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsDifferentInEgBandwidthValuesDifferentNodesDifferentTimes()
   :outertype: BandwidthAvailabilityServiceTest

reservationsDifferentInEgBandwidthValuesDifferentNodesSameTime
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsDifferentInEgBandwidthValuesDifferentNodesSameTime()
   :outertype: BandwidthAvailabilityServiceTest

reservationsDifferentInEgBandwidthValuesSameNodeDifferentTimes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsDifferentInEgBandwidthValuesSameNodeDifferentTimes()
   :outertype: BandwidthAvailabilityServiceTest

reservationsDifferentInEgBandwidthValuesSameNodeSameTime
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsDifferentInEgBandwidthValuesSameNodeSameTime()
   :outertype: BandwidthAvailabilityServiceTest

reservationsMix
^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsMix()
   :outertype: BandwidthAvailabilityServiceTest

reservationsOffPathTest
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsOffPathTest()
   :outertype: BandwidthAvailabilityServiceTest

reservationsStartAfterEndAfter
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsStartAfterEndAfter()
   :outertype: BandwidthAvailabilityServiceTest

reservationsStartBeforeEndAfterTest
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsStartBeforeEndAfterTest()
   :outertype: BandwidthAvailabilityServiceTest

reservationsStartBeforeEndAtStart
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsStartBeforeEndAtStart()
   :outertype: BandwidthAvailabilityServiceTest

reservationsStartBeforeEndBefore
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsStartBeforeEndBefore()
   :outertype: BandwidthAvailabilityServiceTest

reservationsStartBeforeEndDuring
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsStartBeforeEndDuring()
   :outertype: BandwidthAvailabilityServiceTest

reservationsStartDuringEndAfter
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsStartDuringEndAfter()
   :outertype: BandwidthAvailabilityServiceTest

reservationsStartDuringEndDuring
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsStartDuringEndDuring()
   :outertype: BandwidthAvailabilityServiceTest

reservationsStartDuringEndDuringDifferentPipeAndJunctionBw
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsStartDuringEndDuringDifferentPipeAndJunctionBw()
   :outertype: BandwidthAvailabilityServiceTest

reservationsThreeWayOverlapDifferentNodes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsThreeWayOverlapDifferentNodes()
   :outertype: BandwidthAvailabilityServiceTest

reservationsThreeWayOverlapSameNode
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void reservationsThreeWayOverlapSameNode()
   :outertype: BandwidthAvailabilityServiceTest

specifiedEro
^^^^^^^^^^^^

.. java:method:: @Test public void specifiedEro()
   :outertype: BandwidthAvailabilityServiceTest

twoDisjointPaths
^^^^^^^^^^^^^^^^

.. java:method:: public void twoDisjointPaths()
   :outertype: BandwidthAvailabilityServiceTest

twoSpecifiedEros
^^^^^^^^^^^^^^^^

.. java:method:: @Test public void twoSpecifiedEros()
   :outertype: BandwidthAvailabilityServiceTest

