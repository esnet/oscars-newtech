package net.es.oscars.resv.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.db.BlueprintRepository;
import net.es.oscars.resv.db.FixtureRepository;
import net.es.oscars.resv.db.ScheduleRepository;
import net.es.oscars.resv.db.VlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.ReservableCommandParam;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.svc.TopoService;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class ResvService {
    @Autowired
    TopoService topoService;
    @Autowired
    BlueprintRepository blueprintRepo;

    @Autowired
    VlanRepository vlanRepo;

    @Autowired
    FixtureRepository fixtureRepo;

    @Autowired
    ScheduleRepository scheduleRepo;


    public static String randomHashid() {
        Random rand = new Random();
        Integer randInt = rand.nextInt();
        randInt = randInt < 0 ? -1 * randInt : randInt;

        Hashids hashids = new Hashids("oscars");
        return hashids.encode(randInt);
    }

    public Blueprint designToHeld(Blueprint design) {
        return new Blueprint();

    }



    public static List<Schedule> schedulesOverlapping(List<Schedule> all, Instant b, Instant e) {
        List<Schedule> overlapping = new ArrayList<>();
        for (Schedule s : all) {
            if (s.getPhase().equals(Phase.HELD) || s.getPhase().equals(Phase.RESERVED)) {
                if (s.overlaps(b, e)) {
                    overlapping.add(s);
                }

            }
        }
        return overlapping;
    }


    public static Map<String, Set<IntRange>> availableVlanMap(Map<String, TopoUrn> baseline, Collection<Vlan> reservedVlans) {

        Map<String, Set<Integer>> reservedVlanMap = new HashMap<>();
        reservedVlans.forEach(v -> {
            if (!reservedVlanMap.keySet().contains(v.getUrn())) {
                reservedVlanMap.put(v.getUrn(), new HashSet<>());
            }
            reservedVlanMap.get(v.getUrn()).add(v.getVlan());
        });

        Map<String, Set<IntRange>> availableVlanMap = new HashMap<>();
        for (String urn: baseline.keySet()) {
            Set<IntRange> reservable = baseline.get(urn).getReservableVlans();
            Set<IntRange> availableVlans = availableInts(reservable, reservedVlanMap.get(urn));
            availableVlanMap.put(urn, availableVlans);
        }
        return availableVlanMap;
    }

    public static Set<IntRange> availableInts(Set<IntRange> reservable, Set<Integer> reserved) {
        Set<Integer> available = new HashSet<>();
        for (IntRange range : reservable) {
            available.addAll(range.asSet());
        }
        available.removeAll(reserved);
        return IntRange.fromSet(available);
    }

    public static Map<String, TopoUrn> constructAvailabilityMap(
            Map<String, TopoUrn> urnMap,
            Map<String, Set<IntRange>> availVlanMap,
            Map<String, Integer> availIngressBwMap,
            Map<String, Integer> availEgressBwMap,
            Map<String, Set<ReservableCommandParam>> availCommandParamMap) {

        Map<String, TopoUrn> result = new HashMap<>();

        for (Map.Entry<String, TopoUrn> e : urnMap.entrySet()) {
            TopoUrn u = e.getValue();

            HashSet<Layer> capabilities = new HashSet<>();
            capabilities.addAll(u.getCapabilities());

            TopoUrn copy = TopoUrn.builder()
                    .capabilities(capabilities)
                    .device(u.getDevice())
                    .port(u.getPort())
                    .reservableVlans(availVlanMap.get(u.getUrn()))
                    .reservableEgressBw(availEgressBwMap.get(u.getUrn()))
                    .reservableIngressBw(availIngressBwMap.get(u.getUrn()))
                    .reservableCommandParams(availCommandParamMap.get(u.getUrn()))
                    .build();

            result.put(e.getKey(), copy);
        }
        return result;

    }
    public static Integer availBandwidth(Integer reservableBw, List<PeriodBandwidth> periodBandwidths, Instant when) {
        Integer result = reservableBw;
        for (PeriodBandwidth pbw: periodBandwidths) {
            if (pbw.getBeginning().isBefore(when) && pbw.getEnding().isAfter(when)) {
                result -= pbw.getBandwidth();
            }
        }

        return result;
    }
    public static Integer overallAvailBandwidth(Integer reservableBw, List<PeriodBandwidth> periodBandwidths) {
        Map<Instant, Set<Integer>> timeline = new HashMap<>();
        for (PeriodBandwidth pbw: periodBandwidths) {
            if (!timeline.containsKey(pbw.getBeginning())) {
                timeline.put(pbw.getBeginning(), new HashSet<>());
            }
            if (!timeline.containsKey(pbw.getEnding())) {
                timeline.put(pbw.getEnding(), new HashSet<>());
            }
            timeline.get(pbw.getBeginning()).add(pbw.getBandwidth());
            timeline.get(pbw.getEnding()).add(-1 * pbw.getBandwidth());
        }
        List<Instant> instants = new ArrayList<>(timeline.keySet());
        instants.sort(Comparator.comparing(Instant::getEpochSecond));

        Integer maxReserved = 0;

        Integer runningReserved = 0;
        for (Instant inst : instants) {
            Set<Integer> deltas = timeline.get(inst);
            for (Integer d : deltas) {
                runningReserved += d;
            }
            if (runningReserved > maxReserved) {
                maxReserved = runningReserved;
            }
        }
        return reservableBw - maxReserved;
    }


}
