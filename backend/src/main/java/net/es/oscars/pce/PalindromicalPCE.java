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

    public Map<EroDirection, List<EroHop>> palindromicERO(VlanPipe requestPipe,
                                                          Map<String, Integer> availIngressBw,
                                                          Map<String, Integer> availEgressBw,
                                                          Map<String, Set<IntRange>> availVlans) throws PCEException {
        Map<EroDirection, List<EroHop>> result = new HashMap<>();

        List<TopoAdjcy> topoAdjcies = topoService.getTopoAdjcies();

        TopoUrn src = topoService.getTopoUrnMap().get(requestPipe.getA().getDeviceUrn());
        TopoUrn dst = topoService.getTopoUrnMap().get(requestPipe.getZ().getDeviceUrn());

        List<TopoAdjcy> pruned = PruningLibrary.pruneAdjacencies(topoAdjcies,
                requestPipe.getAzBandwidth(), requestPipe.getZaBandwidth(),
                availIngressBw, availEgressBw);

        List<EroHop> azERO = dijkstraPCE.computeShortestPathEdges(pruned, src, dst);

        if (azERO.isEmpty()) {
            throw new PCEException("Empty path from Palindromical PCE");
        }

        List<EroHop> zaEro = new ArrayList<>();
        for (EroHop hop : azERO) {
            zaEro.add(EroHop.builder().urn(hop.getUrn()).build());
        }
        Collections.reverse(zaEro);

        result.put(EroDirection.A_TO_Z, azERO);
        result.put(EroDirection.Z_TO_A, zaEro);
        return result;

    }
}