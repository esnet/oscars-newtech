.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars AbstractCoreTest

.. java:import:: net.es.oscars.dto.spec PalindromicType

.. java:import:: net.es.oscars.dto.spec SurvivabilityType

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums PortLayer

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.helpers RequestedEntityBuilder

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.pce.helpers AsymmTopologyBuilder

.. java:import:: net.es.oscars.pce.helpers TopologyBuilder

.. java:import:: net.es.oscars.topo.ent BidirectionalPathE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.junit Test

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.time Instant

.. java:import:: java.time.temporal ChronoUnit

TopPceTestSurvivablePartial
===========================

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Transactional public class TopPceTestSurvivablePartial extends AbstractCoreTest

Methods
-------
survPartialThreeDisjointAllMPLSK2Fail
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointAllMPLSK2Fail()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointAllMPLSK2Pass
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointAllMPLSK2Pass()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointAllMPLSK3Fail
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointAllMPLSK3Fail()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointAllMPLSK3Pass
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointAllMPLSK3Pass()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointEdgeAsymmK2Pass
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointEdgeAsymmK2Pass()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointEdgeAsymmK3Fail
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointEdgeAsymmK3Fail()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointEdgeEthInternalEthK2Pass
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointEdgeEthInternalEthK2Pass()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointEdgeEthInternalEthK3Fail
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointEdgeEthInternalEthK3Fail()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointEdgeEthK2Pass
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointEdgeEthK2Pass()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointEdgeEthK3Pass
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointEdgeEthK3Pass()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointEdgeEthK4Fail
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointEdgeEthK4Fail()
   :outertype: TopPceTestSurvivablePartial

survPartialThreeDisjointEdgeEthPortOnRoutersK3Pass
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialThreeDisjointEdgeEthPortOnRoutersK3Pass()
   :outertype: TopPceTestSurvivablePartial

survPartialWithEthPortsOnRoutersTest1
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialWithEthPortsOnRoutersTest1()
   :outertype: TopPceTestSurvivablePartial

survPartialWithEthPortsOnRoutersTest2
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialWithEthPortsOnRoutersTest2()
   :outertype: TopPceTestSurvivablePartial

survPartialWithEthPortsOnRoutersTest3
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survPartialWithEthPortsOnRoutersTest3()
   :outertype: TopPceTestSurvivablePartial

survivablePartialPceTestESnet
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePartialPceTestESnet()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest1
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest1()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest10
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest10()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest11
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest11()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest12
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest12()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest13
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest13()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest14
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest14()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest15
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest15()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest2
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest2()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest3
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest3()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest4
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest4()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest4_2
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest4_2()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest5
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest5()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest6
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest6()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest7
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest7()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest8
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest8()
   :outertype: TopPceTestSurvivablePartial

survivablePceTest9
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void survivablePceTest9()
   :outertype: TopPceTestSurvivablePartial

