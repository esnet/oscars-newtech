.. java:import:: net.es.oscars.resv.ent ReservedVlanE

.. java:import:: org.springframework.data.jpa.repository Query

.. java:import:: org.springframework.data.repository CrudRepository

.. java:import:: org.springframework.stereotype Repository

.. java:import:: java.time Instant

.. java:import:: java.util List

.. java:import:: java.util Optional

ReservedVlanRepository
======================

.. java:package:: net.es.oscars.resv.dao
   :noindex:

.. java:type:: @Repository public interface ReservedVlanRepository extends CrudRepository<ReservedVlanE, Long>

Methods
-------
findAll
^^^^^^^

.. java:method::  List<ReservedVlanE> findAll()
   :outertype: ReservedVlanRepository

findOverlappingInterval
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  Optional<List<ReservedVlanE>> findOverlappingInterval(Instant period_start, Instant period_end)
   :outertype: ReservedVlanRepository

