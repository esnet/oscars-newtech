.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: org.springframework.data.repository CrudRepository

.. java:import:: org.springframework.stereotype Repository

.. java:import:: java.util List

.. java:import:: java.util Optional

UrnRepository
=============

.. java:package:: net.es.oscars.topo.dao
   :noindex:

.. java:type:: @Repository public interface UrnRepository extends CrudRepository<UrnE, Long>

Methods
-------
findAll
^^^^^^^

.. java:method::  List<UrnE> findAll()
   :outertype: UrnRepository

findByUrn
^^^^^^^^^

.. java:method::  Optional<UrnE> findByUrn(String urn)
   :outertype: UrnRepository

