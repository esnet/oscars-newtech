.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars AbstractCoreTest

.. java:import:: net.es.oscars.dto.spec PalindromicType

.. java:import:: net.es.oscars.dto.spec SurvivabilityType

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.helpers RequestedEntityBuilder

.. java:import:: net.es.oscars.resv.dao ReservedBandwidthRepository

.. java:import:: net.es.oscars.pce.helpers MultipointTopologyBuilder

.. java:import:: org.junit Test

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.time Instant

.. java:import:: java.time.temporal ChronoUnit

TopPceTestMultipoint
====================

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Transactional public class TopPceTestMultipoint extends AbstractCoreTest

   Created by jeremy on 6/30/16. Tests End-to-End correctness of the PCE modules

Methods
-------
multipointPceTest1
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multipointPceTest1()
   :outertype: TopPceTestMultipoint

multipointPceTest2
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multipointPceTest2()
   :outertype: TopPceTestMultipoint

multipointPceTest3
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multipointPceTest3()
   :outertype: TopPceTestMultipoint

multipointPceTest4
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multipointPceTest4()
   :outertype: TopPceTestMultipoint

multipointPceTest5
^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multipointPceTest5()
   :outertype: TopPceTestMultipoint

multipointPceTestComplexNonPalindrome
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multipointPceTestComplexNonPalindrome()
   :outertype: TopPceTestMultipoint

multipointPceTestComplexPalindrome
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void multipointPceTestComplexPalindrome()
   :outertype: TopPceTestMultipoint

