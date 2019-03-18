.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto IntRange

.. java:import:: net.es.oscars.dto.resv ResourceType

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.helpers IntRangeParsing

.. java:import:: net.es.oscars.pss PCEAssistant

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: org.junit Test

.. java:import:: java.time Instant

ReserveTopoTest
===============

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j public class ReserveTopoTest

Methods
-------
buildAbcEro
^^^^^^^^^^^

.. java:method:: public List<TopoEdge> buildAbcEro()
   :outertype: ReserveTopoTest

buildDecomposablePath
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<TopoEdge> buildDecomposablePath()
   :outertype: ReserveTopoTest

decideVCIDTest
^^^^^^^^^^^^^^

.. java:method:: @Test public void decideVCIDTest() throws PCEException
   :outertype: ReserveTopoTest

testDecompose
^^^^^^^^^^^^^

.. java:method:: @Test public void testDecompose() throws PSSException
   :outertype: ReserveTopoTest

testEroFromTopoEdge
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void testEroFromTopoEdge()
   :outertype: ReserveTopoTest

testIntRangeMerging
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void testIntRangeMerging()
   :outertype: ReserveTopoTest

testIntRangeParsing
^^^^^^^^^^^^^^^^^^^

.. java:method:: @Test public void testIntRangeParsing()
   :outertype: ReserveTopoTest

