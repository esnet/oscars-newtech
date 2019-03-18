.. java:import:: freemarker.template TemplateException

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.pss.beans AluTemplatePaths

.. java:import:: net.es.oscars.pss.beans ConfigException

.. java:import:: net.es.oscars.pss.tpl Assembler

.. java:import:: net.es.oscars.pss.tpl Stringifier

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.io IOException

AluCommandGenerator
===================

.. java:package:: net.es.oscars.pss.svc
   :noindex:

.. java:type:: @Slf4j @Component public class AluCommandGenerator

Methods
-------
build
^^^^^

.. java:method:: public String build(AluParams params) throws ConfigException
   :outertype: AluCommandGenerator

dismantle
^^^^^^^^^

.. java:method:: public String dismantle(AluParams params) throws ConfigException
   :outertype: AluCommandGenerator

