.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.pss EthFixtureType

.. java:import:: net.es.oscars.dto.pss EthJunctionType

.. java:import:: net.es.oscars.dto.pss EthPipeType

.. java:import:: net.es.oscars.dto.spec PalindromicType

.. java:import:: net.es.oscars.dto.spec SurvivabilityType

.. java:import:: net.es.oscars.dto.topo.enums UrnType

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.time.temporal ChronoUnit

RequestedEntityBuilder
======================

.. java:package:: net.es.oscars.helpers
   :noindex:

.. java:type:: @Slf4j @Component public class RequestedEntityBuilder

Fields
------
urnRepo
^^^^^^^

.. java:field:: @Autowired  UrnRepository urnRepo
   :outertype: RequestedEntityBuilder

Methods
-------
buildConnection
^^^^^^^^^^^^^^^

.. java:method:: public ConnectionE buildConnection(RequestedBlueprintE blueprint, ScheduleSpecificationE schedule, String connectionID, String description)
   :outertype: RequestedEntityBuilder

buildRequest
^^^^^^^^^^^^

.. java:method:: public RequestedBlueprintE buildRequest(String deviceName, List<String> fixtureNames, Integer azMbps, Integer zaMbps, String vlanExp, String connectionId)
   :outertype: RequestedEntityBuilder

buildRequest
^^^^^^^^^^^^

.. java:method:: public RequestedBlueprintE buildRequest(String aPort, String aDevice, String zPort, String zDevice, Integer azMbps, Integer zaMbps, PalindromicType palindromic, SurvivabilityType survivable, String vlanExp, Integer numDisjoint, Integer minPipes, Integer maxPipes, String connectionId)
   :outertype: RequestedEntityBuilder

buildRequest
^^^^^^^^^^^^

.. java:method:: public RequestedBlueprintE buildRequest(String aPort, String aDevice, String zPort, String zDevice, Integer azMbps, Integer zaMbps, PalindromicType palindromic, SurvivabilityType survivable, String vlanExp, Set<String> blacklist, Integer numDisjoint, Integer minPipes, Integer maxPipes, String connectionId)
   :outertype: RequestedEntityBuilder

buildRequest
^^^^^^^^^^^^

.. java:method:: public RequestedBlueprintE buildRequest(String aPort, String aDevice, String zPort, String zDevice, Integer azMbps, Integer zaMbps, PalindromicType palindromic, SurvivabilityType survivable, String aVlanExp, String zVlanExp, Integer numDisjoint, Integer minPipes, Integer maxPipes, String connectionId)
   :outertype: RequestedEntityBuilder

buildRequest
^^^^^^^^^^^^

.. java:method:: public RequestedBlueprintE buildRequest(List<String> aPorts, String aDevice, List<String> zPorts, String zDevice, Integer azMbps, Integer zaMbps, PalindromicType palindromic, SurvivabilityType survivable, String vlanExp, Integer numDisjoint, Integer minPipes, Integer maxPipes, String connectionId)
   :outertype: RequestedEntityBuilder

buildRequest
^^^^^^^^^^^^

.. java:method:: public RequestedBlueprintE buildRequest(List<String> aPorts, List<String> aDevices, List<String> zPorts, List<String> zDevices, List<Integer> azMbpsList, List<Integer> zaMbpsList, List<PalindromicType> palindromicList, List<SurvivabilityType> survivableList, List<String> vlanExps, List<Integer> numDisjoints, List<Integer> priorities, Integer minPipes, Integer maxPipes, String connectionId)
   :outertype: RequestedEntityBuilder

buildRequest
^^^^^^^^^^^^

.. java:method:: public RequestedBlueprintE buildRequest(Set<RequestedVlanPipeE> requestedPipes, Integer minPipes, Integer maxPipes, String connectionId)
   :outertype: RequestedEntityBuilder

buildRequest
^^^^^^^^^^^^

.. java:method:: public RequestedBlueprintE buildRequest(List<String> deviceNames, List<List<String>> portNames, List<Integer> azMbpsList, List<Integer> zaMbpsList, List<String> vlanExps, String connectionId)
   :outertype: RequestedEntityBuilder

buildRequest
^^^^^^^^^^^^

.. java:method:: public RequestedBlueprintE buildRequest(List<String> azERO, List<String> zaERO, Integer azBandwidth, Integer zaBandwidth, String connectionId)
   :outertype: RequestedEntityBuilder

buildRequestedFixture
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public RequestedVlanFixtureE buildRequestedFixture(String fixName, Integer azMbps, Integer zaMbps, String vlanExp)
   :outertype: RequestedEntityBuilder

buildRequestedJunction
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public RequestedVlanJunctionE buildRequestedJunction(String deviceName, List<String> fixtureNames, Integer azMbps, Integer zaMbps, String vlanExp, boolean startJunc)
   :outertype: RequestedEntityBuilder

buildRequestedPipe
^^^^^^^^^^^^^^^^^^

.. java:method:: public RequestedVlanPipeE buildRequestedPipe(String aPort, String aDevice, String zPort, String zDevice, Integer azMbps, Integer zaMbps, PalindromicType palindromic, SurvivabilityType survivable, String aVlanExp, String zVlanExp, Integer numPaths, Integer priority)
   :outertype: RequestedEntityBuilder

buildRequestedPipe
^^^^^^^^^^^^^^^^^^

.. java:method:: public RequestedVlanPipeE buildRequestedPipe(String aPort, String aDevice, String zPort, String zDevice, Integer azMbps, Integer zaMbps, PalindromicType palindromic, SurvivabilityType survivable, String vlanExp, Set<String> blacklist, Integer numPaths, Integer priority)
   :outertype: RequestedEntityBuilder

buildRequestedPipe
^^^^^^^^^^^^^^^^^^

.. java:method:: public RequestedVlanPipeE buildRequestedPipe(List<String> aPorts, String aDevice, List<String> zPorts, String zDevice, Integer azMbps, Integer zaMbps, PalindromicType palindromic, SurvivabilityType survivable, String vlanExp, Integer numPaths, Integer priority)
   :outertype: RequestedEntityBuilder

buildSchedule
^^^^^^^^^^^^^

.. java:method:: public ScheduleSpecificationE buildSchedule(Date start, Date end)
   :outertype: RequestedEntityBuilder

