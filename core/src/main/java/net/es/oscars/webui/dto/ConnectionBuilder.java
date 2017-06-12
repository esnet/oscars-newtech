package net.es.oscars.webui.dto;

import net.es.oscars.dto.pss.EthFixtureType;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.pss.EthPipeType;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.Schedule;
import net.es.oscars.dto.resv.States;
import net.es.oscars.dto.spec.*;
import net.es.oscars.st.oper.OperState;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;

import java.util.*;
import java.util.stream.Collectors;


public class ConnectionBuilder
{


    public Connection buildConnectionFromMinimalRequest(MinimalRequest minRequest)
    {
        return buildConnection(minRequest.getConnectionId(), minRequest.getStartAt(), minRequest.getEndAt(),
                minRequest.getJunctions(), minRequest.getPipes(), new HashMap<>(), false, minRequest.getDescription());
    }

    public Connection buildConnectionFromAdvancedRequest(AdvancedRequest advRequest){

        return buildConnection(advRequest.getConnectionId(), advRequest.getStartAt(), advRequest.getEndAt(),
                advRequest.getJunctions(), new HashMap<>(), advRequest.getPipes(), true, advRequest.getDescription());
    }

    private Connection buildConnection(String connectionId, Integer startAt, Integer endAt, Map<String, MinimalJunction> minimalJunctionMap,
                                       Map<String, MinimalPipe> minimalPipeMap, Map<String, AdvancedPipe> advancedPipeMap,
                                       Boolean advanced, String description) {

        List<Date> startDates = Collections.singletonList(buildDate(startAt));
        List<Date> endDates = Collections.singletonList(buildDate(endAt));

        //TODO: Pull this minimum duration from the user
        Long minimumDuration = 0L;

        String username = "some user";

        ReservedBlueprint rbp = buildReservedBlueprint(connectionId);
        ScheduleSpecification ss = buildScheduleSpecification(startDates, endDates, minimumDuration);

        Schedule sch = buildSchedule(startDates.get(0), endDates.get(0));

        States states = buildStates();

        Layer3Flow layer3Flow = buildLayer3Flow();

        Map<String, RequestedVlanJunction> junctionMap = buildJunctionMap(minimalJunctionMap);
        Set<RequestedVlanJunction> inPipes = new HashSet<>();
        Set<RequestedVlanPipe> pipes;
        if(advanced){
            pipes = buildRequestedPipesFromAdvanced(advancedPipeMap, inPipes, junctionMap);
        }
        else{
            pipes = buildRequestedPipesFromMinimal(minimalPipeMap, inPipes, junctionMap);
        }
        Set<RequestedVlanJunction> junctions = junctionMap.values().stream().filter(j -> !inPipes.contains(j)).collect(Collectors.toSet());

        RequestedVlanFlow reqvf = buildRequestedVlanFlow(junctions, pipes, pipes.size(), pipes.size(), connectionId);

        RequestedBlueprint reqbp = buildRequestedBlueprint(layer3Flow, reqvf, connectionId);

        Specification spec = buildSpecification(ss, connectionId, description, reqbp, username);

        return Connection.builder()
                .connectionId(connectionId)
                .specification(spec)
                .reserved(rbp)
                .schedule(sch)
                .states(states)
                .reservedSchedule(new ArrayList<>())
                .build();
    }


    public Date buildDate(Integer date){
        // Multiply start and end times by 1000
        // Unix time was returned by resv_gui.js as the number of SECONDS since the epoch
        // Date expects the number of MILLISECONDS
        return new Date(date * 1000L);
    }

    public ReservedBlueprint buildReservedBlueprint(String connectionId){
        ReservedVlanFlow resvf = ReservedVlanFlow.builder()
                .ethPipes(new HashSet<>())
                .junctions(new HashSet<>())
                .mplsPipes(new HashSet<>())
                .containerConnectionId(connectionId)
                .build();

        return  ReservedBlueprint.builder()
                .vlanFlow(resvf)
                .containerConnectionId(connectionId)
                .build();
    }

    public ScheduleSpecification buildScheduleSpecification(List<Date> startDates, List<Date> endDates, Long minDuration){
        return ScheduleSpecification.builder()
                .startDates(startDates)
                .endDates(endDates)
                .minimumDuration(minDuration)
                .build();
    }

    public Schedule buildSchedule(Date startDate, Date endDate){
        return Schedule.builder()
                .setup(startDate)
                .teardown(endDate)
                .submitted(new Date())
                .build();
    }

    public States buildStates(){
        return States.builder()
                .oper(OperState.ADMIN_DOWN_OPER_DOWN)
                .prov(ProvState.INITIAL)
                .resv(ResvState.SUBMITTED)
                .build();
    }

    public Layer3Flow buildLayer3Flow(){
        return Layer3Flow.builder()
                .junctions(new HashSet<>())
                .pipes(new HashSet<>())
                .build();
    }

    public RequestedVlanFlow buildRequestedVlanFlow(Set<RequestedVlanJunction> junctions, Set<RequestedVlanPipe> pipes,
                                                    Integer minPipes, Integer maxPipes, String connectionId){
        return RequestedVlanFlow.builder()
                .junctions(junctions)
                .pipes(pipes)
                .maxPipes(maxPipes)
                .minPipes(minPipes)
                .containerConnectionId(connectionId)
                .build();
    }

