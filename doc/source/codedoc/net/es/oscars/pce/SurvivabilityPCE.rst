.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.spec SurvivabilityType

.. java:import:: net.es.oscars.dto.topo.enums PortLayer

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.servicetopo SurvivableServiceLayerTopology

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util.stream Collectors

SurvivabilityPCE
================

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Component public class SurvivabilityPCE

   Created by jeremy on 7/27/16.

Methods
-------
computeSurvivableERO
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, List<TopoEdge>> computeSurvivableERO(RequestedVlanPipeE requestPipe, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList) throws PCEException
   :outertype: SurvivabilityPCE

   Depends on BhandariPCE to construct the survivable physical-layer EROs for a request after pruning the topology based on requested parameters

   :param requestPipe: Requested pipe with required reservation parameters
   :param bwAvailMap:
   :throws PCEException:
   :return: A four- element Map containing both the primary and secondary link-disjoint forward-direction EROs and the primary and secondary link-disjoint reverse-direction EROs

