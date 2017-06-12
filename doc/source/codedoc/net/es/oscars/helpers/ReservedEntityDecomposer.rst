.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.topo.enums UrnType

.. java:import:: net.es.oscars.topo.dao UrnRepository

.. java:import:: net.es.oscars.topo.ent EdgeE

.. java:import:: net.es.oscars.topo.ent UrnE

.. java:import:: org.springframework.beans.factory.annotation Autowired

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util.stream Collectors

ReservedEntityDecomposer
========================

.. java:package:: net.es.oscars.helpers
   :noindex:

.. java:type:: @Slf4j @Component public class ReservedEntityDecomposer

Methods
-------
decomposeEdgeList
^^^^^^^^^^^^^^^^^

.. java:method:: public List<String> decomposeEdgeList(List<EdgeE> edges)
   :outertype: ReservedEntityDecomposer

decomposeEthPipe
^^^^^^^^^^^^^^^^

.. java:method:: public Set<UrnE> decomposeEthPipe(ReservedEthPipeE pipe)
   :outertype: ReservedEntityDecomposer

   Convert a reserved Ethernet pipe into a set of URNs. Retrieves URNs from URN repository.

   :param pipe: - A reserved Ethernet pipe
   :return: Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe

decomposeEthPipe
^^^^^^^^^^^^^^^^

.. java:method:: public Set<UrnE> decomposeEthPipe(ReservedEthPipeE pipe, List<UrnE> urns)
   :outertype: ReservedEntityDecomposer

   Convert a reserved Ethernet pipe into a set of URNs. Retrieves URNs from input URN list.

   :param pipe: - A reserved Ethernet pipe
   :param urns: - A list of URN objects
   :return: Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe

decomposeEthPipe
^^^^^^^^^^^^^^^^

.. java:method:: public Set<UrnE> decomposeEthPipe(ReservedEthPipeE pipe, Map<String, UrnE> urnMap)
   :outertype: ReservedEntityDecomposer

   Convert a reserved Ethernet pipe into a set of URNs. Retrieves URNs from input URN map.

   :param pipe: - A reserved Ethernet pipe
   :param urnMap: - A map of URN strings to URN objects
   :return: Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe

decomposeEthPipeIntoAzEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeEthPipeIntoAzEROList(ReservedEthPipeE pipe)
   :outertype: ReservedEntityDecomposer

decomposeEthPipeIntoAzEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeEthPipeIntoAzEROList(ReservedEthPipeE pipe, List<UrnE> urns)
   :outertype: ReservedEntityDecomposer

decomposeEthPipeIntoAzEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeEthPipeIntoAzEROList(ReservedEthPipeE pipe, Map<String, UrnE> urnMap)
   :outertype: ReservedEntityDecomposer

decomposeEthPipeIntoZaEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeEthPipeIntoZaEROList(ReservedEthPipeE pipe)
   :outertype: ReservedEntityDecomposer

decomposeEthPipeIntoZaEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeEthPipeIntoZaEROList(ReservedEthPipeE pipe, List<UrnE> urns)
   :outertype: ReservedEntityDecomposer

decomposeEthPipeIntoZaEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeEthPipeIntoZaEROList(ReservedEthPipeE pipe, Map<String, UrnE> urnMap)
   :outertype: ReservedEntityDecomposer

decomposeJunction
^^^^^^^^^^^^^^^^^

.. java:method:: public Set<UrnE> decomposeJunction(ReservedVlanJunctionE junction) throws NoSuchElementException
   :outertype: ReservedEntityDecomposer

   Convert a requested junction into a set of URNs. Retrieves URNs from junction and fixtures, no passed in URNs needed.

   :param junction: - The reserved junction
   :return: Set of all URNs making up the junction (device) and fixtures (ports)

decomposeMplsPipe
^^^^^^^^^^^^^^^^^

