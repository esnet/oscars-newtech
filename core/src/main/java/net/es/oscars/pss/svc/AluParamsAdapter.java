package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.params.Policing;
import net.es.oscars.dto.pss.params.alu.*;
import net.es.oscars.dto.resv.ResourceType;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AluParamsAdapter {



    public AluParams isolatedJunction(ConnectionE c, ReservedVlanJunctionE rvj) throws PSSException {
        log.info("creating ALU params for an isolated junction");
        ReservedPssResourceE aluSvcId = null;
        for (ReservedPssResourceE rpr : rvj.getReservedPssResources()) {
            if (rpr.getResourceType().equals(ResourceType.ALU_SVC_ID)) {
                aluSvcId = rpr;
            }
        }
        if (aluSvcId == null) {
            log.error("no ALU SVC ID");
            throw new PSSException("ALU svc id not found!");
        }


        List<AluQos> qoses = new ArrayList<>();
        List<AluSap> saps = new ArrayList<>();

        for (ReservedVlanFixtureE rvf : rvj.getFixtures()) {

            Integer vlan = null;
            for (ReservedVlanE rv : rvf.getReservedVlans()) {
                vlan = rv.getVlan();
            }
            if (vlan == null) {
                throw new PSSException("no VLAN reserved");
            }

            ReservedPssResourceE inQosIdRes = null;
            ReservedPssResourceE egQosIdRes = null;
            for (ReservedPssResourceE rpr : rvf.getReservedPssResources()) {
                if (rpr.getResourceType().equals(ResourceType.ALU_INGRESS_POLICY_ID)) {
                    inQosIdRes = rpr;
                } else if (rpr.getResourceType().equals(ResourceType.ALU_EGRESS_POLICY_ID)) {
                    egQosIdRes = rpr;
                }
            }

            String[] parts = rvf.getIfceUrn().split(":");
            if (parts.length != 2) {
                throw new PSSException("Invalid port URN format");
            }
            String port = parts[1];

            String configPortStr = port.replace('/', '_')+'_'+vlan;
            if (inQosIdRes != null) {
                AluQos qos = AluQos.builder()
                        .description(c.getConnectionId())
                        .mbps(rvf.getReservedBandwidth().getInBandwidth())
                        .policing(Policing.STRICT)
                        .policyId(inQosIdRes.getResource())
                        .policyName(c.getConnectionId() + "-" + configPortStr + "-in")
                        .type(AluQosType.SAP_INGRESS)
                        .build();
                qoses.add(qos);

            } else {
                throw new PSSException("no ingress qos id");
            }

            if (egQosIdRes != null) {
                AluQos qos = AluQos.builder()
                        .description(c.getConnectionId())
                        .mbps(rvf.getReservedBandwidth().getInBandwidth())
                        .policing(Policing.STRICT)
                        .policyId(egQosIdRes.getResource())
                        .policyName(c.getConnectionId() + "-" + configPortStr + "-eg")
                        .type(AluQosType.SAP_EGRESS)
                        .build();
                qoses.add(qos);
            } else {
                throw new PSSException("no egress qos id");
            }


            AluSap sap = AluSap.builder()
                    .vlan(vlan)
                    .ingressQosId(inQosIdRes.getResource())
                    .egressQosId(egQosIdRes.getResource())
                    .port(port)
                    .description(c.getConnectionId()+" - "+configPortStr)
                    .build();
            saps.add(sap);
        }

        AluVpls vpls = AluVpls.builder()
                .description(c.getConnectionId())
                .saps(saps)
                .serviceName(c.getConnectionId())
                .svcId(aluSvcId.getResource())
                .build();

        return AluParams.builder()
                .applyQos(true)
                .qoses(qoses)
                .aluVpls(vpls)
                .build();
    }


}
