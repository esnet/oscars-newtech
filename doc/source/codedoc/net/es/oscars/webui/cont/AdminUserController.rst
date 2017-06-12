.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.authnz.dao UserRepository

.. java:import:: net.es.oscars.authnz.ent UserE

.. java:import:: net.es.oscars.authnz.svc UserService

.. java:import:: net.es.oscars.dto.auth Permissions

.. java:import:: net.es.oscars.dto.auth User

.. java:import:: org.modelmapper ModelMapper

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.http HttpStatus

.. java:import:: org.springframework.security.crypto.bcrypt BCryptPasswordEncoder

.. java:import:: org.springframework.stereotype Controller

.. java:import:: org.springframework.ui Model

.. java:import:: java.util List

.. java:import:: java.util NoSuchElementException

.. java:import:: java.util.stream Collectors

AdminUserController
===================

.. java:package:: net.es.oscars.webui.cont
   :noindex:

.. java:type:: @Slf4j @Controller public class AdminUserController

Constructors
------------
AdminUserController
^^^^^^^^^^^^^^^^^^^

.. java:constructor:: @Autowired public AdminUserController(UserRepository userRepo, UserService userService)
   :outertype: AdminUserController

Methods
-------
admin_user_add
^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String admin_user_add(Model model)
   :outertype: AdminUserController

admin_user_add_submit
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String admin_user_add_submit(User addedUser)
   :outertype: AdminUserController

admin_user_del_submit
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String admin_user_del_submit(User userToDelete)
   :outertype: AdminUserController

admin_user_edit
^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String admin_user_edit(String username, Model model)
   :outertype: AdminUserController

admin_user_list
^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String admin_user_list(Model model)
   :outertype: AdminUserController

admin_user_pwd_submit
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String admin_user_pwd_submit(User updatedUser)
   :outertype: AdminUserController

admin_user_update_submit
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping public String admin_user_update_submit(User updatedUser)
   :outertype: AdminUserController

getInstitutions
^^^^^^^^^^^^^^^

.. java:method:: @RequestMapping @ResponseBody public List<String> getInstitutions()
   :outertype: AdminUserController

handleResourceNotFoundException
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @ExceptionHandler @ResponseStatus public void handleResourceNotFoundException(NoSuchElementException ex)
   :outertype: AdminUserController

