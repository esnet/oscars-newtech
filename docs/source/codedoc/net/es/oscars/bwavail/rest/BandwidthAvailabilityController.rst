.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.bwavail.svc BandwidthAvailabilityService

.. java:import:: net.es.oscars.dto.bwavail BandwidthAvailabilityRequest

.. java:import:: net.es.oscars.dto.bwavail BandwidthAvailabilityResponse

.. java:import:: net.es.oscars.dto.bwavail PortBandwidthAvailabilityRequest

.. java:import:: net.es.oscars.dto.bwavail PortBandwidthAvailabilityResponse

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Controller

.. java:import:: org.springframework.web.bind.annotation RequestBody

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RequestMethod

.. java:import:: org.springframework.web.bind.annotation ResponseBody

BandwidthAvailabilityController
===============================

.. java:package:: net.es.oscars.bwavail.rest
   :noindex:

.. java:type:: @Slf4j @Controller public class BandwidthAvailabilityController

   Provides bandwidth availability information for given paths or parts of the network.

Methods
-------
getAllPortAvailability
^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public PortBandwidthAvailabilityResponse getAllPortAvailability(PortBandwidthAvailabilityRequest bwRequest)
   :outertype: BandwidthAvailabilityController

getPathAvailability
^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public BandwidthAvailabilityResponse getPathAvailability(BandwidthAvailabilityRequest request)
   :outertype: BandwidthAvailabilityController

