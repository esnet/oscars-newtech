.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars AbstractCoreTest

.. java:import:: net.es.oscars.dto.spec PalindromicType

.. java:import:: net.es.oscars.dto.spec SurvivabilityType

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums UrnType

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.helpers RequestedEntityBuilder

.. java:import:: net.es.oscars.pce.helpers AsymmTopologyBuilder

.. java:import:: net.es.oscars.pce.helpers TopologyBuilder

.. java:import:: net.es.oscars.topo.dao UrnAdjcyRepository

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: org.junit Test

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.time Instant

.. java:import:: java.time.temporal ChronoUnit

TopPceTestNonPalindromic
========================

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Transactional public class TopPceTestNonPalindromic extends AbstractCoreTest

   Created by jeremy on 7/8/16. Tests End-to-End correctness of the Non-Palindromical PCE modules

Methods
-------
asymmPceTest10
^^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTest10()
   :outertype: TopPceTestNonPalindromic

asymmPceTest11
^^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTest11()
   :outertype: TopPceTestNonPalindromic

asymmPceTest12
^^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTest12()
   :outertype: TopPceTestNonPalindromic

asymmPceTest2
^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTest2()
   :outertype: TopPceTestNonPalindromic

asymmPceTest3
^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTest3()
   :outertype: TopPceTestNonPalindromic

asymmPceTest4
^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTest4()
   :outertype: TopPceTestNonPalindromic

asymmPceTest5
^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTest5()
   :outertype: TopPceTestNonPalindromic

asymmPceTest7
^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTest7()
   :outertype: TopPceTestNonPalindromic

asymmPceTest8
^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTest8()
   :outertype: TopPceTestNonPalindromic

asymmPceTest9
^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTest9()
   :outertype: TopPceTestNonPalindromic

asymmPceTestNoFixtures
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void asymmPceTestNoFixtures()
   :outertype: TopPceTestNonPalindromic

basicPceTest10
^^^^^^^^^^^^^^

.. java:method:: @Test public void basicPceTest10()
   :outertype: TopPceTestNonPalindromic

basicPceTest11
^^^^^^^^^^^^^^

.. java:method:: @Test public void basicPceTest11()
   :outertype: TopPceTestNonPalindromic

basicPceTest12
^^^^^^^^^^^^^^

.. java:method:: @Test public void basicPceTest12()
   :outertype: TopPceTestNonPalindromic

basicPceTest2
^^^^^^^^^^^^^

.. java:method:: @Test public void basicPceTest2()
   :outertype: TopPceTestNonPalindromic

basicPceTest3
^^^^^^^^^^^^^

.. java:method:: @Test public void basicPceTest3()
   :outertype: TopPceTestNonPalindromic

basicPceTest4
^^^^^^^^^^^^^

.. java:method:: @Test public void basicPceTest4()
   :outertype: TopPceTestNonPalindromic

basicPceTest5
^^^^^^^^^^^^^

.. java:method:: @Test public void basicPceTest5()
   :outertype: TopPceTestNonPalindromic

basicPceTest7
^^^^^^^^^^^^^

.. java:method:: @Test public void basicPceTest7()
   :outertype: TopPceTestNonPalindromic

basicPceTest8
^^^^^^^^^^^^^

.. java:method:: @Test public void basicPceTest8()
   :outertype: TopPceTestNonPalindromic

basicPceTest9
^^^^^^^^^^^^^

.. java:method:: @Test public void basicPceTest9()
   :outertype: TopPceTestNonPalindromic

multiFixtureTest
^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multiFixtureTest()
   :outertype: TopPceTestNonPalindromic

multiMplsPipeTestNonPal
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multiMplsPipeTestNonPal()
   :outertype: TopPceTestNonPalindromic

multiMplsPipeTestPal
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multiMplsPipeTestPal()
   :outertype: TopPceTestNonPalindromic

multiMplsPipeTestPalHighBW
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multiMplsPipeTestPalHighBW()
   :outertype: TopPceTestNonPalindromic

nonPalHighLinkCostTest10
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalHighLinkCostTest10()
   :outertype: TopPceTestNonPalindromic

nonPalHighLinkCostTest11
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalHighLinkCostTest11()
   :outertype: TopPceTestNonPalindromic

nonPalPceAsymmTest1
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceAsymmTest1()
   :outertype: TopPceTestNonPalindromic

nonPalPceAsymmTest2
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceAsymmTest2()
   :outertype: TopPceTestNonPalindromic

nonPalPceAsymmTest3
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceAsymmTest3()
   :outertype: TopPceTestNonPalindromic

nonPalPceAsymmTest4
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceAsymmTest4()
   :outertype: TopPceTestNonPalindromic

nonPalPceAsymmTest5
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceAsymmTest5()
   :outertype: TopPceTestNonPalindromic

nonPalPceAsymmTest6
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceAsymmTest6()
   :outertype: TopPceTestNonPalindromic

nonPalPceHighLinkCostTest2
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceHighLinkCostTest2()
   :outertype: TopPceTestNonPalindromic

nonPalPceHighLinkCostTest3
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceHighLinkCostTest3()
   :outertype: TopPceTestNonPalindromic

nonPalPceHighLinkCostTest5
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceHighLinkCostTest5()
   :outertype: TopPceTestNonPalindromic

nonPalPceSymmTest1
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceSymmTest1()
   :outertype: TopPceTestNonPalindromic

nonPalPceSymmTest2
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceSymmTest2()
   :outertype: TopPceTestNonPalindromic

nonPalPceSymmTest3
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceSymmTest3()
   :outertype: TopPceTestNonPalindromic

nonPalPceSymmTest4
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceSymmTest4()
   :outertype: TopPceTestNonPalindromic

nonPalPceSymmTest5
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceSymmTest5()
   :outertype: TopPceTestNonPalindromic

nonPalPceSymmTest6
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalPceSymmTest6()
   :outertype: TopPceTestNonPalindromic

nonPalWithEthPortsOnRoutersTest
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void nonPalWithEthPortsOnRoutersTest()
   :outertype: TopPceTestNonPalindromic

sharedLinkPceTest1
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void sharedLinkPceTest1()
   :outertype: TopPceTestNonPalindromic

sharedLinkPceTest2
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void sharedLinkPceTest2()
   :outertype: TopPceTestNonPalindromic

sharedLinkPceTest3
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void sharedLinkPceTest3()
   :outertype: TopPceTestNonPalindromic

sharedLinkPceTest4
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void sharedLinkPceTest4()
   :outertype: TopPceTestNonPalindromic

