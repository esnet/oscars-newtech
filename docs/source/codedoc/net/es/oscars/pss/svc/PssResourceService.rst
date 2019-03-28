.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto IntRange

.. java:import:: net.es.oscars.dto.resv ResourceType

.. java:import:: net.es.oscars.helpers IntRangeParsing

.. java:import:: net.es.oscars.helpers ResourceChooser

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.pss.prop PssConfig

.. java:import:: net.es.oscars.resv.dao ReservedPssResourceRepository

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.time Instant

.. java:import:: java.util HashSet

.. java:import:: java.util List

.. java:import:: java.util Optional

.. java:import:: java.util Set

PssResourceService
==================

.. java:package:: net.es.oscars.pss.svc
   :noindex:

.. java:type:: @Service @Transactional @Slf4j public class PssResourceService

Constructors
------------
PssResourceService
^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public PssResourceService(ResourceChooser chooser, TopoService topoService, ReservedPssResourceRepository pssResRepo, PssConfig pssConfig)
   :outertype: PssResourceService

Methods
-------
release
^^^^^^^

.. java:method:: public void release(ConnectionE conn)
   :outertype: PssResourceService

reserve
^^^^^^^

.. java:method:: public void reserve(ConnectionE conn) throws PSSException
   :outertype: PssResourceService

