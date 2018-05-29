package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.EroHop;
import net.es.oscars.topo.beans.TopoAdjcy;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.web.beans.PcePath;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class DijkstraPCE {
    @Autowired
    FloydWarshall floydWarshall;

    public PcePath shortestPath(DirectedWeightedMultigraph<TopoUrn, TopoAdjcy> graph , TopoUrn src, TopoUrn dst) {

        DijkstraShortestPath<TopoUrn, TopoAdjcy> alg = new DijkstraShortestPath<>(graph);
        GraphPath<TopoUrn, TopoAdjcy> path = alg.getPath(src, dst);
        double w = alg.getPathWeight(src,dst);
        log.info("shortest path cost: "+w);
        List<EroHop> azEro = PceLibrary.toEro(path);
        List<EroHop> zaEro = new ArrayList<>();
        for (EroHop hop : azEro) {
            zaEro.add(EroHop.builder().urn(hop.getUrn()).build());
        }

        Collections.reverse(zaEro);

        return PcePath.builder()
                .azEro(azEro)
                .zaEro(zaEro)
                .cost(w)
                .build();

    }

    @Deprecated
    private List<EroHop> floyd(List<TopoAdjcy> adjcies, TopoUrn src, TopoUrn dst,
                                   Map<String, Integer> availIngressBw,
                                   Map<String, Integer> availEgressBw) {

        DirectedWeightedMultigraph<TopoUrn, TopoAdjcy> graph = PceLibrary.makeGraph(adjcies, new HashMap<>());
        Map<TopoAdjcy, Integer> capacityMap = new HashMap<>();
        for (TopoAdjcy adjcy : adjcies) {
            Integer aIngress = availIngressBw.get(adjcy.getA().getUrn());
            Integer aEgress = availEgressBw.get(adjcy.getA().getUrn());
            Integer zIngress = availIngressBw.get(adjcy.getZ().getUrn());
            Integer zEgress = availEgressBw.get(adjcy.getA().getUrn());

            if (aIngress == null) aIngress = Integer.MAX_VALUE;
            if (aEgress == null) aEgress = Integer.MAX_VALUE;
            if (zIngress == null) zIngress = Integer.MAX_VALUE;
            if (zEgress == null) zEgress = Integer.MAX_VALUE;

            Integer azCapacity = aEgress;
            if (zIngress < azCapacity) {
                azCapacity = zIngress;
            }
            Integer zaCapacity = zEgress;
            if (aIngress < azCapacity) {
                zaCapacity = aIngress;
            }
            capacityMap.put(adjcy, azCapacity+zaCapacity);


        }

        floydWarshall.prepare(graph, capacityMap);
        List<EroHop> ero = new ArrayList<>();
        ero.add(EroHop.builder().urn(src.getUrn()).build());

        floydWarshall.tracePath(src.getUrn(), dst.getUrn()).forEach(h -> {
            ero.add(EroHop.builder().urn(h).build());
        });
        ero.add(EroHop.builder().urn(dst.getUrn()).build());
        return ero;

    }


    @Deprecated
    private List<EroHop> kruskal(List<TopoAdjcy> adjcies, TopoUrn src, TopoUrn dst,
                                   Map<String, Integer> availIngressBw,
                                   Map<String, Integer> availEgressBw) {


        Map<TopoAdjcy, Double> weights = new HashMap<>();
        for (TopoAdjcy adjcy : adjcies) {
            Integer aIngress = availIngressBw.get(adjcy.getA().getUrn());
            Integer aEgress = availEgressBw.get(adjcy.getA().getUrn());
            Integer zIngress = availIngressBw.get(adjcy.getZ().getUrn());
            Integer zEgress = availEgressBw.get(adjcy.getA().getUrn());

            if (aIngress == null) aIngress = Integer.MAX_VALUE;
            if (aEgress == null) aEgress = Integer.MAX_VALUE;
            if (zIngress == null) zIngress = Integer.MAX_VALUE;
            if (zEgress == null) zEgress = Integer.MAX_VALUE;

            Integer azCapacity = aEgress;
            if (zIngress < azCapacity) {
                azCapacity = zIngress;
            }
            Integer zaCapacity = zEgress;
            if (aIngress < azCapacity) {
                zaCapacity = aIngress;
            }
            Integer totalCapacity = azCapacity + zaCapacity;
            Double weight = 1000 / totalCapacity.doubleValue();

            weights.put(adjcy, weight);


        }
        DirectedWeightedMultigraph<TopoUrn, TopoAdjcy> graph = PceLibrary.makeGraph(adjcies, weights);
        KruskalMinimumSpanningTree<TopoUrn, TopoAdjcy> mstAlg = new KruskalMinimumSpanningTree<>(graph);
        DirectedWeightedMultigraph<TopoUrn, TopoAdjcy> mst = new DirectedWeightedMultigraph<>(TopoAdjcy.class);

        mstAlg.getSpanningTree().getEdges().forEach(e -> {
            mst.addVertex(e.getA());
            mst.addVertex(e.getZ());
            mst.addEdge(e.getA(), e.getZ(), e);
            mst.setEdgeWeight(e, 1);
        });


        DijkstraShortestPath<TopoUrn, TopoAdjcy> alg = new DijkstraShortestPath<>(mst);
        GraphPath<TopoUrn, TopoAdjcy> path = alg.getPath(src, dst);
        return PceLibrary.toEro(path);

    }



}
