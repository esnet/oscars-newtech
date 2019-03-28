.. java:import:: net.es.oscars.resv.ent ReservedBandwidthE

.. java:import:: org.springframework.data.jpa.repository Query

.. java:import:: org.springframework.data.repository CrudRepository

.. java:import:: org.springframework.stereotype Repository

.. java:import:: java.time Instant

.. java:import:: java.util List

.. java:import:: java.util Optional

ReservedBandwidthRepository
===========================

.. java:package:: net.es.oscars.resv.dao
   :noindex:

.. java:type:: @Repository public interface ReservedBandwidthRepository extends CrudRepository<ReservedBandwidthE, Long>

Methods
-------
findAll
^^^^^^^

.. java:method::  List<ReservedBandwidthE> findAll()
   :outertype: ReservedBandwidthRepository

findOverlappingInterval
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Query  Optional<List<ReservedBandwidthE>> findOverlappingInterval(Instant period_start, Instant period_end)
   :outertype: ReservedBandwidthRepository

