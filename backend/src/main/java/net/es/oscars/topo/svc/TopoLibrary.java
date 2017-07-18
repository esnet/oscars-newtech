package net.es.oscars.topo.svc;

import net.es.oscars.topo.beans.TopoAdjcy;
import net.es.oscars.topo.ent.PortAdjcy;

import java.util.ArrayList;
import java.util.List;

public class TopoLibrary {

    public static List<PortAdjcy> adjciesOriginatingFrom(String urn, List<PortAdjcy> allAdjcies) {
        List<PortAdjcy> result = new ArrayList<>();
        allAdjcies.forEach(adj -> {
            if (adj.getA().getUrn().equals(urn)) {
                result.add(adj);
            }
        });

        return result;
    }

}
