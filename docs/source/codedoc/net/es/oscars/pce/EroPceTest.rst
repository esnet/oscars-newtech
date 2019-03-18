.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars AbstractCoreTest

.. java:import:: net.es.oscars.dto.spec PalindromicType

.. java:import:: net.es.oscars.dto.spec SurvivabilityType

.. java:import:: net.es.oscars.helpers RequestedEntityBuilder

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.resv.dao ReservedBandwidthRepository

.. java:import:: net.es.oscars.resv.svc ResvService

.. java:import:: net.es.oscars.pce.helpers TopologyBuilder

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: org.junit Test

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.time Instant

.. java:import:: java.time.temporal ChronoUnit

.. java:import:: java.util.stream Collectors

.. java:import:: java.util.stream Stream

EroPceTest
==========

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Transactional public class EroPceTest extends AbstractCoreTest

   Created by jeremy on 7/22/16.

   Tests End-to-End correctness of the PCE modules with specified EROs

Methods
-------
eroPceTestBadNonPalindrome1
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroPceTestBadNonPalindrome1() throws PCEException
   :outertype: EroPceTest

eroPceTestBadNonPalindrome2
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroPceTestBadNonPalindrome2() throws PCEException
   :outertype: EroPceTest

eroPceTestDuplicateNode1
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroPceTestDuplicateNode1() throws PCEException
   :outertype: EroPceTest

eroPceTestDuplicateNode2
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroPceTestDuplicateNode2() throws PCEException
   :outertype: EroPceTest

eroPceTestNonPalindrome
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroPceTestNonPalindrome() throws PCEException
   :outertype: EroPceTest

eroPceTestNonPalindrome2
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroPceTestNonPalindrome2() throws PCEException
   :outertype: EroPceTest

eroPceTestPalindrome
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroPceTestPalindrome() throws PCEException
   :outertype: EroPceTest

eroPceTestSharedLink
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroPceTestSharedLink() throws PCEException
   :outertype: EroPceTest

eroSpecTestEmptyAZ
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroSpecTestEmptyAZ() throws PCEException, PSSException
   :outertype: EroPceTest

eroSpecTestEmptyZA
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroSpecTestEmptyZA() throws PCEException, PSSException
   :outertype: EroPceTest

eroSpecTestSharedLinkInsufficientBW
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroSpecTestSharedLinkInsufficientBW() throws PCEException, PSSException
   :outertype: EroPceTest

eroSpecTestSharedLinkSufficientBW
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void eroSpecTestSharedLinkSufficientBW() throws PCEException, PSSException
   :outertype: EroPceTest

multiMplsPipeTestNonPal
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multiMplsPipeTestNonPal() throws PCEException, PSSException
   :outertype: EroPceTest

partialEroMultiIntermediateTest
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void partialEroMultiIntermediateTest() throws PCEException
   :outertype: EroPceTest

partialEroOneIntermediateTest
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void partialEroOneIntermediateTest() throws PCEException
   :outertype: EroPceTest

partialEroTwoIntermediateTest
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void partialEroTwoIntermediateTest() throws PCEException
   :outertype: EroPceTest

pceSubmitPartialEroMultiIntermediateTest
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void pceSubmitPartialEroMultiIntermediateTest() throws PCEException, PSSException
   :outertype: EroPceTest

