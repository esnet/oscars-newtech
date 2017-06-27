package net.es.oscars.bwavail;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.bwavail.svc.BandwidthAvailabilityService;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.pce.helpers.RepoEntityBuilder;
import net.es.oscars.pce.helpers.TopologyBuilder;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Transactional

public class BandwidthAvailabilityServiceTest extends AbstractCoreTest {

    @Autowired
    private BandwidthAvailabilityService bwAvailService;

    @Autowired
    private TopologyBuilder topologyBuilder;

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private ReservedBandwidthRepository reservedBandwidthRepo;
    
    @Autowired
    private RepoEntityBuilder repoEntityBuilder;

    private final String az1 = "Az1";
    private final String za1 = "Za1";
    private final String az2 = "Az2";
    private final String za2 = "Za2";

    @Test
    public void noReservationsTest(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = new ArrayList<>();
        List<Instant> reservedStartTimes = new ArrayList<>();
        List<Instant> reservedEndTimes = new ArrayList<>();
        List<Integer> inBandwidths = new ArrayList<>();
        List<Integer> egBandwidths = new ArrayList<>();

        Integer expectedMinAzBw = 1000;
        Integer expectedMinZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);


        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void noReservationsTestJunction(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = new ArrayList<>();
        List<Instant> reservedStartTimes = new ArrayList<>();
        List<Instant> reservedEndTimes = new ArrayList<>();
        List<Integer> inBandwidths = new ArrayList<>();
        List<Integer> egBandwidths = new ArrayList<>();

        Integer expectedMinAzBw = 1000;
        Integer expectedMinZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);


        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequestJunction(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void noReservationsTestJunctionPath(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = new ArrayList<>();
        List<Instant> reservedStartTimes = new ArrayList<>();
        List<Instant> reservedEndTimes = new ArrayList<>();
        List<Integer> inBandwidths = new ArrayList<>();
        List<Integer> egBandwidths = new ArrayList<>();

        Integer expectedMinAzBw = 1000;
        Integer expectedMinZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);


        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequestJunctionPath(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsOffPathTest(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeM:1", "nodeL:1");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 1000;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 1000;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsStartBeforeEndAfterTest(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "portA");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(-1L, ChronoUnit.HOURS),
                now.plus(-1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(8L, ChronoUnit.HOURS),
                now.plus(10L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 800;
        Integer expectedMaxAzBw = 800;
        Integer expectedMinZaBw = 800;
        Integer expectedMaxZaBw = 800;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 800);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 800);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 800);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 800);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);

    }

    @Test
    public void reservationsStartBeforeEndDuring(){
        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.minus(100L, ChronoUnit.DAYS),
                now.minus(100L, ChronoUnit.DAYS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 900;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMinAzBw);
        azGoalMap.put(reservedEndTimes.get(0), expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMinZaBw);
        zaGoalMap.put(reservedEndTimes.get(0), expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);

    }

    @Test
    public void reservationsStartDuringEndDuring(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 900;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(reservedStartTimes.get(0), expectedMinAzBw);
        azGoalMap.put(reservedEndTimes.get(0), expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(reservedStartTimes.get(0), expectedMinZaBw);
        zaGoalMap.put(reservedEndTimes.get(0), expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsStartDuringEndDuringDifferentPipeAndJunctionBw(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeK:3", "nodeW:1", "nodeW:2", "nodeQ:3", "portZ");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 500, 500, 500, 500, 200);
        List<Integer> egBandwidths = Arrays.asList(100, 500, 500, 500, 500, 200);

        Integer expectedMinAzBw = 500;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 500;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(reservedStartTimes.get(0), expectedMinAzBw);
        azGoalMap.put(reservedEndTimes.get(0), expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(reservedStartTimes.get(0), expectedMinZaBw);
        zaGoalMap.put(reservedEndTimes.get(0), expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }


    @Test
    public void reservationsStartDuringEndAfter(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(7L, ChronoUnit.HOURS),
                now.plus(7L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 900;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(reservedStartTimes.get(0), expectedMinAzBw);
        azGoalMap.put(requestEndTime, expectedMinAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(reservedStartTimes.get(0), expectedMinZaBw);
        zaGoalMap.put(requestEndTime, expectedMinZaBw);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsStartAfterEndAfter(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(6L, ChronoUnit.HOURS),
                now.plus(6L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(8L, ChronoUnit.HOURS),
                now.plus(8L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 1000;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 1000;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsStartBeforeEndAtStart(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(0L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 1000;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 1000;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsStartBeforeEndBefore(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(-1L, ChronoUnit.HOURS),
                now.plus(-1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(0L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100);

        Integer expectedMinAzBw = 1000;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 1000;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(requestStartTime, expectedMaxAzBw);
        azGoalMap.put(requestEndTime, expectedMaxAzBw);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(requestStartTime, expectedMaxZaBw);
        zaGoalMap.put(requestEndTime, expectedMaxZaBw);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsMix(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "portA", "portA", "portA", "portA");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(-1L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(9L, ChronoUnit.HOURS),
                now.plus(-1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(4L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(8L, ChronoUnit.HOURS),
                now.plus(10L, ChronoUnit.HOURS),
                now.plus(0L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100, 100, 100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100, 100, 100, 100);

        Integer expectedMinAzBw = 700;
        Integer expectedMaxAzBw = 900;
        Integer expectedMinZaBw = 700;
        Integer expectedMaxZaBw = 900;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 700);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 800);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 700);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 800);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsThreeWayOverlapDifferentNodes(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("portA", "nodeK:3", "nodeQ:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(6L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100, 100);

        Integer expectedMinAzBw = 900;
        Integer expectedMaxAzBw = 900;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 900;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsThreeWayOverlapSameNode(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeK:3", "nodeK:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(6L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 100, 100);
        List<Integer> egBandwidths = Arrays.asList(100, 100, 100);

        Integer expectedMinAzBw = 800;
        Integer expectedMaxAzBw = 900;
        Integer expectedMinZaBw = 800;
        Integer expectedMaxZaBw = 900;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 800);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 800);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 800);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 800);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 900);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsDifferentBandwidthValuesSameNode(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeK:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 500);
        List<Integer> egBandwidths = Arrays.asList(100, 500);

        Integer expectedMinAzBw = 400;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 400;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 400);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 400);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsDifferentBandwidthValuesDifferentNodes(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeW:2");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 500);
        List<Integer> egBandwidths = Arrays.asList(100, 500);

        Integer expectedMinAzBw = 500;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 500;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsDifferentBandwidthValuesSameNodeDifferentTimes(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeK:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 500);
        List<Integer> egBandwidths = Arrays.asList(100, 500);

        Integer expectedMinAzBw = 500;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 500;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsDifferentInEgBandwidthValuesSameNodeDifferentTimes(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeK:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 25);
        List<Integer> egBandwidths = Arrays.asList(50, 3);

        Integer expectedMinAzBw = 950;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 950);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 997);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 975);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsDifferentInEgBandwidthValuesDifferentNodesDifferentTimes(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeQ:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(4L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 25);
        List<Integer> egBandwidths = Arrays.asList(50, 3);

        Integer expectedMinAzBw = 950;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 950);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 975);
        azGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 997);
        zaGoalMap.put(now.plus(4L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsDifferentInEgBandwidthValuesDifferentNodesSameTime(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeQ:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 25);
        List<Integer> egBandwidths = Arrays.asList(50, 3);

        Integer expectedMinAzBw = 950;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 900;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 950);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 950);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 975);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 997);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void reservationsDifferentInEgBandwidthValuesSameNodeSameTime(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeK:3", "nodeK:3");
        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(0L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(100, 25);
        List<Integer> egBandwidths = Arrays.asList(50, 3);

        Integer expectedMinAzBw = 947;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 875;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 950);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 947);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 997);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 900);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 875);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 975);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void noPathStartNodeFullyReserved(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Collections.singletonList("portA");
        List<Instant> reservedStartTimes = Collections.singletonList(
                now.plus(1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Collections.singletonList(
                now.plus(5L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Collections.singletonList(1000);
        List<Integer> egBandwidths = Collections.singletonList(1000);

        Integer expectedMinAzBw = 0;
        Integer expectedMaxAzBw = 0;
        Integer expectedMinZaBw = 0;
        Integer expectedMaxZaBw = 0;

        // Path should just be [Source, Dest]
        List<String> path = Arrays.asList("nodeK", "nodeQ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 0);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 0);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 0);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 0);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void noPathIntermediateNodesFullyReserved(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeW:1", "nodeX:1", "nodeM:3", "nodeL:3");

        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(1000, 1000, 1000, 1000);
        List<Integer> egBandwidths = Arrays.asList(1000, 1000, 1000, 1000);

        Integer expectedMinAzBw = 0;
        Integer expectedMaxAzBw = 0;
        Integer expectedMinZaBw = 0;
        Integer expectedMaxZaBw = 0;

        List<String> path = Arrays.asList("nodeK", "nodeQ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 0);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 0);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 0);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 0);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void onePathAvailable(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeW:1", "nodeX:1", "nodeL:3", "nodeM:3");

        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(1000, 1000, 1000, 500);
        List<Integer> egBandwidths = Arrays.asList(1000, 1000, 1000, 500);

        Integer expectedMinAzBw = 500;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 500;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:2", "nodeM:1", "nodeM", "nodeM:3", "nodeR:1", "nodeR", "nodeR:3", "nodeQ:2", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void specifiedEro(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeW:1", "nodeX:1", "nodeL:3", "nodeM:3");

        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS),
                now.plus(2L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS),
                now.plus(3L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(1000, 1000, 1000, 500);
        List<Integer> egBandwidths = Arrays.asList(1000, 1000, 1000, 500);

        Integer expectedMinAzBw = 500;
        Integer expectedMaxAzBw = 1000;
        Integer expectedMinZaBw = 500;
        Integer expectedMaxZaBw = 1000;

        List<String> path = Arrays.asList("portA", "nodeK", "nodeK:2", "nodeM:1", "nodeM", "nodeM:3", "nodeR:1", "nodeR", "nodeR:3", "nodeQ:2", "nodeQ", "portZ");
        List<String> revPath = new ArrayList<>(path);
        Collections.reverse(revPath);

        List<List<String>> azEros = new ArrayList<>();
        List<List<String>> zaEros = new ArrayList<>();

        azEros.add(path);
        zaEros.add(revPath);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap = new HashMap<>();
        azGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 500);
        azGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 1000);
        azGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap = new HashMap<>();
        zaGoalMap.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(2L, ChronoUnit.HOURS), 500);
        zaGoalMap.put(now.plus(3L, ChronoUnit.HOURS), 1000);
        zaGoalMap.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw);
        minExpectedBwMap.put(za1, expectedMinZaBw);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap);
        expectedBwMap.put(za1, zaGoalMap);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime, azEros, zaEros);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    @Test
    public void twoSpecifiedEros(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeM:3", "nodeW:1");

        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(500, 700);
        List<Integer> egBandwidths = Arrays.asList(500, 700);

        Integer expectedMinAzBw1 = 500;
        Integer expectedMinZaBw1 = 500;
        Integer expectedMinAzBw2 = 300;
        Integer expectedMinZaBw2 = 300;

        List<String> path1 = Arrays.asList("portA", "nodeK", "nodeK:2", "nodeM:1", "nodeM", "nodeM:3", "nodeR:1", "nodeR", "nodeR:3", "nodeQ:2", "nodeQ", "portZ");
        List<String> revPath1 = new ArrayList<>(path1);
        Collections.reverse(revPath1);

        List<String> path2 = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath2 = new ArrayList<>(path2);
        Collections.reverse(revPath2);

        List<List<String>> azEros = new ArrayList<>();
        List<List<String>> zaEros = new ArrayList<>();

        azEros.add(path1);
        azEros.add(path2);
        zaEros.add(revPath1);
        zaEros.add(revPath2);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap1 = new HashMap<>();
        azGoalMap1.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap1.put(now.plus(2L, ChronoUnit.HOURS), 500);
        azGoalMap1.put(now.plus(3L, ChronoUnit.HOURS), 1000);
        azGoalMap1.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap1 = new HashMap<>();
        zaGoalMap1.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap1.put(now.plus(2L, ChronoUnit.HOURS), 500);
        zaGoalMap1.put(now.plus(3L, ChronoUnit.HOURS), 1000);
        zaGoalMap1.put(now.plus(5L, ChronoUnit.HOURS), 1000);

        Map<Instant, Integer> azGoalMap2 = new HashMap<>();
        azGoalMap2.put(now.plus(1L, ChronoUnit.HOURS), 300);
        azGoalMap2.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap2 = new HashMap<>();
        zaGoalMap2.put(now.plus(1L, ChronoUnit.HOURS), 300);
        zaGoalMap2.put(now.plus(5L, ChronoUnit.HOURS), 1000);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw1);
        minExpectedBwMap.put(za1, expectedMinZaBw1);
        minExpectedBwMap.put(az2, expectedMinAzBw2);
        minExpectedBwMap.put(za2, expectedMinZaBw2);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap1);
        expectedBwMap.put(za1, zaGoalMap1);
        expectedBwMap.put(az2, azGoalMap2);
        expectedBwMap.put(za2, zaGoalMap2);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime, azEros, zaEros);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }

    public void twoDisjointPaths(){

        reservedBandwidthRepo.deleteAll();
        topologyBuilder.buildTopoFourPaths();
        List<UrnE> urns = urnRepo.findAll();
        Map<String, UrnE> urnMap = urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
        Instant now = Instant.parse("1995-10-23T00:00:00Z");

        List<String> reservedPortNames = Arrays.asList("nodeP:1", "nodeW:1");

        List<Instant> reservedStartTimes = Arrays.asList(
                now.plus(2L, ChronoUnit.HOURS),
                now.plus(1L, ChronoUnit.HOURS));
        List<Instant> reservedEndTimes = Arrays.asList(
                now.plus(3L, ChronoUnit.HOURS),
                now.plus(5L, ChronoUnit.HOURS));
        List<Integer> inBandwidths = Arrays.asList(500, 700);
        List<Integer> egBandwidths = Arrays.asList(500, 700);

        Integer expectedMinAzBw1 = 500;
        Integer expectedMinZaBw1 = 500;
        Integer expectedMinAzBw2 = 300;
        Integer expectedMinZaBw2 = 300;

        List<String> path1 = Arrays.asList("portA", "nodeK", "nodeK:1", "nodeL:1", "nodeL", "nodeL:3", "nodeP:1", "nodeP", "nodeP:2", "nodeQ:1", "nodeQ", "portZ");
        List<String> revPath1 = new ArrayList<>(path1);
        Collections.reverse(revPath1);

        List<String> path2 = Arrays.asList("portA", "nodeK", "nodeK:3", "nodeW:1", "nodeW", "nodeW:2", "nodeQ:3", "nodeQ", "portZ");
        List<String> revPath2 = new ArrayList<>(path2);
        Collections.reverse(revPath2);

        List<List<String>> azEros = new ArrayList<>();
        List<List<String>> zaEros = new ArrayList<>();

        azEros.add(path1);
        azEros.add(path2);
        zaEros.add(revPath1);
        zaEros.add(revPath2);

        Instant requestStartTime = now.plus(1L, ChronoUnit.HOURS);
        Instant requestEndTime = now.plus(5L, ChronoUnit.HOURS);

        Map<Instant, Integer> azGoalMap1 = new HashMap<>();
        azGoalMap1.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        azGoalMap1.put(now.plus(2L, ChronoUnit.HOURS), 500);
        azGoalMap1.put(now.plus(3L, ChronoUnit.HOURS), 1000);
        azGoalMap1.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap1 = new HashMap<>();
        zaGoalMap1.put(now.plus(1L, ChronoUnit.HOURS), 1000);
        zaGoalMap1.put(now.plus(2L, ChronoUnit.HOURS), 500);
        zaGoalMap1.put(now.plus(3L, ChronoUnit.HOURS), 1000);
        zaGoalMap1.put(now.plus(5L, ChronoUnit.HOURS), 1000);

        Map<Instant, Integer> azGoalMap2 = new HashMap<>();
        azGoalMap2.put(now.plus(1L, ChronoUnit.HOURS), 300);
        azGoalMap2.put(now.plus(5L, ChronoUnit.HOURS), 1000);
        Map<Instant, Integer> zaGoalMap2 = new HashMap<>();
        zaGoalMap2.put(now.plus(1L, ChronoUnit.HOURS), 300);
        zaGoalMap2.put(now.plus(5L, ChronoUnit.HOURS), 1000);

        log.info("Start time: " + requestStartTime);
        log.info("End time: " + requestEndTime);

        Map<String, Integer> minExpectedBwMap = new HashMap<>();
        minExpectedBwMap.put(az1, expectedMinAzBw1);
        minExpectedBwMap.put(za1, expectedMinZaBw1);
        minExpectedBwMap.put(az2, expectedMinAzBw2);
        minExpectedBwMap.put(za2, expectedMinZaBw2);

        Map<String, Map<Instant, Integer>> expectedBwMap = new HashMap<>();
        expectedBwMap.put(az1, azGoalMap1);
        expectedBwMap.put(za1, zaGoalMap1);
        expectedBwMap.put(az2, azGoalMap2);
        expectedBwMap.put(za2, zaGoalMap2);

        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Reserve Bandwidth ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        repoEntityBuilder.reserveBandwidth(reservedPortNames, reservedStartTimes, reservedEndTimes, inBandwidths, egBandwidths);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~ Make the request ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        BandwidthAvailabilityResponse response = placeRequest(requestStartTime, requestEndTime, 2, true);
        testResponse(response, minExpectedBwMap, expectedBwMap);
    }


    private void testResponse(BandwidthAvailabilityResponse response,
                              Map<String, Integer> expectedMinAvailableBwMap,
                              Map<String, Map<Instant, Integer>> expectedBwMap) {

        Map<String, Map<Instant, Integer>> bwMap = response.getBwAvailabilityMap();
        Map<String, Integer> minAvailableBwMap = response.getMinAvailableBwMap();

        log.info("Expected Min BW Map: " + expectedMinAvailableBwMap);
        log.info("Actual Min BW Map: " + minAvailableBwMap);
        log.info("Expected BW Map: " + expectedBwMap);
        log.info("Actual BW Map: " + bwMap);

        assert(bwMap.equals(expectedBwMap));
        assert(minAvailableBwMap.equals(expectedMinAvailableBwMap));
    }

    private BandwidthAvailabilityResponse placeRequest(Instant requestStartTime, Instant requestEndTime) {
        BandwidthAvailabilityRequest request = BandwidthAvailabilityRequest.builder()
                .startDate(new Date(requestStartTime.toEpochMilli()))
                .endDate(new Date(requestEndTime.toEpochMilli()))
                .minAzBandwidth(10)
                .minZaBandwidth(10)
                .srcDevice("nodeK")
                .srcPorts(Collections.singleton("portA"))
                .dstDevice("nodeQ")
                .dstPorts(Collections.singleton("portZ"))
                .numPaths(1)
                .disjointPaths(true)
                .azEros(new ArrayList<>())
                .zaEros(new ArrayList<>())
                .build();

        return bwAvailService.getBandwidthAvailabilityMap(request);
    }

    private BandwidthAvailabilityResponse placeRequestJunction(Instant requestStartTime, Instant requestEndTime) {
        BandwidthAvailabilityRequest request = BandwidthAvailabilityRequest.builder()
                .startDate(new Date(requestStartTime.toEpochMilli()))
                .endDate(new Date(requestEndTime.toEpochMilli()))
                .minAzBandwidth(10)
                .minZaBandwidth(10)
                .srcDevice("nodeK")
                .srcPorts(Collections.singleton("portA"))
                .dstDevice("nodeK")
                .dstPorts(Collections.singleton("nodeK:3"))
                .numPaths(1)
                .disjointPaths(true)
                .azEros(new ArrayList<>())
                .zaEros(new ArrayList<>())
                .build();

        return bwAvailService.getBandwidthAvailabilityMap(request);
    }

    private BandwidthAvailabilityResponse placeRequestJunctionPath(Instant requestStartTime, Instant requestEndTime) {
        ArrayList<String> azERO = new ArrayList<>();
        azERO.add("portA");
        azERO.add("nodeK");
        azERO.add("nodeK:3");
        ArrayList<String> zaERO = new ArrayList<>(azERO);
        Collections.reverse(zaERO);

        BandwidthAvailabilityRequest request = BandwidthAvailabilityRequest.builder()
                .startDate(new Date(requestStartTime.toEpochMilli()))
                .endDate(new Date(requestEndTime.toEpochMilli()))
                .minAzBandwidth(10)
                .minZaBandwidth(10)
                .srcDevice("")
                .srcPorts(Collections.singleton(""))
                .dstDevice("")
                .dstPorts(Collections.singleton(""))
                .numPaths(1)
                .disjointPaths(true)
                .azEros(Collections.singletonList(azERO))
                .zaEros(Collections.singletonList(zaERO))
                .build();

        return bwAvailService.getBandwidthAvailabilityMap(request);
    }

    private BandwidthAvailabilityResponse placeRequest(Instant requestStartTime, Instant requestEndTime,
                                                       Integer numPaths, Boolean disjoint) {
        BandwidthAvailabilityRequest request = BandwidthAvailabilityRequest.builder()
                .startDate(new Date(requestStartTime.toEpochMilli()))
                .endDate(new Date(requestEndTime.toEpochMilli()))
                .minAzBandwidth(10)
                .minZaBandwidth(10)
                .srcDevice("nodeK")
                .srcPorts(Collections.singleton("portA"))
                .dstDevice("nodeQ")
                .dstPorts(Collections.singleton("portZ"))
                .numPaths(numPaths)
                .disjointPaths(disjoint)
                .azEros(new ArrayList<>())
                .zaEros(new ArrayList<>())
                .build();

        return bwAvailService.getBandwidthAvailabilityMap(request);
    }

    private BandwidthAvailabilityResponse placeRequest(Instant requestStartTime, Instant requestEndTime,
                                                       List<List<String>> azEROs, List<List<String>> zaEROs) {
        BandwidthAvailabilityRequest request = BandwidthAvailabilityRequest.builder()
                .startDate(new Date(requestStartTime.toEpochMilli()))
                .endDate(new Date(requestEndTime.toEpochMilli()))
                .minAzBandwidth(10)
                .minZaBandwidth(10)
                .azEros(azEROs)
                .zaEros(zaEROs)
                .build();

        return bwAvailService.getBandwidthAvailabilityMap(request);
    }
    
}
