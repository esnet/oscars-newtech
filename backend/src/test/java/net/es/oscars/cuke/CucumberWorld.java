package net.es.oscars.cuke;

import net.es.oscars.resv.beans.PeriodBandwidth;
import net.es.oscars.resv.ent.Vlan;
import net.es.oscars.resv.enums.BwDirection;
import net.es.oscars.topo.beans.TopoUrn;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CucumberWorld {
    private List<Exception> exceptions = new ArrayList<>();
    private boolean expectException;

    Map<BwDirection, Map<String, List<PeriodBandwidth>>> bwMaps = new HashMap<>();
    Map<BwDirection, Map<String, Integer>> bwBaseline = new HashMap<>();

    List<Vlan> reservedVlans;
    Map<String, TopoUrn> vlanBaseline ;


    public void expectException() {
        expectException = true;
    }

    public void add(Exception e) throws RuntimeException {
        if (!expectException) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        exceptions.add(e);
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }


}
