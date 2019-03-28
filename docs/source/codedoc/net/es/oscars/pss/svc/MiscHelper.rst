.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.pss.params MplsHop

.. java:import:: net.es.oscars.dto.pss.params MplsPath

.. java:import:: net.es.oscars.dto.resv ResourceType

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.pss.dao UrnAddressRepository

.. java:import:: net.es.oscars.pss.ent UrnAddressE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util Set

.. java:import:: java.util.stream Collectors

MiscHelper
==========

.. java:package:: net.es.oscars.pss.svc
   :noindex:

.. java:type:: @Component @Slf4j public class MiscHelper

Methods
-------
junctionSdpId
^^^^^^^^^^^^^

.. java:method:: public Optional<Integer> junctionSdpId(ReservedVlanJunctionE rvj)
   :outertype: MiscHelper

junctionVcId
^^^^^^^^^^^^

.. java:method:: public Optional<Integer> junctionVcId(ReservedVlanJunctionE rvj)
   :outertype: MiscHelper

mplsPathBuilder
^^^^^^^^^^^^^^^

.. java:method:: public MplsPath mplsPathBuilder(ConnectionE conn, List<String> ero) throws PSSException
   :outertype: MiscHelper

vlanString
^^^^^^^^^^

.. java:method:: public String vlanString(ReservedVlanFixtureE f)
   :outertype: MiscHelper