    public Map<String, RequestedVlanJunction> buildJunctionMap(Map<String, MinimalJunction> junctions){
        Map<String, RequestedVlanJunction> junctionMap = new HashMap<>();

        for (String nodeId : junctions.keySet()) {
            RequestedVlanJunction rvj = RequestedVlanJunction.builder()
                    .deviceUrn(nodeId)
                    .fixtures(new HashSet<>())
                    .junctionType(EthJunctionType.REQUESTED)
                    .build();
            for (String port : junctions.get(nodeId).getFixtures().keySet()) {
                MinimalFixture fix = junctions.get(nodeId).getFixtures().get(port);
                Integer azBw = Integer.parseInt(fix.getAzbw());
                Integer zaBw = Integer.parseInt(fix.getZabw());
                String vlan = fix.getVlan();

                RequestedVlanFixture rvfix = RequestedVlanFixture.builder()
                        .fixtureType(EthFixtureType.REQUESTED)
                        .portUrn(port)
                        .egMbps(zaBw)
                        .inMbps(azBw)
                        .vlanExpression(vlan)
                        .build();
                rvj.getFixtures().add(rvfix);
            }

            junctionMap.put(nodeId, rvj);
        }
        return junctionMap;
    }

    public Set<RequestedVlanPipe> buildRequestedPipesFromMinimal(Map<String, MinimalPipe> minimalPipeMap, Set<RequestedVlanJunction> inPipes,
                                                                 Map<String, RequestedVlanJunction> junctionMap){
        Set<RequestedVlanPipe> pipes = new HashSet<>();
        for (String pipeId : minimalPipeMap.keySet()) {
            MinimalPipe mp = minimalPipeMap.get(pipeId);
            RequestedVlanJunction aJ = junctionMap.get(mp.getA());
            RequestedVlanJunction zJ = junctionMap.get(mp.getZ());
            // Store used junctions in inPipes set
            inPipes.add(aJ);
            inPipes.add(zJ);
            Integer bw = Integer.parseInt(mp.getBw());

            List<String> azERO = mp.getAzERO() != null ? mp.getAzERO() : new ArrayList<>();

            List<String> zaERO = mp.getZaERO() != null ? mp.getZaERO() : new ArrayList<>();

            RequestedVlanPipe rvp = buildRequestedPipe(aJ, zJ, 1, bw, bw, azERO, zaERO, new HashSet<>(),
                    PalindromicType.PALINDROME, SurvivabilityType.SURVIVABILITY_NONE);
            pipes.add(rvp);
        }
        return pipes;
    }

    public Set<RequestedVlanPipe> buildRequestedPipesFromAdvanced(Map<String, AdvancedPipe> advancedPipeMap, Set<RequestedVlanJunction> inPipes,
                                                                 Map<String, RequestedVlanJunction> junctionMap){
        Set<RequestedVlanPipe> pipes = new HashSet<>();
        for (String pipeId : advancedPipeMap.keySet()) {
            AdvancedPipe ap = advancedPipeMap.get(pipeId);
            RequestedVlanJunction aJ = junctionMap.get(ap.getA());
            RequestedVlanJunction zJ = junctionMap.get(ap.getZ());
            // Store used junctions in inPipes set
            inPipes.add(aJ);
            inPipes.add(zJ);
            Integer azBw = Integer.parseInt(ap.getAzbw());
            Integer zaBw = Integer.parseInt(ap.getZabw());

            List<String> azERO = ap.getAzERO() != null ? ap.getAzERO() : new ArrayList<>();

            List<String> zaERO = ap.getZaERO() != null ? ap.getZaERO() : new ArrayList<>();

            Set<String> blackList = ap.getBlacklist() != null ? new HashSet<>(ap.getBlacklist()) : new HashSet<>();

            Integer numPaths = Integer.valueOf(ap.getNumPaths());

            PalindromicType pType =ap.getPalindromicPath() ? PalindromicType.PALINDROME : PalindromicType.NON_PALINDROME;
            SurvivabilityType sType = determineSurvivabilityType(ap.getSurvivabilityType());

            RequestedVlanPipe rvp = buildRequestedPipe(aJ, zJ, numPaths, azBw, zaBw, azERO, zaERO, blackList, pType, sType);
            pipes.add(rvp);
        }
        return pipes;
    }

    private SurvivabilityType determineSurvivabilityType(String survivabilityType) {
        if(survivabilityType.equals("End-to-End")){
            return SurvivabilityType.SURVIVABILITY_TOTAL;
        }
        else if(survivabilityType.contains("MPLS")){
            return SurvivabilityType.SURVIVABILITY_PARTIAL;
        }
        else{
            return SurvivabilityType.SURVIVABILITY_NONE;
        }
    }

    public RequestedVlanPipe buildRequestedPipe(RequestedVlanJunction aJ, RequestedVlanJunction zJ, Integer numPaths,
                                                Integer azBw, Integer zaBw, List<String> azERO, List<String> zaERO,
                                                Set<String> blacklist, PalindromicType pType, SurvivabilityType sType){
        return RequestedVlanPipe.builder()
                .aJunction(aJ)
                .zJunction(zJ)
                .numPaths(numPaths)
                .azMbps(azBw)
                .zaMbps(zaBw)
                .azERO(azERO)
                .zaERO(zaERO)
                .urnBlacklist(blacklist)
                .eroPalindromic(pType)
                .eroSurvivability(sType)
                .priority(Integer.MAX_VALUE)
                .pipeType(EthPipeType.REQUESTED)
                .build();
    }

    public RequestedBlueprint buildRequestedBlueprint(Layer3Flow layer3Flow, RequestedVlanFlow vlanFlow, String connectionId){
        return RequestedBlueprint.builder()
                .layer3Flow(layer3Flow)
                .vlanFlow(vlanFlow)
                .containerConnectionId(connectionId)
                .build();
    }

    public Specification buildSpecification(ScheduleSpecification ss, String connectionId, String description,
                                            RequestedBlueprint reqbp, String username){
        return Specification.builder()
                .scheduleSpec(ss)
                .containerConnectionId(connectionId)
                .description(description)
                .requested(reqbp)
                .username(username)
                .version(0)
                .build();
    }

}
