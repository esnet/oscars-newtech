.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.resv Connection

.. java:import:: net.es.oscars.dto.viz VizGraph

.. java:import:: net.es.oscars.resv.ent ConnectionE

.. java:import:: net.es.oscars.resv.svc ResvService

.. java:import:: net.es.oscars.webui.viz VizExporter

.. java:import:: org.modelmapper ModelMapper

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Controller

.. java:import:: org.springframework.web.bind.annotation PathVariable

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RequestMethod

.. java:import:: org.springframework.web.bind.annotation ResponseBody

.. java:import:: java.util Arrays

.. java:import:: java.util NoSuchElementException

VizController
=============

.. java:package:: net.es.oscars.webui.cont
   :noindex:

.. java:type:: @Slf4j @Controller public class VizController

Constructors
------------
VizController
^^^^^^^^^^^^^

.. java:constructor:: @Autowired public VizController(VizExporter vizExporter, ResvService resvService)
   :outertype: VizController

Methods
-------
listTopoPorts
^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public String[] listTopoPorts()
   :outertype: VizController

viz_connection
^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public VizGraph viz_connection(String connectionId)
   :outertype: VizController

viz_topology
^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public VizGraph viz_topology(String classifier)
   :outertype: VizController