.. java:method:: public Set<UrnE> decomposeMplsPipe(ReservedMplsPipeE pipe)
   :outertype: ReservedEntityDecomposer

   Convert a reserved MPLS pipe into a set of URNs. Retrieves URNs from URN repository.

   :param pipe: - A MPLS Ethernet pipe
   :return: Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe

decomposeMplsPipe
^^^^^^^^^^^^^^^^^

.. java:method:: public Set<UrnE> decomposeMplsPipe(ReservedMplsPipeE pipe, List<UrnE> urns)
   :outertype: ReservedEntityDecomposer

   Convert a reserved MPLS pipe into a set of URNs. Retrieves URNs from input URN list.

   :param pipe: - A reserved MPLS pipe
   :param urns: - A list of URN objects
   :return: Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe

decomposeMplsPipe
^^^^^^^^^^^^^^^^^

.. java:method:: public Set<UrnE> decomposeMplsPipe(ReservedMplsPipeE pipe, Map<String, UrnE> urnMap)
   :outertype: ReservedEntityDecomposer

   Convert a reserved MPLS pipe into a set of URNs. Retrieves URNs from input URN map.

   :param pipe: - A reserved MPLS pipe
   :param urnMap: - A map of URN strings to URN objects
   :return: Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe

decomposeMplsPipeIntoAzEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeMplsPipeIntoAzEROList(ReservedMplsPipeE pipe)
   :outertype: ReservedEntityDecomposer

decomposeMplsPipeIntoAzEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeMplsPipeIntoAzEROList(ReservedMplsPipeE pipe, List<UrnE> urns)
   :outertype: ReservedEntityDecomposer

decomposeMplsPipeIntoAzEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeMplsPipeIntoAzEROList(ReservedMplsPipeE pipe, Map<String, UrnE> urnMap)
   :outertype: ReservedEntityDecomposer

decomposeMplsPipeIntoZaEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeMplsPipeIntoZaEROList(ReservedMplsPipeE pipe)
   :outertype: ReservedEntityDecomposer

decomposeMplsPipeIntoZaEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeMplsPipeIntoZaEROList(ReservedMplsPipeE pipe, List<UrnE> urns)
   :outertype: ReservedEntityDecomposer

decomposeMplsPipeIntoZaEROList
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> decomposeMplsPipeIntoZaEROList(ReservedMplsPipeE pipe, Map<String, UrnE> urnMap)
   :outertype: ReservedEntityDecomposer

decomposeReservedBlueprint
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Set<UrnE> decomposeReservedBlueprint(ReservedBlueprintE resBlueprint)
   :outertype: ReservedEntityDecomposer

   Convert a reserved blueprint into a set of URNs. Retrieves URNs from repository.

   :param resBlueprint: - The reserved blueprint
   :return: Set of all URNs that make up the reserved pipes/junction/fixtures in the blueprint.

decomposeReservedBlueprint
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Set<UrnE> decomposeReservedBlueprint(ReservedBlueprintE resBlueprint, List<UrnE> urns)
   :outertype: ReservedEntityDecomposer

   Convert a reserved blueprint into a set of URNs. Retrieves URNs from input list of URNs

   :param resBlueprint: - The reserved blueprint
   :param urns: - A list of URNs
   :return: Set of all URNs that make up the reserved pipes/junction/fixtures in the blueprint.

decomposeReservedBlueprint
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Set<UrnE> decomposeReservedBlueprint(ReservedBlueprintE resBlueprint, Map<String, UrnE> urnMap)
   :outertype: ReservedEntityDecomposer

   Convert a reserved blueprint into a set of URNs. Retrieves URNs from input URN map

   :param resBlueprint: - The reserved blueprint
   :param urnMap: - A map of URN strings to URN objects
   :return: Set of all URNs that make up the reserved pipes/junction/fixtures in the blueprint.

translateStringListToUrns
^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public List<UrnE> translateStringListToUrns(List<String> pathElements)
   :outertype: ReservedEntityDecomposer

