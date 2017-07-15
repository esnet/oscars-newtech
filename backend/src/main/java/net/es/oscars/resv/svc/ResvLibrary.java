package net.es.oscars.resv.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.beans.PeriodBandwidth;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.BwDirection;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.ReservableCommandParam;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;

import java.time.Instant;
import java.util.*;

@Slf4j
public class ResvLibrary {


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

    public static List<PeriodBandwidth> pbwsOverlapping(List<PeriodBandwidth> candidates, Instant b, Instant e) {
        List<PeriodBandwidth> result = new ArrayList<>();
        for (PeriodBandwidth pbw : candidates) {
            log.info("evaluating a pbw: "+pbw.toString());
            boolean add = true;
            if (pbw.getEnding().isBefore(b) || pbw.getBeginning().isAfter(e)) {
                add = false;
            }
            if (add) {
                log.info("adding a pbw: "+pbw.toString());
                result.add(pbw);
            }
        }

        return result;
    }


    public static Map<String, Integer> availableBandwidthMap(BwDirection dir, Map<String, TopoUrn> baseline,
                                                          Map<String, List<PeriodBandwidth>> reservedBandwidths) {
        Map<String, Integer> result = new HashMap<>();
        for (String urn: baseline.keySet()) {
            if (baseline.get(urn).getUrnType().equals(UrnType.PORT)) {
                Integer reservable = 0;
                switch (dir) {
                    case INGRESS:
                        reservable = baseline.get(urn).getReservableIngressBw();
                        break;
                    case EGRESS:
                        reservable = baseline.get(urn).getReservableEgressBw();
                        break;
                }
                Integer availableBw = overallAvailBandwidth(reservable, reservedBandwidths.get(urn));
                result.put(urn, availableBw);
            }
        }
        return result;

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

    public static Map<String, TopoUrn> constructAvailabilityMap (
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


    public static Integer availBandwidth(Integer reservableBw,
                                         Collection<PeriodBandwidth> periodBandwidths,
                                         Instant when) {
        Integer result = reservableBw;
        for (PeriodBandwidth pbw: periodBandwidths) {
            if (pbw.getBeginning().isBefore(when) && pbw.getEnding().isAfter(when)) {
                result -= pbw.getBandwidth();
            }
        }

        return result;
    }
    public static Integer overallAvailBandwidth(Integer reservableBw,
                                                Collection<PeriodBandwidth> periodBandwidths) {
        Map<Instant, Set<Integer>> timeline = new HashMap<>();
        if (periodBandwidths == null) {
            periodBandwidths = new ArrayList<>();
        }
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
