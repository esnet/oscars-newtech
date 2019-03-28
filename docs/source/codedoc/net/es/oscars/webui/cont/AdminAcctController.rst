.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.acct.ent CustomerE

.. java:import:: net.es.oscars.acct.svc CustService

.. java:import:: net.es.oscars.dto.acct Customer

.. java:import:: org.modelmapper ModelMapper

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.dao DataIntegrityViolationException

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.stereotype Controller

.. java:import:: org.springframework.ui Model

.. java:import:: java.util List

.. java:import:: java.util NoSuchElementException

.. java:import:: java.util.stream Collectors

AdminAcctController
===================

.. java:package:: net.es.oscars.webui.cont
   :noindex:

.. java:type:: @Slf4j @Controller public class AdminAcctController

Constructors
------------
AdminAcctController
^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public AdminAcctController(CustService custService)
   :outertype: AdminAcctController

Methods
-------
admin_comp_list
^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String admin_comp_list(Model model)
   :outertype: AdminAcctController

admin_cust_edit
^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String admin_cust_edit(String name, Model model)
   :outertype: AdminAcctController

admin_user_update_submit
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String admin_user_update_submit(Customer updatedCustomer)
   :outertype: AdminAcctController

handleDataIntegrityViolationException
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @ExceptionHandler @ResponseStatus public void handleDataIntegrityViolationException(DataIntegrityViolationException ex)
   :outertype: AdminAcctController

handleResourceNotFoundException
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @ExceptionHandler @ResponseStatus public void handleResourceNotFoundException(NoSuchElementException ex)
   :outertype: AdminAcctController

