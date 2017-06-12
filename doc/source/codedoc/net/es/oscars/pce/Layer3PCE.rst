.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.resv.ent Layer3FlowE

.. java:import:: net.es.oscars.resv.ent ScheduleSpecificationE

.. java:import:: org.springframework.stereotype Component

Layer3PCE
=========

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Component public class Layer3PCE

Methods
-------
makeReserved
^^^^^^^^^^^^

.. java:method:: public Layer3FlowE makeReserved(Layer3FlowE req_f, ScheduleSpecificationE scheduleSpec) throws PCEException
   :outertype: Layer3PCE

