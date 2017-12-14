package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PCEException;
import net.es.oscars.resv.ent.EroHop;
import net.es.oscars.resv.ent.VlanPipe;
import net.es.oscars.resv.enums.EroDirection;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.TopoAdjcy;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.PcePath;
import net.es.oscars.web.beans.PceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by jeremy on 6/22/16.
 */
@Slf4j
@Component
public class PalindromicalPCE {
    @Autowired
    private TopoService topoService;

    @Autowired
    private DijkstraPCE dijkstraPCE;


    public PceResponse shortestAndFits(VlanPipe requestPipe,
                                      Map<String, Integer> availIngressBw,
                                      Map<String, Integer> availEgressBw) {

        List<TopoAdjcy> topoAdjcies = topoService.getTopoAdjcies();
        Map<String, TopoUrn> baseline = topoService.getTopoUrnMap();


        TopoUrn src = topoService.getTopoUrnMap().get(requestPipe.getA().getDeviceUrn());
        TopoUrn dst = topoService.getTopoUrnMap().get(requestPipe.getZ().getDeviceUrn());

        PcePath shortest = dijkstraPCE.shortestPath(topoAdjcies, src, dst);


        List<TopoAdjcy> pruned = PruningLibrary.pruneAdjacencies(topoAdjcies,
                requestPipe.getAzBandwidth(), requestPipe.getZaBandwidth(),
                availIngressBw, availEgressBw);


        PcePath fits = dijkstraPCE.shortestPath(pruned, src, dst);

        PceLibrary.pathBandwidths(fits, baseline, availIngressBw, availEgressBw);
        PceLibrary.pathBandwidths(shortest, baseline, availIngressBw, availEgressBw);


        return PceResponse.builder()
                .fits(fits)
                .shortest(shortest)
                .build();


    }
}