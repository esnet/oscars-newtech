package net.es.oscars.web.rest;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.NextHop;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoLibrary;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
public class TopoController {

    @Autowired
    private TopoService topoService;

    @Autowired
    private PortAdjcyRepository portAdjcyRepository;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }

    @RequestMapping(value = "/api/topo/nextHopsForEro", method = RequestMethod.POST)
    @ResponseBody
    public List<NextHop> nextHopsForEro(@RequestBody List<String> ero) {
        List<NextHop> nextHops = new ArrayList<>();
        Set<Device> devicesCrossed = new HashSet<>();
        List<PortAdjcy> portAdjcies = portAdjcyRepository.findAll();

        String lastDevice = "";
        for (String urn : ero) {
            if (!topoService.getTopoUrnMap().containsKey(urn)) {
                throw new NoSuchElementException("URN " + urn + " not found in topology");
            }
            TopoUrn topoUrn = topoService.getTopoUrnMap().get(urn);
            devicesCrossed.add(topoUrn.getDevice());
            lastDevice = urn;
        }


        TopoUrn topoUrn = topoService.getTopoUrnMap().get(lastDevice);
        if (!topoUrn.getUrnType().equals(UrnType.DEVICE)) {
            throw new NoSuchElementException("Last URN in ERO must be a device");

        } else {

            topoUrn.getDevice().getPorts().forEach(p -> {

                String portUrn = p.getUrn();
                TopoLibrary.adjciesOriginatingFrom(portUrn, portAdjcies).forEach(adj -> {
                    if (!devicesCrossed.contains(adj.getZ().getDevice())) {
                        NextHop nh = NextHop.builder()
                                .urn(p.getUrn())
                                .to(adj.getZ().getDevice().getUrn())
                                .through(adj.getZ().getUrn())
                                .build();
                        nextHops.add(nh);
                    }
                });

            });
        }

        return nextHops;

    }

}