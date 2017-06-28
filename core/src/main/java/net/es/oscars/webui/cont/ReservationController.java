package net.es.oscars.webui.cont;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.bwavail.svc.BandwidthAvailabilityService;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityResponse;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.cmd.GeneratedCommands;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ConnectionFilter;
import net.es.oscars.dto.resv.precheck.PreCheckResponse;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.RequestedVlanFlow;
import net.es.oscars.dto.spec.RequestedVlanPipe;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.dto.topo.BidirectionalPath;
import net.es.oscars.dto.topo.Edge;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.svc.RouterCommandsService;
import net.es.oscars.resv.ent.ConnectionE;
import net.es.oscars.resv.ent.RequestedVlanFixtureE;
import net.es.oscars.resv.ent.RequestedVlanJunctionE;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.st.oper.OperState;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;
import net.es.oscars.webui.dto.AdvancedRequest;
import net.es.oscars.webui.dto.ConnectionBuilder;
import net.es.oscars.webui.dto.Filter;
import net.es.oscars.webui.dto.MinimalRequest;
import org.hashids.Hashids;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class ReservationController {


    @Autowired
    public ReservationController(ResvService resvService,
                                 BandwidthAvailabilityService bwAvailService,
                                 RouterCommandsService routerCommandsService) {
        this.resvService = resvService;
        this.bwAvailService = bwAvailService;
        this.routerCommandsService = routerCommandsService;
    }

    private ResvService resvService;
    private BandwidthAvailabilityService bwAvailService;
    private RouterCommandsService routerCommandsService;

    private ModelMapper modelMapper = new ModelMapper();



    @RequestMapping(value = "/resv/get/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public Connection resv_get_details(@PathVariable String connectionId) {
        return convertConnToDto(resvService.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new));
    }


    @RequestMapping(value = "/resv/list/allconnections", method = RequestMethod.GET)
    @ResponseBody
    public Set<Connection> resv_list_connections() {
        ConnectionFilter f = makeConnectionFilter(Filter.builder().build());
        return this.filtered(f);
    }

    @RequestMapping(value = "/resv/list/filter", method = RequestMethod.POST)
    @ResponseBody
    public Set<Connection> resv_filter_connections(@RequestBody Filter filter) {
        ConnectionFilter f = makeConnectionFilter(filter);
        return this.filtered(f);
    }


    @RequestMapping(value = "/resv/commit", method = RequestMethod.POST)
    @ResponseBody
    public String connection_commit_react(@RequestBody String connectionId) {

        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.commitConnection(connectionId);
    }

    public String commitConnection(String connectionId) {

        log.info("attempting to commit " + connectionId);
        ConnectionE connE = resvService.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new);
        if (connE.getStates().getResv().equals(ResvState.HELD)) {
            connE.getStates().setResv(ResvState.COMMITTING);
            resvService.save(connE);
        }
        return connE.getConnectionId();
    }


    @RequestMapping(value = "/resv/newConnectionId", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> new_connection_id() {
        Map<String, String> result = new HashMap<>();

        Hashids hashids = new Hashids("oscars");

        boolean found = false;
        Random rand = new Random();
        String connectionId = "";
        while (!found) {
            Integer id = rand.nextInt();
            if (id < 0) {
                id = -1 * id;
            }
            connectionId = hashids.encode(id);
            Optional<ConnectionE> connE = resvService.findByConnectionId(connectionId);

            if (!connE.isPresent()) {
                // it's good that it doesn't exist, means we can use it
                found = true;
            }
        }

        result.put("connectionId", connectionId);
        log.info("provided new connection id: " + result.get("connectionId"));
        return result;
    }




    @RequestMapping(value = "/resv/commands/{connectionId}/{deviceUrn}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> commands(@PathVariable("connectionId") String connectionId,
                                        @PathVariable("deviceUrn") String deviceUrn) {
        log.info("getting commands for " + connectionId + " " + deviceUrn);

        GeneratedCommands commands = routerCommandsService.commands(connectionId, deviceUrn);

        // TODO: consume this on client side
        Map<String, String> badstuff = new HashMap<>();
        badstuff.put("commands", commands.getGenerated().get(CommandType.BUILD));
        return badstuff;
    }


    @RequestMapping(value = "/resv/minimal_hold", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> resv_minimal_hold(@RequestBody MinimalRequest request) throws PSSException, PCEException {
        Connection c = this.holdMinimal(request);
        Map<String, String> res = new HashMap<>();
        res.put("connectionId", c.getConnectionId());
        return res;

    }

    @RequestMapping(value = "/resv/advanced_hold", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> resv_advanced_hold(@RequestBody AdvancedRequest request) throws PSSException, PCEException {
        Connection c = this.holdAdvanced(request);
        Map<String, String> res = new HashMap<>();
        res.put("connectionId", c.getConnectionId());

        return res;
    }


    @RequestMapping(value = "/resv/precheck", method = RequestMethod.POST)
    @ResponseBody
    public PreCheckResponse resv_preCheck(@RequestBody MinimalRequest request) throws PSSException, PCEException {
        Connection c = this.preCheckMinimal(request);
        log.info("Request Details: " + request.toString());

        return processPrecheckResponse(request.getConnectionId(), c);
    }

    @RequestMapping(value = "/resv/advanced_precheck", method = RequestMethod.POST)
    @ResponseBody
    public PreCheckResponse resv_precheck_advanced(@RequestBody AdvancedRequest request) throws PSSException, PCEException {
        Connection c = this.preCheckAdvanced(request);
        log.info("Request Details: " + request.toString());

        return processPrecheckResponse(request.getConnectionId(), c);
    }

    private PreCheckResponse processPrecheckResponse(String connectionId, Connection c) {
        PreCheckResponse response = PreCheckResponse.builder()
                .connectionId(connectionId)
                .linksToHighlight(new HashSet<>())
                .nodesToHighlight(new HashSet<>())
                .precheckResult(PreCheckResponse.PrecheckResult.SUCCESS)
                .build();

        //TODO: Pass back reservation with all details
        if (c == null) {
            response.setPrecheckResult(PreCheckResponse.PrecheckResult.UNSUCCESSFUL);
            log.info("Pre-Check Result: UNSUCCESSFUL");
        } else {
            log.info("Pre-Check Result: SUCCESS");

            Set<BidirectionalPath> allPaths = c.getReserved().getVlanFlow().getAllPaths();

            for (BidirectionalPath biPath : allPaths) {
                List<Edge> oneAzPath = biPath.getAzPath();

                for (Edge edge : oneAzPath) {
                    if (edge.getOriginType().equals("DEVICE")) {
                        response.getNodesToHighlight().add(edge.getOrigin());
                    }
                    if (edge.getTargetType().equals("DEVICE")) {
                        response.getNodesToHighlight().add(edge.getTarget());
                    }
                    if (edge.getOriginType().equals("PORT") && edge.getTargetType().equals("PORT")) {
                        String linkName = edge.getOrigin() + " -- " + edge.getTarget();
                        response.getLinksToHighlight().add(linkName);
                    }
                }
            }

        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            log.info(pretty);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "/resv/topo/bwAvailAllPorts/", method = RequestMethod.POST)
    @ResponseBody
    public PortBandwidthAvailabilityResponse queryPortBwAvailability(@RequestBody MinimalRequest request) {
        log.info("Querying for Port Bandwdidth Availability");
        PortBandwidthAvailabilityRequest bwRequest = new PortBandwidthAvailabilityRequest();
        Date startDate = new Date(request.getStartAt() * 1000L);
        Date endDate = new Date(request.getEndAt() * 1000L);

        bwRequest.setStartDate(startDate);
        bwRequest.setEndDate(endDate);


        return bwAvailService.getBandwidthAvailabilityOnAllPorts(bwRequest);

    }




    private ConnectionFilter makeConnectionFilter(Filter filter) {
        Integer numFilters = filter.getNumFilters() == null ? 0 : filter.getNumFilters();

        Set<String> connectionIds = filter.getConnectionIds() == null ? new HashSet<>() : filter.getConnectionIds();

        Set<String> userNames = filter.getUserNames() == null ? new HashSet<>() : filter.getUserNames();

        DateFormat df = new ISO8601DateFormat();
        Set<Date> startDates = new HashSet<>();
        if (filter.getStartDates() != null) {
            for (String date : filter.getStartDates()) {
                try {
                    startDates.add(df.parse(date));
                } catch (ParseException e) {
                    log.info("Invalid start date input for filter: " + date);
                }
            }
        }
        Set<Date> endDates = new HashSet<>();
        if (filter.getEndDates() != null) {
            for (String date : filter.getEndDates()) {
                try {
                    endDates.add(df.parse(date));
                } catch (ParseException e) {
                    log.info("Invalid end date input for filter: " + date);
                }
            }
        }

        Set<ResvState> resvStates = filter.getResvStates() == null ? new HashSet<>() :
                filter.getResvStates().stream().map(s -> ResvState.get(s).orElse(ResvState.IDLE_WAIT)).collect(Collectors.toSet());

        Set<ProvState> provStates = filter.getProvStates() == null ? new HashSet<>() :
                filter.getProvStates().stream().map(s -> ProvState.get(s).orElse(ProvState.BUILT)).collect(Collectors.toSet());

        Set<OperState> operStates = filter.getOperStates() == null ? new HashSet<>() :
                filter.getOperStates().stream().map(s -> OperState.get(s).orElse(OperState.ADMIN_UP_OPER_UP)).collect(Collectors.toSet());

        Set<Integer> minBandwidths = filter.getMinBandwidths() == null ? new HashSet<>() : filter.getMinBandwidths();
        Set<Integer> maxBandwidths = filter.getMaxBandwidths() == null ? new HashSet<>() : filter.getMaxBandwidths();
        return ConnectionFilter.builder()
                .numFilters(numFilters)
                .connectionIds(connectionIds)
                .userNames(userNames)
                .resvStates(resvStates)
                .provStates(provStates)
                .operStates(operStates)
                .startDates(startDates)
                .endDates(endDates)
                .minBandwidths(minBandwidths)
                .maxBandwidths(maxBandwidths)
                .build();
    }


    public Set<Connection> filtered(ConnectionFilter filter) {

        List<ConnectionE> allConnections = resvService.findAll();
        Set<Connection> result = new HashSet<>();
        ;

        // No Filters specified
        if (filter.getNumFilters() == 0) {
            ;
        } else {
            /* Filter by exact matches */
            // User Name
            if (!filter.getUserNames().isEmpty()) {
                allConnections = allConnections.stream()
                        .filter(c -> filter.getUserNames().contains(c.getSpecification().getUsername()))
                        .collect(Collectors.toList());
            }

            // Resv State
            if (!filter.getResvStates().isEmpty()) {
                allConnections = allConnections.stream()
                        .filter(c -> filter.getResvStates().contains(c.getStates().getResv()))
                        .collect(Collectors.toList());
            }

            // Oper State
            if (!filter.getOperStates().isEmpty()) {
                allConnections = allConnections.stream()
                        .filter(c -> filter.getOperStates().contains(c.getStates().getOper()))
                        .collect(Collectors.toList());
            }

            // Prov State
            if (!filter.getProvStates().isEmpty()) {
                allConnections = allConnections.stream()
                        .filter(c -> filter.getProvStates().contains(c.getStates().getProv()))
                        .collect(Collectors.toList());
            }

            // Connection ID
            if (!filter.getConnectionIds().isEmpty()) {
                allConnections = allConnections.stream()
                        .filter(c -> filter.getConnectionIds().contains(c.getConnectionId()))
                        .collect(Collectors.toList());
            }


            /* Filter by range values */
            Set<Date> allStartDates = filter.getStartDates();
            Set<Date> allEndDates = filter.getEndDates();
            Set<Integer> allMinBWs = filter.getMinBandwidths();
            Set<Integer> allMaxBWs = filter.getMaxBandwidths();

            List<ConnectionE> connstoRemove = new ArrayList<>();

            // Start Dates
            if (!allStartDates.isEmpty()) {
                Date earliestStart = allStartDates.stream().findFirst().get();

                for (Date oneStartDate : allStartDates) {
                    if (oneStartDate.before(earliestStart))
                        earliestStart = oneStartDate;
                }

                for (ConnectionE c : allConnections) {
                    if (earliestStart.after(c.getSpecification().getScheduleSpec().getStartDates().get(0)))  // Only checks the first requested Start Date.
                        connstoRemove.add(c);
                }
            }

            // End Dates
            if (!allEndDates.isEmpty()) {
                Date latestEnd = allEndDates.stream().findFirst().get();

                for (Date oneEndDate : allEndDates) {
                    if (oneEndDate.after(latestEnd))
                        latestEnd = oneEndDate;
                }

                for (ConnectionE c : allConnections) {
                    if (latestEnd.before(c.getSpecification().getScheduleSpec().getStartDates().get(0)))  // Only checks the first requested End Date.
                        connstoRemove.add(c);
                }
            }

            // Min/Max Bandwidth
            Integer smallestMin = Integer.MAX_VALUE;
            Integer largestMax = Integer.MIN_VALUE;

            if (!allMinBWs.isEmpty()) {
                for (Integer oneMin : allMinBWs) {
                    if (oneMin < smallestMin)
                        smallestMin = oneMin;
                }
            }

            if (!allMaxBWs.isEmpty()) {
                for (Integer oneMax : allMaxBWs) {
                    if (oneMax > largestMax)
                        largestMax = oneMax;
                }
            }

            for (ConnectionE c : allConnections) {
                Set<RequestedVlanPipeE> allRequestedPipes = c.getSpecification().getRequested().getVlanFlow().getPipes();
                Set<RequestedVlanJunctionE> allRequestedJunctions = c.getSpecification().getRequested().getVlanFlow().getJunctions();

                Integer largestRequested = 0;
                Integer smallestRequested = Integer.MAX_VALUE;

                for (RequestedVlanPipeE onePipe : allRequestedPipes) {
                    if (onePipe.getAzMbps() > largestRequested)
                        largestRequested = onePipe.getAzMbps();

                    if (onePipe.getZaMbps() > largestRequested)
                        largestRequested = onePipe.getZaMbps();

                    if (onePipe.getAzMbps() < smallestRequested)
                        smallestRequested = onePipe.getAzMbps();

                    if (onePipe.getZaMbps() < smallestRequested)
                        smallestRequested = onePipe.getZaMbps();
                }

                for (RequestedVlanJunctionE oneJunc : allRequestedJunctions) {
                    Set<RequestedVlanFixtureE> theRequestedFixtures = oneJunc.getFixtures();

                    for (RequestedVlanFixtureE oneFix : theRequestedFixtures) {
                        if (oneFix.getInMbps() > largestRequested)
                            largestRequested = oneFix.getInMbps();

                        if (oneFix.getEgMbps() > largestRequested)
                            largestRequested = oneFix.getEgMbps();

                        if (oneFix.getInMbps() < smallestRequested)
                            smallestRequested = oneFix.getInMbps();

                        if (oneFix.getEgMbps() < smallestRequested)
                            smallestRequested = oneFix.getEgMbps();

                    }
                }

                Integer correctedMin = smallestMin;
                Integer correctedMax = largestMax;

                if (smallestMin == Integer.MAX_VALUE)
                    correctedMin = Integer.MIN_VALUE;

                if (largestMax == Integer.MIN_VALUE)
                    correctedMax = Integer.MAX_VALUE;

                if (!(correctedMin <= largestRequested && correctedMax >= smallestRequested))
                    connstoRemove.add(c);
            }

            allConnections.removeAll(connstoRemove);
        }

        for (ConnectionE oneConnectionE : allConnections) {
            Connection oneConnDTO = convertConnToDto(oneConnectionE);
            result.add(oneConnDTO);
        }

        return result;
    }


    public Connection holdMinimal(MinimalRequest minimalRequest) throws PCEException, PSSException {
        log.info("holding minimal " + minimalRequest.toString());

        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        Connection c = connectionBuilder.buildConnectionFromMinimalRequest(minimalRequest);
        return holdConnection(c);
    }

    public Connection holdAdvanced(AdvancedRequest advancedRequest) throws PCEException, PSSException {
        log.info("Holding advanced " + advancedRequest.toString());

        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        Connection c = connectionBuilder.buildConnectionFromAdvancedRequest(advancedRequest);
        return holdConnection(c);
    }

    public Connection holdConnection(Connection connection) throws PCEException, PSSException {
        connection = defineDefaults(connection);
        ConnectionE connE = modelMapper.map(connection, ConnectionE.class);

        resvService.hold(connE);

        log.info("saved connection, connectionId " + connection.getConnectionId());
        log.info(connE.toString());


        Connection conn = modelMapper.map(connE, Connection.class);
        log.info(conn.toString());


        return conn;

    }


    private Connection defineDefaults(Connection connection) {
        RequestedVlanFlow flow = connection.getSpecification().getRequested().getVlanFlow();
        if (flow.getMinPipes() == null) {
            flow.setMinPipes(flow.getPipes().size());
        }
        if (flow.getMaxPipes() == null) {
            flow.setMaxPipes(flow.getPipes().size());
        }

        Set<RequestedVlanPipe> pipes = flow.getPipes();
        for (RequestedVlanPipe pipe : pipes) {
            if (pipe.getAzMbps() == null) {
                pipe.setAzMbps(0);
            }
            if (pipe.getZaMbps() == null) {
                pipe.setZaMbps(0);
            }
            if (pipe.getAzERO() == null) {
                pipe.setAzERO(new ArrayList<>());
            }
            if (pipe.getZaERO() == null) {
                pipe.setZaERO(new ArrayList<>());
            }
            if (pipe.getUrnBlacklist() == null) {
                pipe.setUrnBlacklist(new HashSet<>());
            }
            if (pipe.getPipeType() == null) {
                pipe.setPipeType(EthPipeType.REQUESTED);
            }
            if (pipe.getEroPalindromic() == null) {
                pipe.setEroPalindromic(PalindromicType.PALINDROME);
            }
            if (pipe.getEroSurvivability() == null) {
                pipe.setEroSurvivability(SurvivabilityType.SURVIVABILITY_NONE);
            }
            if (pipe.getNumPaths() == null) {
                pipe.setNumPaths(1);
            }
            if (pipe.getPriority() == null) {
                pipe.setPriority(Integer.MAX_VALUE);
            }
        }
        flow.setPipes(pipes);
        connection.getSpecification().getRequested().setVlanFlow(flow);
        return connection;
    }


    public Connection preCheckMinimal(MinimalRequest minimalRequest) throws PCEException, PSSException {
        log.info("Pre-checking minimal " + minimalRequest.getConnectionId());

        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        Connection c = connectionBuilder.buildConnectionFromMinimalRequest(minimalRequest);
        return preCheck(c);
    }

    public Connection preCheckAdvanced(AdvancedRequest advancedRequest) throws PCEException, PSSException {
        log.info("Pre-checking minimal " + advancedRequest.getConnectionId());

        ConnectionBuilder connectionBuilder = new ConnectionBuilder();
        Connection c = connectionBuilder.buildConnectionFromAdvancedRequest(advancedRequest);
        return preCheck(c);
    }

    public Connection preCheck(Connection connection) throws PCEException, PSSException {
        log.info("Pre-checking ConnectionID: " + connection.getConnectionId());
        connection = defineDefaults(connection);
        ConnectionE connE = modelMapper.map(connection, ConnectionE.class);

        Boolean successful = resvService.preCheck(connE);

        Connection conn = modelMapper.map(connE, Connection.class);

        if (successful) {
            log.info("Pre-check result: SUCCESS");
            return conn;
        } else {
            log.info("Pre-check result: UNSUCCESSFUL");
            return null;
        }
    }

    public Connection convertConnToDto(ConnectionE connectionE) {
        return modelMapper.map(connectionE, Connection.class);
    }

}