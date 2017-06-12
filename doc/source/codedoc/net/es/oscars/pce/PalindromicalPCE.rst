.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.pce.exc PCEException

.. java:import:: net.es.oscars.resv.ent RequestedVlanPipeE

.. java:import:: net.es.oscars.resv.ent ReservedVlanE

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo Topology

.. java:import:: net.es.oscars.topo.svc TopoService

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

PalindromicalPCE
================

.. java:package:: net.es.oscars.pce
   :noindex:

.. java:type:: @Slf4j @Component public class PalindromicalPCE

   Created by jeremy on 6/22/16.

Methods
-------
computePalindromicERO
^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, List<TopoEdge>> computePalindromicERO(RequestedVlanPipeE requestPipe, Map<String, Map<String, Integer>> bwAvailMap, List<ReservedVlanE> rsvVlanList) throws PCEException
   :outertype: PalindromicalPCE

   Depends on DijkstraPCE to construct the Physical-Layer EROs for a request

   :param requestPipe: Requested pipe with required reservation parameters
   :throws PCEException:
   :return: A two-element Map containing both the forward-direction (A->Z) ERO and the reverse-direction (Z->A) ERO

