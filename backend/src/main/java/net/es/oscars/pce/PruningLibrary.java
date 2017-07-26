package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.TopoAdjcy;
import net.es.oscars.topo.enums.Layer;

import java.util.*;

@Slf4j
public class PruningLibrary {

    public static List<TopoAdjcy> pruneAdjacencies(List<TopoAdjcy> topoAdjcies,
                                                     Integer azBw, Integer zaBw,
                                                     Map<String, Integer> availIngressBw,
                                                     Map<String, Integer> availEgressBw) {

        List<TopoAdjcy> pruned = new ArrayList<>();

        for (TopoAdjcy adjcy : topoAdjcies) {
            // internal adjacencies are port-to-device; always acceptable
            if (adjcy.getMetrics().containsKey(Layer.INTERNAL)) {
                log.info("adding "+adjcy.asLogString());
                pruned.add(adjcy);
            } else {
                // this is a port-to-port adjacency. we accept it if it can satisfy either going the A-Z direction or Z-A.
                // A-Z direction:      min(aEgress, zIngress) > azBw && min (aIngress, zEgress) >= zaBw

                Integer aIngress = availIngressBw.get(adjcy.getA().getUrn());
                Integer aEgress = availEgressBw.get(adjcy.getA().getUrn());
                Integer zIngress = availIngressBw.get(adjcy.getZ().getUrn());
                Integer zEgress = availEgressBw.get(adjcy.getZ().getUrn());

                // first of all, aIngress SHOULD == zEgress and zEgress == zIngress
                if (aEgress > zIngress || aEgress < zIngress) {
                    log.error("mismatch in available capacity over internal link");
                }
                if (aIngress > zEgress || aIngress < zEgress) {
                    log.error("mismatch in available capacity over internal link");
                }

                //
                Integer azCapacity = aEgress;
                if (zIngress < aEgress) {
                    azCapacity = zIngress;
                }
                Integer zaCapacity = zEgress;
                if (aIngress < zEgress) {
                    zaCapacity = aIngress;
                }
                boolean fits = false;

                if (azBw <= azCapacity && zaBw <= zaCapacity) {
                    fits = true;
                } else if (azBw <= zaCapacity && zaBw <= azCapacity) {
                    fits = true;
                }
                if (fits) {
                    log.info("adding "+adjcy.asLogString());
                    pruned.add(adjcy);
                }
            }
        }

        return pruned;
    }

}
