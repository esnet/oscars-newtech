package net.es.oscars.web.rest;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.enums.BwDirection;
import net.es.oscars.resv.svc.ResvLibrary;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.NextHop;
import net.es.oscars.topo.beans.PortBwVlan;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.enums.Layer;
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


    @RequestMapping(value = "/api/topo/reservables", method = RequestMethod.GET)
    @ResponseBody
    public  Map<String, PortBwVlan> reservables() {
        log.info("urns in topology: "+topoService.getTopoUrnMap().entrySet().size());

        // grab everything available, no matter the reservations
        Map<String, Set<IntRange>> availableVlanMap = ResvLibrary.availableVlanMap(topoService.getTopoUrnMap(), new HashSet<>());
        Map<String, Integer> availableIngressBw = ResvLibrary.availableBandwidthMap(BwDirection.INGRESS, topoService.getTopoUrnMap(), new HashMap<>());
        Map<String, Integer> availableEgressBw = ResvLibrary.availableBandwidthMap(BwDirection.EGRESS, topoService.getTopoUrnMap(), new HashMap<>());

        Map<String, PortBwVlan> reservables = new HashMap<>();
        topoService.getTopoUrnMap().forEach((urn, topoUrn) -> {
            if (topoUrn.getUrnType().equals(UrnType.PORT) && topoUrn.getCapabilities().contains(Layer.ETHERNET)) {
                Integer ingBw = availableIngressBw.get(urn);
                Integer egBw = availableEgressBw.get(urn);
                Set<IntRange> intRanges = availableVlanMap.get(urn);
                String vlanExpr = IntRange.asString(intRanges);

                PortBwVlan pbw = PortBwVlan.builder()
                        .vlanRanges(intRanges)
                        .ingressBandwidth(ingBw)
                        .egressBandwidth(egBw)
                        .vlanExpression(vlanExpr)
                        .build();
                reservables.put(urn, pbw);
            }

        });
        return reservables;
    }




}