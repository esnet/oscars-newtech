package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PSSException;
import net.es.oscars.spec.ent.*;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TopPCE {

    @Autowired
    private TopoService topoService;

    @Autowired
    private EthPCE ethPCE;

    @Autowired
    private Layer3PCE layer3PCE;

    public BlueprintE makeReserved(BlueprintE requested) throws PCEException, PSSException {

        verifyRequested(requested);

        BlueprintE reserved = BlueprintE.builder()
                .layer3Flows(new HashSet<>())
                .vlanFlows(new HashSet<>())
                .build();

        for (Layer3FlowE req_f : requested.getLayer3Flows()) {
            Layer3FlowE res_f = layer3PCE.makeReserved(req_f);
            reserved.getLayer3Flows().add(res_f);
        }



        for (VlanFlowE req_f : requested.getVlanFlows()) {
            VlanFlowE res_f = ethPCE.makeReserved(req_f);
            reserved.getVlanFlows().add(res_f);

        }
        return reserved;

    }

    public void verifyRequested(BlueprintE requested) throws PCEException {
        log.info("starting verification");
        if (requested == null) {
            throw new PCEException("Null blueprint!");
        } else if (requested.getVlanFlows() == null || requested.getVlanFlows().isEmpty()) {
            throw new PCEException("No VLAN flows");
        } else if (requested.getVlanFlows().size() != 1) {
            throw new PCEException("Exactly one flow supported right now");
        }

        VlanFlowE flow = requested.getVlanFlows().iterator().next();

        log.info("verifying junctions & pipes");
        if (flow.getJunctions().isEmpty() && flow.getPipes().isEmpty()) {
            throw new PCEException("Junctions or pipes both empty.");
        }

        Set<VlanJunctionE> allJunctions = flow.getJunctions();
        flow.getPipes().stream().forEach(t -> {
            allJunctions.add(t.getAJunction());
            allJunctions.add(t.getZJunction());
        });

        for (VlanJunctionE junction: allJunctions) {
            // throws exception if device not found in topology
            topoService.device(junction.getDeviceUrn());
        }

        Set<String> junctionsWithNoFixtures = allJunctions.stream().
                filter(t -> t.getFixtures().isEmpty()).
                map(VlanJunctionE::getDeviceUrn).collect(Collectors.toSet());

        if (!junctionsWithNoFixtures.isEmpty()) {
            throw new PCEException("Junctions with no fixtures found: " + String.join(" ", junctionsWithNoFixtures));
        }

    }
}