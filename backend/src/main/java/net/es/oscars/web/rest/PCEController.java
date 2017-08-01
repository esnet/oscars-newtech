package net.es.oscars.web.rest;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PCEException;
import net.es.oscars.pce.PalindromicalPCE;
import net.es.oscars.pce.PceService;
import net.es.oscars.resv.beans.PeriodBandwidth;
import net.es.oscars.resv.db.FixtureRepository;
import net.es.oscars.resv.db.ScheduleRepository;
import net.es.oscars.resv.db.VlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.BwDirection;
import net.es.oscars.resv.enums.EroDirection;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.svc.ResvLibrary;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.NextHop;
import net.es.oscars.topo.beans.PortBwVlan;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoLibrary;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.Interval;
import net.es.oscars.web.beans.PceRequest;
import net.es.oscars.web.beans.PceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Slf4j
public class PCEController {

    @Autowired
    private TopoService topoService;


    @Autowired
    private PortAdjcyRepository portAdjcyRepository;

    @Autowired
    private PceService pceService;

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }

    @RequestMapping(value = "/api/pce/nextHopsForEro", method = RequestMethod.POST)
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


    @RequestMapping(value = "/api/pce/paths", method = RequestMethod.POST)
    @ResponseBody
    public PceResponse paths(@RequestBody PceRequest request) throws PCEException {
        return pceService.calculatePaths(request);

    }






}