package net.es.oscars.pss.svc;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.params.Lsp;
import net.es.oscars.dto.pss.params.MplsHop;
import net.es.oscars.dto.pss.params.MplsPath;
import net.es.oscars.dto.pss.params.Policing;
import net.es.oscars.dto.pss.params.alu.*;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
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

        Components cmp = c.getReserved().getCmp();

        List<AluQos> qoses = new ArrayList<>();
        List<AluSap> saps = new ArrayList<>();

        // TODO: non-strict policing
        for (VlanFixture rvf : cmp.getFixtures()) {
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
                .description(c.getConnectionId()+"-VPLS")
                .saps(saps)
                .serviceName("OSCARS-"+c.getConnectionId())
                .sdpToVcIds(new ArrayList<>())
                .svcId(aluSvcId)
                .build();

        List<Lsp> lsps = new ArrayList<>();
        List<AluSdp> sdps = new ArrayList<>();
        List<MplsPath> paths = new ArrayList<>();

        // for each pipe we need:
        // - an LSP to specify the hop-by-hop route over the network
        // - a Path that uses the LSP
        // - an SDP to use the path
        for (VlanPipe p : cmp.getPipes()) {
            Device a_d = null;
            Device z_d = null;
            List<EroHop> hops = null;

            if (p.getA().getDeviceUrn().equals(rvj.getDeviceUrn())) {
                a_d = topoService.getTopoUrnMap().get(p.getA().getDeviceUrn()).getDevice();
                z_d = topoService.getTopoUrnMap().get(p.getZ().getDeviceUrn()).getDevice();
                hops = p.getAzERO();
            } else if (p.getZ().getDeviceUrn().equals(rvj.getDeviceUrn())) {
                z_d = topoService.getTopoUrnMap().get(p.getA().getDeviceUrn()).getDevice();
                a_d = topoService.getTopoUrnMap().get(p.getZ().getDeviceUrn()).getDevice();
                hops = p.getZaERO();

            }
            if (hops != null) {
                PipeResult pr = this.makePipe(z_d, hops, p, rvj, c, vpls);
                sdps.add(pr.getSdp());
                paths.add(pr.getPath());
                lsps.add(pr.getLsp());
            }
        }

        return AluParams.builder()
                .applyQos(true)
                .lsps(lsps)
                .paths(paths)
                .sdps(sdps)
                .qoses(qoses)
                .aluVpls(vpls)
                .build();
    }

    @Data
    private class PipeResult {
        private MplsPath path;
        private Lsp lsp;
        private AluSdp sdp;
    }

    public PipeResult makePipe(Device z_d, List<EroHop> hops, VlanPipe p, VlanJunction j, Connection c, AluVpls vpls) throws PSSException {
        // eroHops look like this:
        // 0: A
        // 1:  A:1/1
        // 2: B:2/1
        // 3: B
        // 4: B:3/2
        // 5: C:4/1
        // 6: C
        // 7: C:8/2
        // 8: Z:2/1
        // 9: Z
        // to make the MPLS path, we take only hops that are index 2 mod 3
        List<MplsHop> mplsHops = new ArrayList<>();

        // hop index starts at 1
        Integer order = 1;
        for (int i = 0; i < hops.size(); i++) {
            if (i % 3 == 2) {
                EroHop hop = hops.get(i);
                Port port = topoService.getTopoUrnMap().get(hop.getUrn()).getPort();
                MplsHop mplsHop = MplsHop.builder()
                        .address(port.getIpv4Address())
                        .order(order)
                        .build();
                order = order + 1;
                mplsHops.add(mplsHop);
            }
        }

        MplsPath path = MplsPath.builder()
                .hops(mplsHops)
                .name(c.getConnectionId()+"-PATH-"+p.getZ().getDeviceUrn())
                .build();

        Lsp lsp = Lsp.builder()
                .holdPriority(5)
                .setupPriority(5)
                .metric(65000)
                .to(z_d.getIpv4Address())
                .pathName(path.getName())
                .name(c.getConnectionId()+"-LSP-"+p.getZ().getDeviceUrn())
                .build();

        Integer sdpId = null;
        for (CommandParam cp: j.getCommandParams()) {
            if (cp.getParamType().equals(CommandParamType.ALU_SDP_ID)) {
                if (cp.getIntent().equals(z_d.getUrn())) {
                    sdpId = cp.getResource();
                }
            }
        }

        if (sdpId == null) {
            throw new PSSException("no sdp id reserved!");
        }


        AluSdp sdp = AluSdp.builder()
                .sdpId(sdpId)
                .description(c.getConnectionId()+"-SDP-"+z_d.getUrn())
                .farEnd(z_d.getIpv4Address())
                .lspName(lsp.getName())
                .build();


        // bleh, side effect
        AluSdpToVcId sdpToVcId = AluSdpToVcId.builder().sdpId(sdpId).vcId(vpls.getSvcId()).build();
        vpls.getSdpToVcIds().add(sdpToVcId);


        PipeResult pr = new PipeResult();
        pr.setLsp(lsp);
        pr.setPath(path);
        pr.setSdp(sdp);

        return pr;

    }



}