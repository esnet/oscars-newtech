package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.TopoAdjcy;
import net.es.oscars.topo.beans.TopoUrn;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class FloydWarshall {

    private Map<String, Map<String, Integer>> bandwidths;
    private Map<String, Map<String, String>> nextVertex;

    public void prepare(DirectedWeightedMultigraph<TopoUrn, TopoAdjcy> g, Map<TopoAdjcy, Integer> capacity) {
        bandwidths = new HashMap<>();
        nextVertex = new HashMap<>();

        for (TopoUrn a : g.vertexSet()) {
            nextVertex.put(a.getUrn(), new HashMap<>());
            bandwidths.put(a.getUrn(), new HashMap<>());
            for (TopoUrn b : g.vertexSet()) {
                bandwidths.get(a.getUrn()).put(b.getUrn(), Integer.MIN_VALUE);
                nextVertex.get(a.getUrn()).put(b.getUrn(), null);

            }
        }

        ArrayList<TopoUrn> vertices = new ArrayList<>();
        vertices.addAll(g.vertexSet());
        ArrayList<TopoAdjcy> adjcies = new ArrayList<>();
        adjcies.addAll(g.edgeSet());

        for (TopoUrn a : vertices) {
            bandwidths.get(a.getUrn()).put(a.getUrn(), 0);
        }
        for (TopoAdjcy adjcy : adjcies) {
            String a = adjcy.getA().getUrn();
            String z = adjcy.getZ().getUrn();
            bandwidths.get(a).put(z, capacity.get(adjcy));
//            log.info(a+" "+z+ " "+capacity.get(adjcy));
        }
        int cnt = 0;
        for (TopoUrn k : vertices) {
            for (TopoUrn i : vertices) {
                for (TopoUrn j : vertices) {
                    Integer ikBw = bandwidths.get(i.getUrn()).get(k.getUrn());
                    Integer kjBw = bandwidths.get(k.getUrn()).get(j.getUrn());
                    Integer ijBw = bandwidths.get(i.getUrn()).get(j.getUrn());
                    Integer minIKandKJ = ikBw;
                    if (kjBw < minIKandKJ) {
                        minIKandKJ = kjBw;
                    }
                    if (minIKandKJ > ijBw) {
//                        log.info("new minimum for "+i.getUrn()+" "+j.getUrn()+" : "+minIKandKJ);
                        bandwidths.get(i.getUrn()).put(j.getUrn(), minIKandKJ);
//                        log.info("new next for "+i.getUrn()+" "+j.getUrn()+" : "+k.getUrn());
                        nextVertex.get(i.getUrn()).put(j.getUrn(), k.getUrn());
                    }
                    cnt ++;
                }
            }
        }
        log.info("did "+cnt+" iterations");
    }

    List<String> tracePath(String src, String dst) {
        /*
        for (Map.Entry<String, Map<String, String>> e1: nextVertex.entrySet()) {
            for (Map.Entry<String, String> e2 : e1.getValue().entrySet()) {
                log.info(e1.getKey()+ " "+e2.getKey()+" "+e2.getValue());
            }
        }
        */

        List<String> result = new ArrayList<>();
        if (bandwidths.get(src).get(dst) == Integer.MIN_VALUE) {
            log.info("no solution for "+src+" to "+dst);
            return result;
        }
        String k = nextVertex.get(src).get(dst);
        if (k == null) {
            return result;

        } else {
            result.addAll(tracePath(src, k));
            result.add(k);
            result.addAll(tracePath(k, dst));
            return result;
        }
    }




}
