package net.es.oscars.web.rest;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.beans.PeriodBandwidth;
import net.es.oscars.resv.db.FixtureRepository;
import net.es.oscars.resv.db.ScheduleRepository;
import net.es.oscars.resv.db.VlanRepository;
import net.es.oscars.resv.ent.Schedule;
import net.es.oscars.resv.ent.Vlan;
import net.es.oscars.resv.ent.VlanFixture;
import net.es.oscars.resv.enums.BwDirection;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.svc.ResvLibrary;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.PortBwVlan;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.Interval;
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
    private VlanRepository vlanRepo;

    @Autowired
    private FixtureRepository fixtureRepo;

    @Autowired
    private ScheduleRepository scheduleRepo;

    @Autowired
    private DeviceRepository deviceRepo;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn("requested an item which did not exist", ex);
    }


    private Map<String, PortBwVlan> portBwVlans(Collection<Vlan> reservedVlans,
                                              Map<String, List<PeriodBandwidth>> reservedIngBws,
                                              Map<String, List<PeriodBandwidth>> reservedEgBws) {
        Map<String, Set<IntRange>> availableVlanMap = ResvLibrary.availableVlanMap(topoService.getTopoUrnMap(), reservedVlans);
        Map<String, Integer> availableIngressBw = ResvLibrary.availableBandwidthMap(BwDirection.INGRESS, topoService.getTopoUrnMap(), reservedIngBws);
        Map<String, Integer> availableEgressBw = ResvLibrary.availableBandwidthMap(BwDirection.EGRESS, topoService.getTopoUrnMap(), reservedEgBws);

        Map<String, PortBwVlan> available = new HashMap<>();
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
                available.put(urn, pbw);
            }

        });
        return available;
    }


    @RequestMapping(value = "/api/topo/ethernetPortsByDevice", method = RequestMethod.GET)
    @ResponseBody
    public  Map<String, List<String>> ethernetPortsByDevice() {
        Map<String, List<String>> result = new HashMap<>();
        deviceRepo.findAll().forEach(d -> {
            List<String> ports = new ArrayList<>();
            d.getPorts().forEach(p -> {
                if (p.getCapabilities().contains(Layer.ETHERNET)) {
                    ports.add(p.getUrn());
                }
            });
            result.put(d.getUrn(), ports);

        });
        return result;
    }


    @RequestMapping(value = "/api/topo/baseline", method = RequestMethod.GET)
    @ResponseBody
    public  Map<String, PortBwVlan> baseline() {

        // grab everything available
        return this.portBwVlans(new HashSet<>(), new HashMap<>(), new HashMap<>());

    }



    @RequestMapping(value = "/api/topo/available", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, PortBwVlan> available(@RequestBody Interval interval) {

        HashSet<Vlan> reservedVlans = new HashSet<>();
        Map<String, List<PeriodBandwidth>> reservedIngBws = new HashMap<>();
        Map<String, List<PeriodBandwidth>> reservedEgBws = new HashMap<>();

        List<Schedule> scheds = scheduleRepo.findOverlapping(interval.getBeginning(), interval.getEnding());
        for (Schedule sch: scheds) {
            if (sch.getPhase().equals(Phase.HELD) || sch.getPhase().equals(Phase.RESERVED)) {
                List<Vlan> vlans = vlanRepo.findBySchedule(sch);
                List<VlanFixture> fixtures = fixtureRepo.findBySchedule(sch);
                reservedVlans.addAll(vlans);
                for (VlanFixture f: fixtures) {
                    String urn = f.getPortUrn();
                    PeriodBandwidth iPbw = PeriodBandwidth.builder()
                            .bandwidth(f.getIngressBandwidth())
                            .beginning(sch.getBeginning())
                            .ending(sch.getEnding())
                            .build();
                    PeriodBandwidth ePbw = PeriodBandwidth.builder()
                            .bandwidth(f.getEgressBandwidth())
                            .beginning(sch.getBeginning())
                            .ending(sch.getEnding())
                            .build();
                    if (!reservedIngBws.containsKey(urn)) {
                        reservedIngBws.put(urn, new ArrayList<>());
                    }
                    if (!reservedEgBws.containsKey(urn)) {
                        reservedEgBws.put(urn, new ArrayList<>());
                    }
                    reservedIngBws.get(urn).add(iPbw);
                    reservedEgBws.get(urn).add(ePbw);
                }
            }
        }


        return this.portBwVlans(reservedVlans, reservedIngBws, reservedEgBws);
    }


}