package net.es.oscars.pce;

import net.es.oscars.app.exc.PCEException;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.resv.ent.VlanPipe;
import net.es.oscars.resv.svc.ResvService;
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
    private AllPathsPCE widestPCE;


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



        Map<String, Integer> availIngressBw = resvService.availableIngBws(request.getInterval());
        Map<String, Integer> availEgressBw = resvService.availableEgBws(request.getInterval());

        // at this point we know:
        // - the baseline topology
        // - the currently available topology
        // - the max available bandwidth anywhere on the network


        return  widestPCE.calculatePaths(bwPipe, availIngressBw, availEgressBw, request.getInclude(), request.getExclude());
    }


}
