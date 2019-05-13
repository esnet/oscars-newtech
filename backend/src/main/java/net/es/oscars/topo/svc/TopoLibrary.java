package net.es.oscars.topo.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.ent.Adjcy;

import java.util.*;

@Slf4j
public class TopoLibrary {

    public static List<Adjcy> adjciesOriginatingFrom(String urn, List<Adjcy> allAdjcies) {
        List<Adjcy> result = new ArrayList<>();
        allAdjcies.forEach(adj -> {
            if (adj.getA().getUrn().equals(urn)) {
                result.add(adj);
            }
        });

        return result;
    }

}
