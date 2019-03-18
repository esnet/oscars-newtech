.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto IntRange

.. java:import:: java.util.regex Matcher

.. java:import:: java.util.regex Pattern

IntRangeParsing
===============

.. java:package:: net.es.oscars.helpers
   :noindex:

.. java:type:: @Slf4j public class IntRangeParsing

Methods
-------
isValidIntRangeInput
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public static Boolean isValidIntRangeInput(String text)
   :outertype: IntRangeParsing

mergeIntRanges
^^^^^^^^^^^^^^

.. java:method:: public static List<IntRange> mergeIntRanges(List<IntRange> input)
   :outertype: IntRangeParsing

retrieveIntRanges
^^^^^^^^^^^^^^^^^

.. java:method:: public static List<IntRange> retrieveIntRanges(String text) throws NumberFormatException
   :outertype: IntRangeParsing

