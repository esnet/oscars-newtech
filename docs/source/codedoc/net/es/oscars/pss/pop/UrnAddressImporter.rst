.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.pss.dao UrnAddressRepository

.. java:import:: net.es.oscars.pss.ent UrnAddressE

.. java:import:: net.es.oscars.topo.prop TopoProperties

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.io File

.. java:import:: java.io IOException

.. java:import:: java.util Arrays

.. java:import:: java.util List

UrnAddressImporter
==================

.. java:package:: net.es.oscars.pss.pop
   :noindex:

.. java:type:: @Slf4j @Component public class UrnAddressImporter

Constructors
------------
UrnAddressImporter
^^^^^^^^^^^^^^^^^^

.. java:constructor:: public UrnAddressImporter(TopoProperties topoProperties, UrnAddressRepository repo)
   :outertype: UrnAddressImporter

Methods
-------
startup
^^^^^^^

.. java:method:: public void startup()
   :outertype: UrnAddressImporter

