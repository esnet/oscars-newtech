.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto IntRange

.. java:import:: net.es.oscars.dto.rsrc ReservableVlan

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.resv.ent ReservedVlanE

TopoAssistant
=============

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j public class TopoAssistant

Methods
-------
makeEro
^^^^^^^

.. java:method:: public static List<String> makeEro(List<TopoEdge> topoEdges, boolean reverse)
   :outertype: TopoAssistant

subtractVlan
^^^^^^^^^^^^

.. java:method:: public static ReservableVlan subtractVlan(ReservableVlan reservable, ReservedVlanE reserved)
   :outertype: TopoAssistant

