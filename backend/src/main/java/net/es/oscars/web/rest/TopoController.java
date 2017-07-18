package net.es.oscars.web.rest;


import net.es.oscars.topo.beans.NextHop;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoLibrary;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class TopoController {

    @Autowired
    private TopoService topoService;

    @Autowired
    private PortAdjcyRepository portAdjcyRepository;

    @RequestMapping(value = "/api/topo/nextHopsFrom/{urn}", method = RequestMethod.GET)
    @ResponseBody
    public List<NextHop> nextHopsFrom(@PathVariable String urn) {
        List<NextHop> nextHops = new ArrayList<>();

        if (!topoService.getTopoUrnMap().containsKey(urn)) {
            return nextHops;
        }
        List<PortAdjcy> portAdjcies = portAdjcyRepository.findAll();

        TopoUrn topoUrn = topoService.getTopoUrnMap().get(urn);
        if (topoUrn.getUrnType().equals(UrnType.DEVICE)) {
            topoUrn.getDevice().getPorts().forEach(p-> {

                String portUrn = p.getUrn();
                TopoLibrary.adjciesOriginatingFrom(portUrn, portAdjcies).forEach(adj -> {
                    NextHop nh = NextHop.builder()
                            .urn(p.getUrn())
                            .to(adj.getZ().getDevice().getUrn())
                            .build();
                    nextHops.add(nh);
                });

            });

        } else {
            TopoLibrary.adjciesOriginatingFrom(urn, portAdjcies).forEach(adj -> {
                NextHop nh = NextHop.builder()
                        .urn(adj.getZ().getUrn())
                        .to(adj.getZ().getDevice().getUrn())
                        .build();
                nextHops.add(nh);
            });
        }

        return nextHops;

    }

}