.. java:import:: net.es.oscars.pss.ent RouterCommandsE

.. java:import:: org.springframework.data.repository CrudRepository

.. java:import:: org.springframework.stereotype Repository

.. java:import:: java.util List

.. java:import:: java.util Optional

RouterCommandsRepository
========================

.. java:package:: net.es.oscars.pss.dao
   :noindex:

.. java:type:: @Repository public interface RouterCommandsRepository extends CrudRepository<RouterCommandsE, Long>

Methods
-------
findAll
^^^^^^^

.. java:method::  List<RouterCommandsE> findAll()
   :outertype: RouterCommandsRepository

findByConnectionIdAndDeviceUrn
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method::  List<RouterCommandsE> findByConnectionIdAndDeviceUrn(String connectionId, String deviceUrn)
   :outertype: RouterCommandsRepository

