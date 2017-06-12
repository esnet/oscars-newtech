.. java:import:: java.util List

.. java:import:: java.util Set

CircuitFlow
===========

.. java:package:: net.es.oscars.dto.resv
   :noindex:

.. java:type:: @Data @Builder @NoArgsConstructor @AllArgsConstructor public class CircuitFlow

Fields
------
azMbps
^^^^^^

.. java:field::  Integer azMbps
   :outertype: CircuitFlow

azRoute
^^^^^^^

.. java:field::  List<String> azRoute
   :outertype: CircuitFlow

blacklist
^^^^^^^^^

.. java:field::  Set<String> blacklist
   :outertype: CircuitFlow

destDevice
^^^^^^^^^^

.. java:field:: @NonNull  String destDevice
   :outertype: CircuitFlow

destPorts
^^^^^^^^^

.. java:field::  Set<String> destPorts
   :outertype: CircuitFlow

destVlan
^^^^^^^^

.. java:field::  String destVlan
   :outertype: CircuitFlow

numPaths
^^^^^^^^

.. java:field::  Integer numPaths
   :outertype: CircuitFlow

palindromic
^^^^^^^^^^^

.. java:field::  String palindromic
   :outertype: CircuitFlow

priority
^^^^^^^^

.. java:field::  Integer priority
   :outertype: CircuitFlow

sourceDevice
^^^^^^^^^^^^

.. java:field:: @NonNull  String sourceDevice
   :outertype: CircuitFlow

sourcePorts
^^^^^^^^^^^

.. java:field::  Set<String> sourcePorts
   :outertype: CircuitFlow

sourceVlan
^^^^^^^^^^

.. java:field::  String sourceVlan
   :outertype: CircuitFlow

survivability
^^^^^^^^^^^^^

.. java:field::  String survivability
   :outertype: CircuitFlow

zaMbps
^^^^^^

.. java:field::  Integer zaMbps
   :outertype: CircuitFlow

zaRoute
^^^^^^^

.. java:field::  List<String> zaRoute
   :outertype: CircuitFlow

