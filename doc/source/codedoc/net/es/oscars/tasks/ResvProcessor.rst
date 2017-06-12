.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.pss.svc PSSAdapter

.. java:import:: net.es.oscars.pss.svc PssResourceService

.. java:import:: net.es.oscars.resv.svc ResvService

.. java:import:: net.es.oscars.st.prov ProvState

.. java:import:: net.es.oscars.st.resv ResvState

.. java:import:: net.es.oscars.tasks.prop ProcessingProperties

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.scheduling.annotation Scheduled

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.transaction.annotation Transactional

ResvProcessor
=============

.. java:package:: net.es.oscars.tasks
   :noindex:

.. java:type:: @Slf4j @Component public class ResvProcessor

Constructors
------------
ResvProcessor
^^^^^^^^^^^^^

.. java:constructor:: @Autowired public ResvProcessor(ResvService resvService, PSSAdapter pssAdapter, ProcessingProperties processingProperties)
   :outertype: ResvProcessor

Methods
-------
processingLoop
^^^^^^^^^^^^^^

.. java:method:: @Scheduled @Transactional public void processingLoop()
   :outertype: ResvProcessor

startup
^^^^^^^

.. java:method:: public void startup()
   :outertype: ResvProcessor

