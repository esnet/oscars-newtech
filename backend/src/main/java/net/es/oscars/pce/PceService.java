package net.es.oscars.pce;

import net.es.oscars.app.exc.PCEException;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.resv.ent.VlanPipe;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.PceRequest;
import net.es.oscars.web.beans.PceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PceService {
    @Autowired
    private ResvService resvService;

    @Autowired
    private TopoService topoService;

    @Autowired
    private WidestPathsPCE widestPCE;

    @Autowired
    private PalindromicalPCE palindromicalPCE;


    public PceResponse calculatePaths(PceRequest request) throws PCEException {

        VlanJunction aj = VlanJunction.builder()
                .refId(request.getA())
                .deviceUrn(request.getA())
                .build();
        VlanJunction zj = VlanJunction.builder()
                .refId(request.getZ())
                .deviceUrn(request.getZ())
                .build();

        VlanPipe bwPipe = VlanPipe.builder()
                .a(aj)
                .z(zj)
                .azBandwidth(request.getAzBw())
                .zaBandwidth(request.getZaBw()).build();


        VlanPipe noBwPipe = VlanPipe.builder()
                .a(aj)
                .z(zj)
                .azBandwidth(0)
                .zaBandwidth(0).build();


        Map<String, Integer> availIngressBw = resvService.availableIngBws(request.getInterval());
        Map<String, Integer> availEgressBw = resvService.availableEgBws(request.getInterval());

        // at this point we know:
        // - the baseline topology
        // - the currently available topology
        // - the max available bandwidth anywhere on the network
        PceResponse response = palindromicalPCE
                .shortestAndFits(bwPipe, availIngressBw, availEgressBw);

        // To find the widest path we get the shortest paths from A to Z and evaluate them.
        //
        // We start with the hop-length of the shortest path, then add a search
        // "relaxation radius", expressed in graph hops.
        //
        // Each device is 3 hops from the next (A <-> A:1 <-> B:2 <-> B)
        // so a radius of 10 makes the PCE evaluate up to around 4-5 devices off the shortest-path

        // TODO: make search radius configurable
        Integer maxLength = response.getShortest().getAzEro().size() + 15;

        PceResponse widestResponse = widestPCE.calculateWidest(noBwPipe, availIngressBw, availEgressBw, maxLength);

        response.setWidestAZ(widestResponse.getWidestAZ());
        response.setWidestZA(widestResponse.getWidestZA());
        response.setWidestSum(widestResponse.getWidestSum());

        response.setEvaluated(widestResponse.getEvaluated());

        return response;
    }


}
