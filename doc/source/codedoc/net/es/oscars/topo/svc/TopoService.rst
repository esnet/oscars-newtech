.. java:import:: com.fasterxml.jackson.core JsonProcessingException

.. java:import:: com.fasterxml.jackson.databind ObjectMapper

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.topo DevicePortMap

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.resv.dao ReservedBandwidthRepository

.. java:import:: net.es.oscars.resv.ent ReservedBandwidthE

.. java:import:: net.es.oscars.topo.dao ReservableBandwidthRepository

.. java:import:: net.es.oscars.topo.dao ReservableVlanRepository

.. java:import:: net.es.oscars.topo.dao UrnAdjcyRepository

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent ReservableBandwidthE

.. java:import:: net.es.oscars.topo.ent ReservableVlanE

.. java:import:: net.es.oscars.topo.ent UrnAdjcyE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: org.springframework.stereotype Service

.. java:import:: java.util.stream Collectors

TopoService
===========

.. java:package:: net.es.oscars.topo.svc
   :noindex:

.. java:type:: @Slf4j @Service @Component public class TopoService

Constructors
------------
TopoService
^^^^^^^^^^^

.. java:constructor:: @Autowired public TopoService(UrnAdjcyRepository adjcyRepo, UrnRepository urnRepo, ReservableVlanRepository vlanRepo, ReservableBandwidthRepository bwRepo, ReservedBandwidthRepository bwResRepo)
   :outertype: TopoService

Methods
-------
buildDeviceToPortMap
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public DevicePortMap buildDeviceToPortMap()
   :outertype: TopoService

buildPortToDeviceMap
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, String> buildPortToDeviceMap(Map<String, Set<String>> deviceToPortMap)
   :outertype: TopoService

determineIfRouterHasEthernetPorts
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public boolean determineIfRouterHasEthernetPorts(String deviceURN)
   :outertype: TopoService

device
^^^^^^

.. java:method:: public UrnE device(String urn) throws NoSuchElementException
   :outertype: TopoService

deviceModels
^^^^^^^^^^^^

.. java:method:: public Map<String, DeviceModel> deviceModels()
   :outertype: TopoService

devices
^^^^^^^

.. java:method:: public List<String> devices()
   :outertype: TopoService

edges
^^^^^

.. java:method:: public List<String> edges(Layer layer)
   :outertype: TopoService

edgesWithCapability
^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<String> edgesWithCapability(String device, Layer layer)
   :outertype: TopoService

getMultilayerTopology
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Topology getMultilayerTopology()
   :outertype: TopoService

getUrn
^^^^^^

.. java:method:: public UrnE getUrn(String urn) throws NoSuchElementException
   :outertype: TopoService

getVertexTypeFromDeviceType
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public VertexType getVertexTypeFromDeviceType(DeviceType deviceType)
   :outertype: TopoService

identifyEdgePortURNs
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Set<String> identifyEdgePortURNs()
   :outertype: TopoService

layer
^^^^^

.. java:method:: public Topology layer(Layer layer) throws NoSuchElementException
   :outertype: TopoService

lookupPortLayer
^^^^^^^^^^^^^^^

.. java:method:: public PortLayer lookupPortLayer(String portURN)
   :outertype: TopoService

reservableBandwidths
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservableBandwidthE> reservableBandwidths()
   :outertype: TopoService

reservableVlans
^^^^^^^^^^^^^^^

.. java:method:: public List<ReservableVlanE> reservableVlans()
   :outertype: TopoService

reservedBandwidths
^^^^^^^^^^^^^^^^^^

.. java:method:: public List<ReservedBandwidthE> reservedBandwidths()
   :outertype: TopoService

