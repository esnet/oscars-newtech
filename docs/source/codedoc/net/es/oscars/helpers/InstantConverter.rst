.. java:import:: javax.persistence AttributeConverter

.. java:import:: javax.persistence Converter

.. java:import:: java.time Instant

.. java:import:: java.util Date

InstantConverter
================

.. java:package:: net.es.oscars.helpers
   :noindex:

.. java:type:: @Converter public class InstantConverter implements AttributeConverter<Instant, Date>

Methods
-------
convertToDatabaseColumn
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Override public Date convertToDatabaseColumn(Instant date)
   :outertype: InstantConverter

convertToEntityAttribute
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: @Override public Instant convertToEntityAttribute(Date value)
   :outertype: InstantConverter

