.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.bwavail.svc BandwidthAvailabilityService

.. java:import:: net.es.oscars.dto.bwavail BandwidthAvailabilityRequest

.. java:import:: net.es.oscars.dto.bwavail BandwidthAvailabilityResponse

.. java:import:: net.es.oscars.dto.rsrc ReservableBandwidth

.. java:import:: net.es.oscars.dto.spec ReservedBandwidth

.. java:import:: net.es.oscars.resv.ent ReservedBandwidthE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: net.es.oscars.webui.dto MinimalBwAvailRequest

.. java:import:: org.joda.time DateTime

.. java:import:: org.modelmapper ModelMapper

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Controller

.. java:import:: java.util.stream Collectors

TopologyController
==================

.. java:package:: net.es.oscars.webui.cont
   :noindex:

.. java:type:: @Slf4j @Controller public class TopologyController

Constructors
------------
TopologyController
^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public TopologyController(TopoService topoService, BandwidthAvailabilityService bwAvailService, TopologyProvider topologyProvider)
   :outertype: TopologyController

Methods
-------
getAllPortCapacity
^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Map<String, Integer> getAllPortCapacity()
   :outertype: TopologyController

getAllReservedBw
^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public List<ReservedBandwidth> getAllReservedBw()
   :outertype: TopologyController

getBwAvailability
^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public BandwidthAvailabilityResponse getBwAvailability(MinimalBwAvailRequest minReq)
   :outertype: TopologyController

get_device2port_map
^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Map<String, Set<String>> get_device2port_map()
   :outertype: TopologyController

get_port2device_map
^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Map<String, String> get_port2device_map()
   :outertype: TopologyController

get_port_capacity
^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Map<String, Integer> get_port_capacity(List<String> ports)
   :outertype: TopologyController

get_reserved_bw
^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public List<ReservedBandwidth> get_reserved_bw(List<String> resUrns)
   :outertype: TopologyController

get_single_port_set
^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public Set<String> get_single_port_set(String deviceURN)
   :outertype: TopologyController

