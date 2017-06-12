.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.topo.dao UrnAdjcyRepository

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent ReservableBandwidthE

.. java:import:: net.es.oscars.topo.ent ReservableVlanE

.. java:import:: net.es.oscars.topo.ent UrnAdjcyE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.dto.topo.enums IfceType

.. java:import:: net.es.oscars.dto.topo.enums UrnType

.. java:import:: net.es.oscars.topo.prop TopoProperties

.. java:import:: net.es.oscars.topo.serialization UrnAdjcy

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.io File

.. java:import:: java.io IOException

TopoFileImporter
================

.. java:package:: net.es.oscars.topo.pop
   :noindex:

.. java:type:: @Slf4j @Service public class TopoFileImporter

Constructors
------------
TopoFileImporter
^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public TopoFileImporter(UrnRepository urnRepo, UrnAdjcyRepository adjcyRepo, TopoProperties topoProperties)
   :outertype: TopoFileImporter

Methods
-------
importFromFile
^^^^^^^^^^^^^^

.. java:method:: @Transactional public void importFromFile(boolean overwrite, String devicesFilename, String adjciesFilename) throws IOException
   :outertype: TopoFileImporter

startup
^^^^^^^

.. java:method:: @Transactional public void startup()
   :outertype: TopoFileImporter

