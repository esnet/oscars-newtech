.. java:import:: net.es.oscars.acct.dao CustomerRepository

.. java:import:: net.es.oscars.acct.ent CustomerE

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Service

.. java:import:: org.springframework.transaction.annotation Transactional

.. java:import:: java.util List

.. java:import:: java.util Optional

CustService
===========

.. java:package:: net.es.oscars.acct.svc
   :noindex:

.. java:type:: @Service @Transactional public class CustService

Constructors
------------
CustService
^^^^^^^^^^^

.. java:constructor:: @Autowired public CustService(CustomerRepository custRepo)
   :outertype: CustService

Methods
-------
delete
^^^^^^

.. java:method:: public void delete(CustomerE customer)
   :outertype: CustService

findAll
^^^^^^^

.. java:method:: public List<CustomerE> findAll()
   :outertype: CustService

findByName
^^^^^^^^^^

.. java:method:: public Optional<CustomerE> findByName(String name)
   :outertype: CustService

save
^^^^

.. java:method:: public CustomerE save(CustomerE customer)
   :outertype: CustService

