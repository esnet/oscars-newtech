.. java:import:: lombok.extern.slf4j Slf4j

.. java:import:: net.es.oscars.dto.pss EthFixtureType

.. java:import:: net.es.oscars.dto.pss EthJunctionType

.. java:import:: net.es.oscars.dto.pss EthPipeType

.. java:import:: net.es.oscars.dto.pss MplsPipeType

.. java:import:: net.es.oscars.dto.resv ResourceType

.. java:import:: net.es.oscars.dto.topo TopoEdge

.. java:import:: net.es.oscars.dto.topo TopoVertex

.. java:import:: net.es.oscars.dto.topo.enums DeviceModel

.. java:import:: net.es.oscars.dto.topo.enums Layer

.. java:import:: net.es.oscars.dto.topo.enums VertexType

.. java:import:: org.springframework.stereotype Component

.. java:import:: java.util.stream Collectors

PCEAssistant
============

.. java:package:: net.es.oscars.pss
   :noindex:

.. java:type:: @Component @Slf4j public class PCEAssistant

Methods
-------
compareEthPipes
^^^^^^^^^^^^^^^

.. java:method:: public boolean compareEthPipes(ReservedEthPipeE pipe1, ReservedEthPipeE pipe2)
   :outertype: PCEAssistant

compareMplsPipes
^^^^^^^^^^^^^^^^

.. java:method:: public boolean compareMplsPipes(ReservedMplsPipeE pipe1, ReservedMplsPipeE pipe2)
   :outertype: PCEAssistant

constructJunctionPairToPipeEROMap
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public void constructJunctionPairToPipeEROMap(Map<List<TopoVertex>, Map<String, List<TopoVertex>>> junctionPairToPipeEROMap, Map<List<TopoVertex>, Layer> allJunctionPairs, List<Map<Layer, List<TopoVertex>>> azSegments, List<Map<Layer, List<TopoVertex>>> zaSegments)
   :outertype: PCEAssistant

   Construct a mapping of Junction Pairs (two vertices) to the AZ/ZA listing of vertices (the pipe) between those two junctions. Updates the pased in junctionPairToPipeEROMap and the allJunctionPairs map to keep track of which junction pairs have been created.

   :param junctionPairToPipeEROMap: - A mapping between junction pairs and pipe vertices.
   :param allJunctionPairs: - A mapping between Junction pairs and layer (determines what kind of pipe to create)
   :param azSegments: - The path segments in the AZ direction
   :param zaSegments: - The path segments in the ZA direction

decideEthPipeType
^^^^^^^^^^^^^^^^^

.. java:method:: public EthPipeType decideEthPipeType(DeviceModel aModel, DeviceModel zModel) throws PSSException
   :outertype: PCEAssistant

   Given the models of the starting/ending devices of an Ethernet pipe, determine the pipe's type

   :param aModel: - The A junction's model
   :param zModel: - The Z junction's model
   :throws PSSException:
   :return: The ethernet pipe's type

decideFixtureType
^^^^^^^^^^^^^^^^^

.. java:method:: public EthFixtureType decideFixtureType(DeviceModel model) throws PSSException
   :outertype: PCEAssistant

   Given the device model of the associated device, determine the fixture's type

   :param model: - The device's model
   :throws PSSException:
   :return: The fixture's type

decideJunctionType
^^^^^^^^^^^^^^^^^^

.. java:method:: public EthJunctionType decideJunctionType(DeviceModel model) throws PSSException
   :outertype: PCEAssistant

   Given a junction's device model, determine the junction's type

   :param model: - The device model
   :throws PSSException:
   :return: The junction's type

decideMplsPipeType
^^^^^^^^^^^^^^^^^^

.. java:method:: public MplsPipeType decideMplsPipeType(DeviceModel aModel, DeviceModel zModel) throws PSSException
   :outertype: PCEAssistant

   Given the models of the starting/ending devices of a MPLS pipe, determine the pipe's type

   :param aModel: - The A junction's device model
   :param zModel: - The Z junction's device model
   :throws PSSException:
   :return: The MPLS pipe's type

decompose
^^^^^^^^^

.. java:method:: public static List<Map<Layer, List<TopoVertex>>> decompose(List<TopoEdge> edges)
   :outertype: PCEAssistant

   Given a list of edges, convert that list to into a number of segments, based on layer. An ETHERNET segment is made entirely of switches and their ports, while a MPLS segment consists of routers and their ports.

   :param edges: - The edges to be decomposed into segments.
   :return: A list of > pairs (segments).

filterEthPipeSet
^^^^^^^^^^^^^^^^

.. java:method:: public Set<ReservedEthPipeE> filterEthPipeSet(Set<ReservedEthPipeE> pipes)
   :outertype: PCEAssistant

filterMplsPipeSet
^^^^^^^^^^^^^^^^^

.. java:method:: public Set<ReservedMplsPipeE> filterMplsPipeSet(Set<ReservedMplsPipeE> pipes)
   :outertype: PCEAssistant

neededJunctionResources
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<ResourceType, List<String>> neededJunctionResources(ReservedVlanJunctionE vj) throws PSSException
   :outertype: PCEAssistant

   Determine what resources are needed for provisioning this reserved junction.

   :param vj: - The reserved junction
   :throws PSSException:
   :return: A mapping of the needed resources

neededPipeResources
^^^^^^^^^^^^^^^^^^^

.. java:method:: public Map<String, ResourceType> neededPipeResources(ReservedMplsPipeE vp) throws PSSException
   :outertype: PCEAssistant

   Determine what resources are needed for a reserved MPLS pipe

   :param vp: - The reserved pipe
   :throws PSSException:
   :return: A mapping of the needed resources

palindromicEros
^^^^^^^^^^^^^^^

.. java:method:: public boolean palindromicEros(List<TopoEdge> azERO, List<TopoEdge> zaERO)
   :outertype: PCEAssistant

   Confirm that the two EROs are identical

   :param azERO: - A path in one direction
   :param zaERO: - A path in another direction
   :return: True if they are identical, False otherwise.

