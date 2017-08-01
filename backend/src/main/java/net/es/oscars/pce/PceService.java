package net.es.oscars.pce;

import net.es.oscars.app.exc.PCEException;
import net.es.oscars.resv.ent.EroHop;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.resv.ent.VlanPipe;
import net.es.oscars.resv.enums.BwDirection;
import net.es.oscars.resv.enums.EroDirection;
import net.es.oscars.resv.svc.ResvLibrary;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.Interval;
import net.es.oscars.web.beans.PcePath;
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
    private PalindromicalPCE palindromicalPCE;


    public PceResponse calculatePaths(PceRequest request) throws PCEException {
        PceResponse response = PceResponse.builder().build();

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
        Integer maxAvail = 0;
        for (Integer i : availEgressBw.values()) {
            if (maxAvail < i) {
                maxAvail = i;
            }
        }
        for (Integer i : availIngressBw.values()) {
            if (maxAvail < i) {
                maxAvail = i;
            }
        }

        Map<String, TopoUrn> baseline = topoService.getTopoUrnMap();
        Map<String, Integer> baselineIngressBw = ResvLibrary
                .availableBandwidthMap(BwDirection.INGRESS, baseline, new HashMap<>());

        Map<String, Integer> baselineEgressBw = ResvLibrary
                .availableBandwidthMap(BwDirection.EGRESS, baseline, new HashMap<>());
        Map<String, Set<IntRange>> baselineVlans = ResvLibrary.availableVlanMap(baseline, new HashSet<>());

        // at this point we know:
        // - the baseline topology
        // - the currently available topology
        // - the max available bandwidth anywhere on the network


        // first we will calculate the shortest possible path based on our topology metrics
        // this does not take into account anything reserved or the requested bandwidths

        Map<EroDirection, List<EroHop>> shortestEros = palindromicalPCE
                .palindromicERO(noBwPipe, baselineIngressBw, baselineEgressBw, baselineVlans);

        PcePath shortest = PcePath.builder()
                .azEro(shortestEros.get(EroDirection.A_TO_Z))
                .zaEro(shortestEros.get(EroDirection.Z_TO_A))
                .build();

        this.pathBandwidths(shortest, availIngressBw, availEgressBw);
        response.setShortest(shortest);

        // then we calculate the shortest path that fits the user request
        Map<EroDirection, List<EroHop>> fitsEros = palindromicalPCE
                .palindromicERO(bwPipe, availIngressBw, availEgressBw, baselineVlans);

        PcePath fits = PcePath.builder()
                .azEro(fitsEros.get(EroDirection.A_TO_Z))
                .zaEro(fitsEros.get(EroDirection.Z_TO_A))
                .build();

        this.pathBandwidths(fits, availIngressBw, availEgressBw);
        response.setFits(fits);

        // TODO: calculate widest, bestAz, bestZa


        return response;
    }


    private void pathBandwidths(PcePath pcePath,
                                Map<String, Integer> availIngressBw,
                                Map<String, Integer> availEgressBw) {

        Map<String, TopoUrn> baseline = topoService.getTopoUrnMap();
        Map<String, Integer> baselineIngressBw = ResvLibrary
                .availableBandwidthMap(BwDirection.INGRESS, baseline, new HashMap<>());

        Map<String, Integer> baselineEgressBw = ResvLibrary
                .availableBandwidthMap(BwDirection.EGRESS, baseline, new HashMap<>());


        pcePath.setAzAvailable(Integer.MAX_VALUE);
        pcePath.setZaAvailable(Integer.MAX_VALUE);
        pcePath.setAzBaseline(Integer.MAX_VALUE);
        pcePath.setZaBaseline(Integer.MAX_VALUE);


        List<EroHop> azHops = pcePath.getAzEro();
        for (int i = 0; i < azHops.size(); i++) {
            String urn = azHops.get(i).getUrn();
            Integer base = pcePath.getAzBaseline();
            Integer avail = pcePath.getAzAvailable();

            if (i % 3 == 1) {
                if (base > baselineEgressBw.get(urn)) {
                    pcePath.setAzBaseline(baselineEgressBw.get(urn));
                }
                if (avail > availEgressBw.get(urn)) {
                    pcePath.setAzAvailable(availEgressBw.get(urn));
                }

            } else if (i % 3 == 2) {
                if (base > baselineIngressBw.get(urn)) {
                    pcePath.setAzBaseline(baselineIngressBw.get(urn));
                }
                if (avail > availIngressBw.get(urn)) {
                    pcePath.setAzAvailable(availIngressBw.get(urn));
                }

            }
        }
        List<EroHop> zaHops = pcePath.getZaEro();

        for (int i = 0; i < zaHops.size(); i++) {
            String urn = zaHops.get(i).getUrn();
            Integer base = pcePath.getZaBaseline();
            Integer avail = pcePath.getZaAvailable();
            if (i % 3 == 1) {
                if (base > baselineEgressBw.get(urn)) {
                    pcePath.setZaBaseline(baselineEgressBw.get(urn));
                }
                if (avail > availEgressBw.get(urn)) {
                    pcePath.setZaAvailable(availEgressBw.get(urn));
                }

            } else if (i % 3 == 2) {
                if (base > baselineIngressBw.get(urn)) {
                    pcePath.setZaBaseline(baselineIngressBw.get(urn));
                }
                if (avail > availIngressBw.get(urn)) {
                    pcePath.setZaAvailable(availIngressBw.get(urn));
                }
            }
        }
    }
}
