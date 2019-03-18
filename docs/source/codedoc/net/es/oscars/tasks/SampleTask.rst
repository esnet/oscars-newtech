.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.scheduling.annotation Scheduled

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.web.client RestTemplate

SampleTask
==========

.. java:package:: net.es.oscars.tasks
   :noindex:

.. java:type:: @Slf4j @Component public class SampleTask

Methods
-------
findPath
^^^^^^^^

.. java:method:: @Scheduled public void findPath()
   :outertype: SampleTask

