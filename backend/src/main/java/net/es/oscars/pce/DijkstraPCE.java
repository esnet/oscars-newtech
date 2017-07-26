package net.es.oscars.pce;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.EroHop;
import net.es.oscars.topo.beans.TopoAdjcy;
import net.es.oscars.topo.beans.TopoUrn;
import org.apache.commons.collections15.Transformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DijkstraPCE {

    public List<EroHop> computeShortestPathEdges(List<TopoAdjcy> adjcies, TopoUrn src, TopoUrn dst) {

        Graph<TopoUrn, TopoAdjcy> graph = new DirectedSparseMultigraph<>();

        Transformer<TopoAdjcy, Long> wtTransformer = edge -> {
            Long max = 0L;
            for (Long metric : edge.getMetrics().values()) {
                if (metric > max) {
                    max = metric;
                }
            }
            return max;
        };
        for (TopoAdjcy adjcy : adjcies) {
            if (!graph.containsVertex(adjcy.getA())) {
                graph.addVertex(adjcy.getA());
            }
            if (!graph.containsVertex(adjcy.getZ())) {
                graph.addVertex(adjcy.getZ());
            }
            graph.addEdge(adjcy, adjcy.getA(), adjcy.getZ(), EdgeType.DIRECTED);
        }



        DijkstraShortestPath<TopoUrn, TopoAdjcy> alg = new DijkstraShortestPath<>(graph, wtTransformer);

        List<TopoAdjcy> path = alg.getPath(src, dst);

        List<EroHop> result = new ArrayList<>();
        if (path.isEmpty()) {
            return result;
        } else {
            TopoUrn a = path.get(0).getA();
            EroHop aDeviceHop = EroHop.builder().urn(a.getUrn()).build();
            result.add(aDeviceHop);

            path.forEach(ta -> {
                EroHop nextZ = EroHop.builder().urn(ta.getZ().getUrn()).build();
                result.add(nextZ);
            });
            return result;
        }

    }
}
