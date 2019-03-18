.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.web.bind.annotation ControllerAdvice

.. java:import:: org.springframework.web.bind.annotation ExceptionHandler

ControllerConfig
================

.. java:package:: net.es.oscars.webui.cont
   :noindex:

.. java:type:: @Slf4j @ControllerAdvice public class ControllerConfig

Methods
-------
handleExceptions
^^^^^^^^^^^^^^^^

.. java:method:: @ExceptionHandler public void handleExceptions(Exception anExc) throws Exception
   :outertype: ControllerConfig

