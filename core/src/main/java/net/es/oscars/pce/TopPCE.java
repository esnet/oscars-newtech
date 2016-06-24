package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TopPCE {

    @Autowired
    private TopoService topoService;

    @Autowired
    private PruningService pruningService;

    @Autowired
    private EthPCE ethPCE;

    @Autowired
    private Layer3PCE layer3PCE;

    public ReservedBlueprintE makeReserved(RequestedBlueprintE requested, ScheduleSpecificationE schedSpec) throws PCEException, PSSException {

        verifyRequested(requested);

        ReservedBlueprintE reserved = ReservedBlueprintE.builder()
                .vlanFlows(new HashSet<>())
                .build();

        /*
        for (Layer3FlowE req_f : requested.getLayer3Flows()) {
            Layer3FlowE res_f = layer3PCE.makeReserved(req_f, schedSpec);
            reserved.getLayer3Flows().add(res_f);
        }
        */



        for (RequestedVlanFlowE req_f : requested.getVlanFlows()) {
            for(RequestedVlanPipeE pipe : req_f.getPipes()){
                //Prune MPLS and Ethernet Topologies (Bandwidth, VLANs)
                //Build specialized service layer topology for this pipe
                //Run Symmetric Dijkstra on this Topology
                //Translate the Shortest Path into EROs, then EROs to Junction/Pipes/Fixtures
            }
            ReservedVlanFlowE res_f = ethPCE.makeReserved(req_f, schedSpec);
            reserved.getVlanFlows().add(res_f);

        }
        return reserved;

    }

    public void verifyRequested(RequestedBlueprintE requested) throws PCEException {
        log.info("starting verification");
        if (requested == null) {
            throw new PCEException("Null blueprint!");
        } else if (requested.getVlanFlows() == null || requested.getVlanFlows().isEmpty()) {
            throw new PCEException("No VLAN flows");
        } else if (requested.getVlanFlows().size() != 1) {
            throw new PCEException("Exactly one flow supported right now");
        }

        RequestedVlanFlowE flow = requested.getVlanFlows().iterator().next();

        log.info("verifying junctions & pipes");
        if (flow.getJunctions().isEmpty() && flow.getPipes().isEmpty()) {
            throw new PCEException("Junctions or pipes both empty.");
        }

        Set<RequestedVlanJunctionE> allJunctions = new HashSet<>();
        allJunctions.addAll(flow.getJunctions());
        flow.getPipes().stream().forEach(t -> {
            allJunctions.add(t.getAJunction());
            allJunctions.add(t.getZJunction());
        });

        for (RequestedVlanJunctionE junction: allJunctions) {
            // throws exception if device not found in topology
            try {
                topoService.device(junction.getDeviceUrn().getUrn());
            } catch (NoSuchElementException ex) {
                throw new PCEException("device not found in topology");
            }
        }

        Set<String> junctionsWithNoFixtures = allJunctions.stream().
                filter(t -> t.getFixtures().isEmpty()).
                map(t -> t.getDeviceUrn().getUrn()).collect(Collectors.toSet());

        if (!junctionsWithNoFixtures.isEmpty()) {
            throw new PCEException("Junctions with no fixtures found: " + String.join(" ", junctionsWithNoFixtures));
        }
        log.info("all junctions & pipes are ok");

    }
}
