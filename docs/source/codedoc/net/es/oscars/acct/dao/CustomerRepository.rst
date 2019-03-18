.. java:import:: net.es.oscars.acct.ent CustomerE

.. java:import:: org.springframework.data.repository CrudRepository

.. java:import:: org.springframework.stereotype Repository

.. java:import:: java.util List

.. java:import:: java.util Optional

CustomerRepository
==================

.. java:package:: net.es.oscars.acct.dao
   :noindex:

.. java:type:: @Repository public interface CustomerRepository extends CrudRepository<CustomerE, Long>

Methods
-------
findAll
^^^^^^^

.. java:method::  List<CustomerE> findAll()
   :outertype: CustomerRepository

findByName
^^^^^^^^^^

.. java:method::  Optional<CustomerE> findByName(String name)
   :outertype: CustomerRepository

