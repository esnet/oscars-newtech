package net.es.oscars.pce;

import net.es.oscars.resv.ent.EroHop;
import net.es.oscars.resv.enums.BwDirection;
import net.es.oscars.resv.enums.EroDirection;
import net.es.oscars.resv.svc.ResvLibrary;
import net.es.oscars.topo.beans.TopoAdjcy;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.web.beans.PcePath;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PceLibrary {


    public static DirectedWeightedMultigraph<TopoUrn, TopoAdjcy>
        makeGraph(List<TopoAdjcy> adjcies, Map<TopoAdjcy, Double> weights) {

        DirectedWeightedMultigraph<TopoUrn, TopoAdjcy> graph
                = new DirectedWeightedMultigraph<>(TopoAdjcy.class);

        for (TopoAdjcy adjcy : adjcies) {
            if (!graph.containsVertex(adjcy.getA())) {
                graph.addVertex(adjcy.getA());
            }
            if (!graph.containsVertex(adjcy.getZ())) {
                graph.addVertex(adjcy.getZ());
            }
            graph.addEdge(adjcy.getA(), adjcy.getZ(), adjcy);

            graph.setEdgeWeight(adjcy, weights.get(adjcy));
        }
        return graph;

    }

    public static void pathBandwidths(PcePath pcePath,
                                 Map<String, TopoUrn> baseline,
                                 Map<String, Integer> availIngressBw,
                                 Map<String, Integer> availEgressBw) {

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

    public static List<EroHop> toEro(GraphPath<TopoUrn, TopoAdjcy> path) {
        List<EroHop> ero = new ArrayList<>();
        if (path != null && path.getEdgeList().size() > 0) {
            path.getVertexList().forEach(v -> {
                ero.add(EroHop.builder().urn(v.getUrn()).build());
            });
        }
        return ero;

    }
}
