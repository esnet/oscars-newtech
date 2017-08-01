package net.es.oscars.resv.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.beans.PeriodBandwidth;
import net.es.oscars.resv.db.FixtureRepository;
import net.es.oscars.resv.db.PipeRepository;
import net.es.oscars.resv.db.ScheduleRepository;
import net.es.oscars.resv.db.VlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.BwDirection;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.topo.beans.*;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
@Slf4j
@Data
public class ResvService {
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private ScheduleRepository scheduleRepo;
    @Autowired
    private VlanRepository vlanRepo;

    @Autowired
    private FixtureRepository fixtureRepo;
    @Autowired
    private PipeRepository pipeRepo;

    @Autowired
    private TopoService topoService;

    public Map<String, List<PeriodBandwidth>> reservedIngBws(Interval interval) {
        List<Schedule> scheds = scheduleRepo.findOverlapping(interval.getBeginning(), interval.getEnding());
        Map<String, List<PeriodBandwidth>> reservedIngBws = new HashMap<>();


        for (Schedule sch : scheds) {
            if (sch.getPhase().equals(Phase.HELD) || sch.getPhase().equals(Phase.RESERVED)) {
                List<VlanFixture> fixtures = fixtureRepo.findBySchedule(sch);
                List<VlanPipe> pipes = pipeRepo.findBySchedule(sch);
                for (VlanFixture f : fixtures) {
                    String urn = f.getPortUrn();
                    PeriodBandwidth iPbw = PeriodBandwidth.builder()
                            .bandwidth(f.getIngressBandwidth())
                            .beginning(sch.getBeginning())
                            .ending(sch.getEnding())
                            .build();
                    addTo(reservedIngBws, urn, iPbw);
                }

                for (VlanPipe pipe : pipes) {
                    // hops go:
                    // device, outPort, inPort, device, outPort, inPort, device
                    // hops will always be empty, or 1 modulo 3
                    // bandwidth gets applied per direction i.e.
                    // az as egress on outPort, as ingress on inPort
                    if (pipe.getAzERO() != null) {

                        for (int i = 0; i < pipe.getAzERO().size(); i++) {
                            EroHop hop = pipe.getAzERO().get(i);
                            String urn = hop.getUrn();
                            PeriodBandwidth pbw = PeriodBandwidth.builder()
                                    .bandwidth(pipe.getAzBandwidth())
                                    .beginning(sch.getBeginning())
                                    .ending(sch.getEnding())
                                    .build();

                            if (i % 3 == 2) {
                                addTo(reservedIngBws, urn, pbw);
                            }
                        }
                    }

                    if (pipe.getZaERO() != null ) {

                        for (int i = 0; i < pipe.getZaERO().size(); i++) {
                            EroHop hop = pipe.getZaERO().get(i);
                            String urn = hop.getUrn();

                            PeriodBandwidth pbw = PeriodBandwidth.builder()
                                    .bandwidth(pipe.getZaBandwidth())
                                    .beginning(sch.getBeginning())
                                    .ending(sch.getEnding())
                                    .build();

                            if (i % 3 == 2) {
                                addTo(reservedIngBws, urn, pbw);
                            }
                        }
                    }
                }
            }
        }
        return reservedIngBws;
    }

