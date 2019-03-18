.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: com.fasterxml.jackson.databind.util ISO8601DateFormat

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.bwavail.svc BandwidthAvailabilityService

.. java:import:: net.es.oscars.dto.bwavail PortBandwidthAvailabilityRequest

.. java:import:: net.es.oscars.dto.bwavail PortBandwidthAvailabilityResponse

.. java:import:: net.es.oscars.dto.pss EthPipeType

.. java:import:: net.es.oscars.dto.pss.cmd CommandType

.. java:import:: net.es.oscars.dto.pss.cmd GeneratedCommands

.. java:import:: net.es.oscars.dto.resv Connection

.. java:import:: net.es.oscars.dto.resv ConnectionFilter

.. java:import:: net.es.oscars.dto.resv.precheck PreCheckResponse

.. java:import:: net.es.oscars.dto.spec PalindromicType

.. java:import:: net.es.oscars.dto.spec RequestedVlanFlow

.. java:import:: net.es.oscars.dto.spec RequestedVlanPipe

.. java:import:: net.es.oscars.dto.spec SurvivabilityType

.. java:import:: net.es.oscars.dto.topo BidirectionalPath

.. java:import:: net.es.oscars.dto.topo Edge

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.pss.svc RouterCommandsService

.. java:import:: net.es.oscars.resv.ent ConnectionE

.. java:import:: net.es.oscars.resv.ent RequestedVlanFixtureE

.. java:import:: net.es.oscars.resv.ent RequestedVlanJunctionE

.. java:import:: net.es.oscars.resv.ent RequestedVlanPipeE

.. java:import:: net.es.oscars.resv.svc ResvService

.. java:import:: net.es.oscars.st.oper OperState

.. java:import:: net.es.oscars.st.prov ProvState

.. java:import:: net.es.oscars.st.resv ResvState

.. java:import:: net.es.oscars.webui.dto AdvancedRequest

.. java:import:: net.es.oscars.webui.dto ConnectionBuilder

.. java:import:: net.es.oscars.webui.dto Filter

.. java:import:: net.es.oscars.webui.dto MinimalRequest

.. java:import:: org.hashids Hashids

.. java:import:: org.modelmapper ModelMapper

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Controller

.. java:import:: org.springframework.ui Model

.. java:import:: java.text DateFormat

.. java:import:: java.text ParseException

.. java:import:: java.util.stream Collectors

ReservationController
=====================

.. java:package:: net.es.oscars.webui.cont
   :noindex:

.. java:type:: @Slf4j @Controller public class ReservationController

Constructors
------------
ReservationController
^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public ReservationController(ResvService resvService, BandwidthAvailabilityService bwAvailService, RouterCommandsService routerCommandsService)
   :outertype: ReservationController

Methods
-------
commands
^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Map<String, String> commands(String connectionId, String deviceUrn)
   :outertype: ReservationController

commitConnection
^^^^^^^^^^^^^^^^

.. java:method:: public String commitConnection(String connectionId)
   :outertype: ReservationController

connection_commit
^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String connection_commit(String connectionId, Model model)
   :outertype: ReservationController

connection_commit_react
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public String connection_commit_react(String connectionId)
   :outertype: ReservationController

convertConnToDto
^^^^^^^^^^^^^^^^

.. java:method:: public Connection convertConnToDto(ConnectionE connectionE)
   :outertype: ReservationController

filtered
^^^^^^^^

.. java:method:: public Set<Connection> filtered(ConnectionFilter filter)
   :outertype: ReservationController

holdAdvanced
^^^^^^^^^^^^

.. java:method:: public Connection holdAdvanced(AdvancedRequest advancedRequest) throws PCEException, PSSException
   :outertype: ReservationController

holdConnection
^^^^^^^^^^^^^^

.. java:method:: public Connection holdConnection(Connection connection) throws PCEException, PSSException
   :outertype: ReservationController

holdMinimal
^^^^^^^^^^^

.. java:method:: public Connection holdMinimal(MinimalRequest minimalRequest) throws PCEException, PSSException
   :outertype: ReservationController

new_connection_id
^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Map<String, String> new_connection_id()
   :outertype: ReservationController

preCheck
^^^^^^^^

.. java:method:: public Connection preCheck(Connection connection) throws PCEException, PSSException
   :outertype: ReservationController

preCheckAdvanced
^^^^^^^^^^^^^^^^

.. java:method:: public Connection preCheckAdvanced(AdvancedRequest advancedRequest) throws PCEException, PSSException
   :outertype: ReservationController

preCheckMinimal
^^^^^^^^^^^^^^^

.. java:method:: public Connection preCheckMinimal(MinimalRequest minimalRequest) throws PCEException, PSSException
   :outertype: ReservationController

queryPortBwAvailability
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public PortBandwidthAvailabilityResponse queryPortBwAvailability(MinimalRequest request)
   :outertype: ReservationController

resv_advanced_hold
^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Map<String, String> resv_advanced_hold(AdvancedRequest request) throws PSSException, PCEException
   :outertype: ReservationController

resv_filter_connections
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Set<Connection> resv_filter_connections(Filter filter)
   :outertype: ReservationController

resv_get_details
^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Connection resv_get_details(String connectionId)
   :outertype: ReservationController

resv_gui
^^^^^^^^

.. java:method:: @RequestMapping public String resv_gui(Model model)
   :outertype: ReservationController

resv_list
^^^^^^^^^

.. java:method:: @RequestMapping public String resv_list(Model model)
   :outertype: ReservationController

resv_list_connections
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Set<Connection> resv_list_connections()
   :outertype: ReservationController

resv_minimal_hold
^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Map<String, String> resv_minimal_hold(MinimalRequest request) throws PSSException, PCEException
   :outertype: ReservationController

resv_preCheck
^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public PreCheckResponse resv_preCheck(MinimalRequest request) throws PSSException, PCEException
   :outertype: ReservationController

resv_precheck_advanced
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public PreCheckResponse resv_precheck_advanced(AdvancedRequest request) throws PSSException, PCEException
   :outertype: ReservationController

resv_timebar
^^^^^^^^^^^^

.. java:method:: @RequestMapping public String resv_timebar(Model model)
   :outertype: ReservationController

resv_view
^^^^^^^^^

.. java:method:: @RequestMapping public String resv_view(String connectionId, Model model)
   :outertype: ReservationController

