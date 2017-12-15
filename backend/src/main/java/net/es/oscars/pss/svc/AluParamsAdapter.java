package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.params.Policing;
import net.es.oscars.dto.pss.params.alu.*;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.enums.CommandParamType;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AluParamsAdapter {

    @Autowired
    private TopoService topoService;

    public AluParams params(Connection c, VlanJunction rvj) throws PSSException {
        Integer aluSvcId = null;
        log.info("making ALU params");
        for (CommandParam rpr : rvj.getCommandParams()) {
            if (rpr.getParamType().equals(CommandParamType.ALU_SVC_ID)) {
                aluSvcId = rpr.getResource();
            }
        }
        if (aluSvcId == null) {
            log.error("no ALU SVC ID");
            throw new PSSException("ALU svc id not found!");
        }


        List<AluQos> qoses = new ArrayList<>();
        List<AluSap> saps = new ArrayList<>();


        for (VlanFixture rvf : c.getReserved().getCmp().getFixtures()) {
            if (rvf.getJunction().equals(rvj)) {
                Integer inQosId = null;
                Integer egQosId = null;
                Integer vlan = rvf.getVlan().getVlanId();
                for (CommandParam cp : rvf.getCommandParams()) {
                    if (cp.getParamType().equals(CommandParamType.ALU_QOS_POLICY_ID)) {
                        inQosId = cp.getResource();
                        egQosId = cp.getResource();
                    }
                }

                if (inQosId == null || egQosId == null) {
                    throw new PSSException("in / eg qosId not reserved");
                }

                TopoUrn urn = topoService.getTopoUrnMap().get(rvf.getPortUrn());
                if (!urn.getUrnType().equals(UrnType.PORT)) {
                    throw new PSSException("invalid urn type");
                }
                String portUrn = urn.getPort().getUrn();
                String[] parts = portUrn.split(":");
                if (parts.length != 2) {
                    throw new PSSException("Invalid port URN format");
                }
                String port = parts[1];

                String configPortStr = port.replace('/', '_') + '_' + vlan;
                AluQos inQos = AluQos.builder()
                        .description(c.getConnectionId())
                        .mbps(rvf.getIngressBandwidth())
                        .policing(Policing.STRICT)
                        .policyId(inQosId)
                        .policyName(c.getConnectionId() + "-" + configPortStr + "-in")
                        .type(AluQosType.SAP_INGRESS)
                        .build();
                qoses.add(inQos);
                AluQos egQos = AluQos.builder()
                        .description(c.getConnectionId())
                        .mbps(rvf.getEgressBandwidth())
                        .policing(Policing.STRICT)
                        .policyId(egQosId)
                        .policyName(c.getConnectionId() + "-" + configPortStr + "-eg")
                        .type(AluQosType.SAP_EGRESS)
                        .build();
                qoses.add(egQos);

                AluSap sap = AluSap.builder()
                        .vlan(vlan)
                        .ingressQosId(inQosId)
                        .egressQosId(egQosId)
                        .port(port)
                        .description(c.getConnectionId() + " - " + configPortStr)
                        .build();
                saps.add(sap);
            }
        }

        AluVpls vpls = AluVpls.builder()
                .description(c.getConnectionId())
                .saps(saps)
                .serviceName(c.getConnectionId())
                .svcId(aluSvcId)
                .build();

        // TODO: add paths and LSPs and whatnot!
        return AluParams.builder()
                .applyQos(true)
                .qoses(qoses)
                .aluVpls(vpls)
                .build();
    }


}