.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.pce.exc DuplicateConnectionIdException

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.pce TopPCE

.. java:import:: net.es.oscars.pss PSSException

.. java:import:: net.es.oscars.pss.svc PssResourceService

.. java:import:: net.es.oscars.resv.dao ConnectionRepository

.. java:import:: net.es.oscars.st.prov ProvState

.. java:import:: net.es.oscars.st.resv ResvState

.. java:import:: org.modelmapper ModelMapper

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.util.stream Stream

ResvService
===========

.. java:package:: net.es.oscars.resv.svc
   :noindex:

.. java:type:: @Service @Transactional @Slf4j public class ResvService

Constructors
------------
ResvService
^^^^^^^^^^^

.. java:constructor:: @Autowired public ResvService(TopPCE topPCE, ConnectionRepository connRepo, PssResourceService pssResourceService)
   :outertype: ResvService

Methods
-------
abort
^^^^^

.. java:method:: public void abort(ConnectionE c)
   :outertype: ResvService

archiveReservation
^^^^^^^^^^^^^^^^^^

.. java:method:: public void archiveReservation(ConnectionE c)
   :outertype: ResvService

   Populates the ArchivedBlueprintE of a Connection when it transitions into a final state from a reserved/active one. This enables freeing of reserved resources but maintains tracking and archival info on the connection.

   :param c: ConnectionE to archive

commit
^^^^^^

.. java:method:: public void commit(ConnectionE c)
   :outertype: ResvService

delete
^^^^^^

.. java:method:: public void delete(ConnectionE resv)
   :outertype: ResvService

findAll
^^^^^^^

.. java:method:: public List<ConnectionE> findAll()
   :outertype: ResvService

findByConnectionId
^^^^^^^^^^^^^^^^^^

.. java:method:: public Optional<ConnectionE> findByConnectionId(String connectionId)
   :outertype: ResvService

generated
^^^^^^^^^

.. java:method:: public void generated(ConnectionE c)
   :outertype: ResvService

hold
^^^^

.. java:method:: public void hold(ConnectionE c) throws PSSException, PCEException
   :outertype: ResvService

ofHeldTimeout
^^^^^^^^^^^^^

.. java:method:: public Stream<ConnectionE> ofHeldTimeout(Integer timeoutMs)
   :outertype: ResvService

ofProvState
^^^^^^^^^^^

.. java:method:: public Stream<ConnectionE> ofProvState(ProvState provState)
   :outertype: ResvService

ofResvState
^^^^^^^^^^^

.. java:method:: public Stream<ConnectionE> ofResvState(ResvState resvState)
   :outertype: ResvService

preCheck
^^^^^^^^

.. java:method:: public Boolean preCheck(ConnectionE c) throws PSSException, PCEException
   :outertype: ResvService

provFailed
^^^^^^^^^^

.. java:method:: public void provFailed(ConnectionE c)
   :outertype: ResvService

save
^^^^

.. java:method:: public void save(ConnectionE resv)
   :outertype: ResvService

timeout
^^^^^^^

.. java:method:: public void timeout(ConnectionE c)
   :outertype: ResvService

