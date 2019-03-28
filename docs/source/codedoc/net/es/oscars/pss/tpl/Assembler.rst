.. java:import:: freemarker.template TemplateException

.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.io IOException

.. java:import:: java.util HashMap

.. java:import:: java.util List

.. java:import:: java.util Map

Assembler
=========

.. java:package:: net.es.oscars.pss.tpl
   :noindex:

.. java:type:: @Slf4j @Component public class Assembler

Methods
-------
assemble
^^^^^^^^

.. java:method:: public String assemble(List<String> fragments, String templateFilename) throws IOException, TemplateException
   :outertype: Assembler

