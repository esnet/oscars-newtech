.. java:import:: net.es.oscars.resv.ent ConnectionE

.. java:import:: org.springframework.data.repository CrudRepository

.. java:import:: org.springframework.stereotype Repository

.. java:import:: java.util List

.. java:import:: java.util Optional

ConnectionRepository
====================

.. java:package:: net.es.oscars.resv.dao
   :noindex:

.. java:type:: @Repository public interface ConnectionRepository extends CrudRepository<ConnectionE, Long>

Methods
-------
findAll
^^^^^^^

.. java:method::  List<ConnectionE> findAll()
   :outertype: ConnectionRepository

findByConnectionId
^^^^^^^^^^^^^^^^^^

.. java:method::  Optional<ConnectionE> findByConnectionId(String connectionId)
   :outertype: ConnectionRepository

