.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.authnz.svc UserService

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Controller

.. java:import:: org.springframework.ui Model

.. java:import:: org.springframework.web.bind.annotation PathVariable

.. java:import:: org.springframework.web.bind.annotation RequestMapping

.. java:import:: org.springframework.web.bind.annotation RequestMethod

.. java:import:: org.springframework.web.bind.annotation ResponseBody

MainController
==============

.. java:package:: net.es.oscars.webui.cont
   :noindex:

.. java:type:: @Slf4j @Controller public class MainController

Constructors
------------
MainController
^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public MainController(UserService userService, TopoService topoService)
   :outertype: MainController

Methods
-------
device_suggestions
^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public List<String> device_suggestions()
   :outertype: MainController

home
^^^^

.. java:method:: @RequestMapping public String home(Model model)
   :outertype: MainController

institution_suggestions
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public List<String> institution_suggestions()
   :outertype: MainController

loginPage
^^^^^^^^^

.. java:method:: @RequestMapping public String loginPage(Model model)
   :outertype: MainController

reactListPage
^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String reactListPage(Model model)
   :outertype: MainController

reactPage
^^^^^^^^^

.. java:method:: @RequestMapping public String reactPage(Model model)
   :outertype: MainController

reactResvPage
^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String reactResvPage(Model model)
   :outertype: MainController

reactWhatifPage
^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String reactWhatifPage(Model model)
   :outertype: MainController

vlanEdge_device_suggestions
^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public List<String> vlanEdge_device_suggestions(String device)
   :outertype: MainController

vlanEdge_suggestions
^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public List<String> vlanEdge_suggestions()
   :outertype: MainController

