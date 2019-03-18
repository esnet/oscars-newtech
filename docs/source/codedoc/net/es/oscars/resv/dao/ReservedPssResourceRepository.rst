.. java:import:: net.es.oscars.resv.ent ReservedPssResourceE

.. java:import:: org.springframework.data.jpa.repository Query

.. java:import:: org.springframework.data.repository CrudRepository

.. java:import:: org.springframework.stereotype Repository

.. java:import:: java.time Instant

.. java:import:: java.util List

.. java:import:: java.util Optional

ReservedPssResourceRepository
=============================

.. java:package:: net.es.oscars.resv.dao
   :noindex:

.. java:type:: @Repository public interface ReservedPssResourceRepository extends CrudRepository<ReservedPssResourceE, Long>

Methods
-------
findAll
^^^^^^^

.. java:method::  List<ReservedPssResourceE> findAll()
   :outertype: ReservedPssResourceRepository

findOverlappingInterval
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  Optional<List<ReservedPssResourceE>> findOverlappingInterval(Instant period_start, Instant period_end)
   :outertype: ReservedPssResourceRepository

