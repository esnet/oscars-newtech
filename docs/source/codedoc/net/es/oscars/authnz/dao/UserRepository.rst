.. java:import:: net.es.oscars.authnz.ent UserE

.. java:import:: org.springframework.data.repository CrudRepository

.. java:import:: org.springframework.stereotype Repository

.. java:import:: java.util List

.. java:import:: java.util Optional

UserRepository
==============

.. java:package:: net.es.oscars.authnz.dao
   :noindex:

.. java:type:: @Repository public interface UserRepository extends CrudRepository<UserE, Long>

Methods
-------
findAll
^^^^^^^

.. java:method::  List<UserE> findAll()
   :outertype: UserRepository

findByCertSubject
^^^^^^^^^^^^^^^^^

.. java:method::  Optional<UserE> findByCertSubject(String certSubject)
   :outertype: UserRepository

findByUsername
^^^^^^^^^^^^^^

.. java:method::  Optional<UserE> findByUsername(String username)
   :outertype: UserRepository

