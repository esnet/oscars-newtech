package net.es.oscars.pce;

import net.es.oscars.topo.beans.TopoAdjcy;
import net.es.oscars.topo.beans.TopoUrn;
import org.jgrapht.graph.DirectedWeightedMultigraph;

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

}
