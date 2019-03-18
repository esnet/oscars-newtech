.. java:import:: net.es.oscars.pss.ent UrnAddressE

.. java:import:: org.springframework.data.repository CrudRepository

.. java:import:: org.springframework.stereotype Repository

.. java:import:: java.util List

.. java:import:: java.util Optional

UrnAddressRepository
====================

.. java:package:: net.es.oscars.pss.dao
   :noindex:

.. java:type:: @Repository public interface UrnAddressRepository extends CrudRepository<UrnAddressE, Long>

Methods
-------
findAll
^^^^^^^

.. java:method::  List<UrnAddressE> findAll()
   :outertype: UrnAddressRepository

findByUrn
^^^^^^^^^

.. java:method::  Optional<UrnAddressE> findByUrn(String urn)
   :outertype: UrnAddressRepository

