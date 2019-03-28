.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.pss.cmd Command

.. java:import:: net.es.oscars.dto.pss.cmd CommandResponse

.. java:import:: net.es.oscars.dto.pss.cmd CommandStatus

.. java:import:: net.es.oscars.dto.pss.cmd GenerateResponse

.. java:import:: net.es.oscars.dto.pss.cp ControlPlaneHealth

.. java:import:: net.es.oscars.pss.beans ConfigException

.. java:import:: net.es.oscars.pss.svc HealthService

.. java:import:: net.es.oscars.pss.svc RouterConfigBuilder

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.http HttpStatus

PssController
=============

.. java:package:: net.es.oscars.pss.rest
   :noindex:

.. java:type:: @Slf4j @RestController public class PssController

Constructors
------------
PssController
^^^^^^^^^^^^^

.. java:constructor:: @Autowired public PssController(HealthService healthService, RouterConfigBuilder routerConfigBuilder)
   :outertype: PssController

Methods
-------
command
^^^^^^^

.. java:method:: @RequestMapping public CommandResponse command(Command cmd)
   :outertype: PssController

commandStatus
^^^^^^^^^^^^^

.. java:method:: @RequestMapping public CommandStatus commandStatus(String commandId)
   :outertype: PssController

generate
^^^^^^^^

.. java:method:: @RequestMapping public GenerateResponse generate(Command cmd) throws ConfigException, JsonProcessingException
   :outertype: PssController

handleConfigException
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @ResponseBody @ExceptionHandler @ResponseStatus public Map<String, Object> handleConfigException(ConfigException ex)
   :outertype: PssController

handleResourceNotFoundException
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @ExceptionHandler @ResponseStatus public void handleResourceNotFoundException(NoSuchElementException ex)
   :outertype: PssController

health
^^^^^^

.. java:method:: @RequestMapping public ControlPlaneHealth health()
   :outertype: PssController

