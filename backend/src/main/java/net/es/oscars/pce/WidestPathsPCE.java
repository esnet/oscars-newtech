package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PCEException;
import net.es.oscars.resv.ent.EroHop;
import net.es.oscars.resv.ent.VlanPipe;
import net.es.oscars.topo.beans.TopoAdjcy;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.PcePath;
import net.es.oscars.web.beans.PceResponse;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class WidestPathsPCE {
    @Autowired
    private TopoService topoService;


    public PceResponse calculateWidest(VlanPipe requestPipe,
                                       Map<String, Integer> availIngressBw,
                                       Map<String, Integer> availEgressBw,
                                       Integer maxLength ) throws PCEException {
        PceResponse response = PceResponse.builder()
                .widestAZ(null)
                .widestZA(null)
                .widestSum(null)
                .build();

        Map<String, TopoUrn> baseline = topoService.getTopoUrnMap();

        List<TopoAdjcy> topoAdjcies = topoService.getTopoAdjcies();
        Map<TopoAdjcy, Double> costs = new HashMap<>();
        for (TopoAdjcy adjcy : topoAdjcies) {
            costs.put(adjcy, 1D);
        }

        DirectedWeightedMultigraph<TopoUrn, TopoAdjcy> graph = PceLibrary.makeGraph(topoAdjcies, costs);

        TopoUrn src = topoService.getTopoUrnMap().get(requestPipe.getA().getDeviceUrn());
        TopoUrn dst = topoService.getTopoUrnMap().get(requestPipe.getZ().getDeviceUrn());


        AllDirectedPaths<TopoUrn, TopoAdjcy> ap = new AllDirectedPaths<>(graph);

        Instant ps = Instant.now();
        List<GraphPath<TopoUrn, TopoAdjcy>> paths = ap.getAllPaths(src, dst, true, maxLength);
        Instant pe = Instant.now();
        log.info(paths.size()+ " distinct paths found between "+src.getUrn() +
                " and "+ dst.getUrn()+ " found in time "+ Duration.between(ps, pe));

        response.setEvaluated(paths.size());


        Instant es = Instant.now();
        for (GraphPath<TopoUrn, TopoAdjcy> path : paths) {

            List<EroHop> azEro = PceLibrary.toEro(path);
            List<EroHop> zaEro = new ArrayList<>();
            for (EroHop hop : azEro) {
                zaEro.add(EroHop.builder().urn(hop.getUrn()).build());
            }

            Collections.reverse(zaEro);

            PcePath pcePath = PcePath.builder()
                    .azEro(azEro)
                    .zaEro(zaEro)
                    .build();
            PceLibrary.pathBandwidths(pcePath, baseline, availIngressBw, availEgressBw);
            pcePath.setWeight(path.getWeight());

            // first path is best by default
            if (response.getWidestAZ() == null) {
                response.setWidestAZ(pcePath);
                response.setWidestZA(pcePath);
                response.setWidestSum(pcePath);
            } else {
                if (response.getWidestAZ().getAzAvailable() < pcePath.getAzAvailable()) {
                    response.setWidestAZ(pcePath);
                } else if (response.getWidestAZ().getAzAvailable().equals(pcePath.getAzAvailable())) {
                    if (response.getWidestAZ().getWeight() > pcePath.getWeight()) {
                        response.setWidestAZ(pcePath);
                    }
                }
                if (response.getWidestZA().getZaAvailable() < pcePath.getZaAvailable()) {
                    response.setWidestZA(pcePath);
                } else if (response.getWidestZA().getZaAvailable().equals(pcePath.getZaAvailable())) {
                    if (response.getWidestZA().getWeight() > pcePath.getWeight()) {
                        response.setWidestZA(pcePath);
                    }
                }
                Integer prevSum = response.getWidestSum().getAzAvailable()+response.getWidestSum().getZaAvailable();
                Integer newSum = pcePath.getAzAvailable() + pcePath.getZaAvailable();
                if (prevSum < newSum) {
                    response.setWidestSum(pcePath);
                } else if (prevSum.equals(newSum)) {
                    if (response.getWidestSum().getWeight() > pcePath.getWeight()) {
                        response.setWidestSum(pcePath);
                    }
                }
            }

        }
        Instant ee = Instant.now();
        log.info("widest paths found in time "+ Duration.between(es, ee));


        return response;
    }
}