.. java:import:: lombok AllArgsConstructor

.. java:import:: lombok Builder

.. java:import:: lombok Data

.. java:import:: lombok NoArgsConstructor

.. java:import:: java.util HashSet

.. java:import:: java.util NoSuchElementException

.. java:import:: java.util Optional

.. java:import:: java.util Set

IntRange
========

.. java:package:: net.es.oscars.dto
   :noindex:

.. java:type:: @Data @NoArgsConstructor @AllArgsConstructor @Builder public class IntRange

Methods
-------
contains
^^^^^^^^

.. java:method:: public boolean contains(Integer i)
   :outertype: IntRange

subtract
^^^^^^^^

.. java:method:: public static Set<IntRange> subtract(IntRange range, Integer i) throws NoSuchElementException
   :outertype: IntRange

