.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums PortLayer

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.resv.ent RequestedVlanFixtureE

.. java:import:: net.es.oscars.resv.ent RequestedVlanPipeE

.. java:import:: net.es.oscars.resv.ent ReservedVlanE

.. java:import:: net.es.oscars.servicetopo ServiceLayerTopology

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util.stream Collectors

NonPalindromicalPCE
===================

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Component public class NonPalindromicalPCE

   Created by jeremy on 6/22/16.

Methods
-------
computeNonPalindromicERO
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, List<TopoEdge>> computeNonPalindromicERO(RequestedVlanPipeE requestPipe, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList) throws PCEException
   :outertype: NonPalindromicalPCE

   Depends on DijkstraPCE and ServiceLayerTopology to construct and build the Service-Layer EROs, and then map them to Physical-Layer EROs

   :param requestPipe: Requested pipe with required reservation parameters
   :throws PCEException:
   :return: A two-element Map containing both the forward-direction (A->Z) ERO and the reverse-direction (Z->A) ERO

