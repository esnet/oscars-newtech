package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.*;
import net.es.oscars.servicetopo.ServiceLayerTopology;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.*;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by jeremy on 6/22/16.
 */
@Slf4j
@Component
public class LacimordnilapPCE
{
    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private DijkstraPCE dijkstraPCE;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private ServiceLayerTopology serviceLayerTopology;

    /**
     * Depends on DijkstraPCE and ServiceLayerTopology to construct and build the Service-Layer EROs, and then map them to Physical-Layer EROs
     *
     * @param requestPipe Requested pipe with required reservation parameters
     * @param requestSched Requested schedule parameters
     * @return A two-element Map containing both the forward-direction (A->Z) ERO and the reverse-direction (Z->A) ERO
     * @throws PCEException
     */
    public Map<String, List<TopoEdge>> computeCimordnilapERO(RequestedVlanPipeE requestPipe, ScheduleSpecificationE requestSched, List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList) throws PCEException
    {

        /* These calls only need to be made once when topology is updated */
        serviceLayerTopology.setTopology(topoService.layer(Layer.ETHERNET));
        serviceLayerTopology.setTopology(topoService.layer(Layer.INTERNAL));
        serviceLayerTopology.setTopology(topoService.layer(Layer.MPLS));

        serviceLayerTopology.createMultilayerTopology();
        /* * */

        serviceLayerTopology.resetLogicalLinks();

        UrnE srcDeviceURN = requestPipe.getAJunction().getDeviceUrn();
        UrnE dstDeviceURN = requestPipe.getZJunction().getDeviceUrn();

        VertexType srcType = topoService.getVertexTypeFromDeviceType(srcDeviceURN.getDeviceType());
        VertexType dstType = topoService.getVertexTypeFromDeviceType(dstDeviceURN.getDeviceType());

        TopoVertex srcDevice = new TopoVertex(srcDeviceURN.getUrn(), srcType);
        TopoVertex dstDevice = new TopoVertex(srcDeviceURN.getUrn(), dstType);

        UrnE srcPortURN = requestPipe.getAJunction().getFixtures().iterator().next().getPortUrn();
        UrnE dstPortURN = requestPipe.getZJunction().getFixtures().iterator().next().getPortUrn();

        TopoVertex srcPort = new TopoVertex(srcPortURN.getUrn(), VertexType.PORT);
        TopoVertex dstPort = new TopoVertex(dstPortURN.getUrn(), VertexType.PORT);

        // Handle MPLS-layer source/destination devices
        serviceLayerTopology.buildLogicalLayerSrcNodes(srcDevice, srcPort);
        serviceLayerTopology.buildLogicalLayerDstNodes(dstDevice, dstPort);

        // Performs shortest path routing on MPLS-layer to properly assign weights to each logical link on Service-Layer
        serviceLayerTopology.calculateLogicalLinkWeights(requestPipe, requestSched, urnRepo.findAll(), rsvBwList, rsvVlanList);

        Topology slTopo = serviceLayerTopology.getSLTopology();
        Topology prunedSlTopo = pruningService.pruneWithPipe(slTopo, requestPipe, requestSched, rsvBwList, rsvVlanList);

        TopoVertex serviceLayerSrcNode;
        TopoVertex serviceLayerDstNode;

        if(srcDevice.getVertexType().equals(VertexType.SWITCH))
        {
            serviceLayerSrcNode = srcPort;
        }
        else
        {
            serviceLayerSrcNode = serviceLayerTopology.getVirtualNode(srcDevice);
            assert(serviceLayerSrcNode != null);
        }

        if(dstDevice.getVertexType().equals(VertexType.SWITCH))
        {
            serviceLayerDstNode = dstPort;
        }
        else
        {
            serviceLayerDstNode = serviceLayerTopology.getVirtualNode(dstDevice);
            assert(serviceLayerDstNode != null);
        }

        // Shortest path routing on Service-Layer
        List<TopoEdge> azServiceLayerERO = dijkstraPCE.computeShortestPathEdges(prunedSlTopo, serviceLayerSrcNode, serviceLayerDstNode);

        if (azServiceLayerERO.isEmpty())
        {
            throw new PCEException("Empty path from Asymmetric PCE");
        }

        // Get symmetric Service-Layer path in reverse-direction
        List<TopoEdge> zaServiceLayerERO = new LinkedList<>();

        // 1. Reverse the links
        for(TopoEdge azEdge : azServiceLayerERO)
        {
            Optional<TopoEdge> reverseEdge = prunedSlTopo.getEdges().stream()
                    .filter(r -> r.getA().equals(azEdge.getZ()))
                    .filter(r -> r.getZ().equals(azEdge.getA()))
                    .findFirst();

            if(reverseEdge.isPresent())
                zaServiceLayerERO.add(reverseEdge.get());
        }

        // 2. Reverse the order
        Collections.reverse(zaServiceLayerERO);

        assert(azServiceLayerERO.size() == zaServiceLayerERO.size());

        // Obtain physical ERO from Service-Layer EROs
        List<TopoEdge> azERO = serviceLayerTopology.getActualERO(azServiceLayerERO);
        List<TopoEdge> zaERO = serviceLayerTopology.getActualERO(zaServiceLayerERO);


        Map<String, List<TopoEdge>> theMap = new HashMap<>();
        theMap.put("az", azERO);
        theMap.put("za", zaERO);

        return theMap;
    }
}