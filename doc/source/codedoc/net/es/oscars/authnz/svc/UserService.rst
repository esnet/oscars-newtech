.. java:import:: net.es.oscars.authnz.dao UserRepository

.. java:import:: net.es.oscars.authnz.ent UserE

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.util ArrayList

.. java:import:: java.util List

.. java:import:: java.util Optional

UserService
===========

.. java:package:: net.es.oscars.authnz.svc
   :noindex:

.. java:type:: @Service @Transactional public class UserService

Constructors
------------
UserService
^^^^^^^^^^^

.. java:constructor:: @Autowired public UserService(UserRepository userRepo)
   :outertype: UserService

Methods
-------
getInstitutions
^^^^^^^^^^^^^^^

.. java:method:: public List<String> getInstitutions()
   :outertype: UserService

matchUsernameAndEncoded
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Optional<UserE> matchUsernameAndEncoded(String username, String encoded)
   :outertype: UserService

save
^^^^

.. java:method:: public UserE save(UserE user)
   :outertype: UserService

usersExist
^^^^^^^^^^

.. java:method:: public boolean usersExist()
   :outertype: UserService