    public Map<String, List<PeriodBandwidth>> reservedEgBws(Interval interval) {
        List<Schedule> scheds = scheduleRepo.findOverlapping(interval.getBeginning(), interval.getEnding());
        Map<String, TopoUrn> urnMap = topoService.getTopoUrnMap();
        Map<String, List<PeriodBandwidth>> reservedEgBws = new HashMap<>();

        for (Schedule sch : scheds) {
            if (sch.getPhase().equals(Phase.HELD) || sch.getPhase().equals(Phase.RESERVED)) {
                List<VlanFixture> fixtures = fixtureRepo.findBySchedule(sch);
                List<VlanPipe> pipes = pipeRepo.findBySchedule(sch);

                for (VlanFixture f : fixtures) {
                    String urn = f.getPortUrn();
                    PeriodBandwidth ePbw = PeriodBandwidth.builder()
                            .bandwidth(f.getEgressBandwidth())
                            .beginning(sch.getBeginning())
                            .ending(sch.getEnding())
                            .build();

                    addTo(reservedEgBws, urn, ePbw);
                }

                for (VlanPipe pipe : pipes) {
                    // hops go:
                    // device, outPort, inPort, device, outPort, inPort, device
                    // hops will always be empty, or 1 modulo 3
                    // bandwidth gets applied per direction i.e.
                    // az as egress on outPort, as ingress on inPort
                    if (pipe.getAzERO() != null) {

                        for (int i = 0; i < pipe.getAzERO().size(); i++) {

                            EroHop hop = pipe.getAzERO().get(i);
                            String urn = hop.getUrn();
                            PeriodBandwidth pbw = PeriodBandwidth.builder()
                                    .bandwidth(pipe.getAzBandwidth())
                                    .beginning(sch.getBeginning())
                                    .ending(sch.getEnding())
                                    .build();

                            if (i % 3 == 1) {
                                addTo(reservedEgBws, urn, pbw);
                            }
                        }
                    }
                    if (pipe.getZaERO() != null) {
                        for (int i = 0; i < pipe.getZaERO().size(); i++) {
                            EroHop hop = pipe.getZaERO().get(i);
                            String urn = hop.getUrn();

                            PeriodBandwidth pbw = PeriodBandwidth.builder()
                                    .bandwidth(pipe.getZaBandwidth())
                                    .beginning(sch.getBeginning())
                                    .ending(sch.getEnding())
                                    .build();

                            if (i % 3 == 1) {

                                addTo(reservedEgBws, urn, pbw);
                            }
                        }
                    }
                }
            }
        }
        return reservedEgBws;
    }

    public Collection<Vlan> reservedVlans(Interval interval) {
        List<Schedule> scheds = scheduleRepo.findOverlapping(interval.getBeginning(), interval.getEnding());
        HashSet<Vlan> reservedVlans = new HashSet<>();

        for (Schedule sch : scheds) {
            if (sch.getPhase().equals(Phase.HELD) || sch.getPhase().equals(Phase.RESERVED)) {

                List<Vlan> vlans = vlanRepo.findBySchedule(sch);
                reservedVlans.addAll(vlans);

            }
        }
        return reservedVlans;
    }

    public Map<String, Integer> availableIngBws(Interval interval) {
        Map<String, List<PeriodBandwidth>> reservedIngBws = reservedIngBws(interval);

        /*
        try {
            ObjectMapper mapper = builder.build();
            log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(reservedIngBws));
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage());
        }
        */




        Map<String, TopoUrn> baseline = topoService.getTopoUrnMap();
        return ResvLibrary.availableBandwidthMap(BwDirection.INGRESS, baseline, reservedIngBws);

    }

    public Map<String, Integer> availableEgBws(Interval interval) {
        Map<String, List<PeriodBandwidth>> reservedEgBws = reservedEgBws(interval);
        /*
        try {
            ObjectMapper mapper = builder.build();
            log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(reservedEgBws));
        } catch (JsonProcessingException ex) {
            log.error(ex.getMessage());
        }
        */

        Map<String, TopoUrn> baseline = topoService.getTopoUrnMap();
        return ResvLibrary.availableBandwidthMap(BwDirection.EGRESS, baseline, reservedEgBws);
    }

    public Map<String, PortBwVlan> available(Interval interval) {
        Collection<Vlan> reservedVlans = reservedVlans(interval);
        Map<String, List<PeriodBandwidth>> reservedEgBws = reservedEgBws(interval);
        Map<String, List<PeriodBandwidth>> reservedIngBws = reservedIngBws(interval);

        return ResvLibrary.portBwVlans(topoService.getTopoUrnMap(), reservedVlans, reservedIngBws, reservedEgBws);
    }

    static private void addTo(Map<String, List<PeriodBandwidth>> bwMap, String urn, PeriodBandwidth pbw) {
        if (!bwMap.containsKey(urn)) {
            bwMap.put(urn, new ArrayList<>());
        }
        bwMap.get(urn).add(pbw);
    }


}
