.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.resv.ent RequestedVlanPipeE

.. java:import:: net.es.oscars.resv.ent ReservedVlanE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util.stream Collectors

EroPCE
======

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Component public class EroPCE

   Created by jeremy on 7/22/16.

Methods
-------
computeSpecifiedERO
^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, List<TopoEdge>> computeSpecifiedERO(RequestedVlanPipeE requestPipe, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList) throws PCEException
   :outertype: EroPCE

   Depends on DijkstraPCE to construct the Physical-Layer EROs for a request after pruning the topology based on requested ERO parameters

   :param requestPipe: Requested pipe with required reservation parameters, and non-empty ERO specifications
   :throws PCEException:
   :return: A two-element Map containing both the forward-direction (A->Z) ERO and the reverse-direction (Z->A) ERO

