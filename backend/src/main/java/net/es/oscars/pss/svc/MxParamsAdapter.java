package net.es.oscars.pss.svc;

import inet.ipaddr.ipv4.IPv4Address;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.params.Lsp;
import net.es.oscars.dto.pss.params.MplsHop;
import net.es.oscars.dto.pss.params.MplsPath;
import net.es.oscars.dto.pss.params.Policing;
import net.es.oscars.dto.pss.params.mx.*;
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
public class MxParamsAdapter {

    @Autowired
    private TopoService topoService;


    public MxParams params(Connection c, VlanJunction rvj) throws PSSException {
        Components cmp = c.getReserved().getCmp();

        Integer vcId = -1;
        Integer loopbackInt = null;
        for (CommandParam rpr : rvj.getCommandParams()) {
            if (rpr.getParamType().equals(CommandParamType.VC_ID)) {
                vcId = rpr.getResource();
            }
            if (rpr.getParamType().equals(CommandParamType.VPLS_LOOPBACK)) {
                loopbackInt = rpr.getResource();
            }
        }
        if (vcId == -1) {
            throw new PSSException("VC id not found!");
        }

        MxVpls vpls = MxVpls.builder()
                .description(c.getConnectionId())
                .serviceName(c.getConnectionId())
                .vcId(vcId)
                .policyName("oscars-policy-"+c.getConnectionId())
                .statsFilter("oscars-stats-"+c.getConnectionId())
                .serviceName("oscars-service-"+c.getConnectionId())
                .build();


        List<TaggedIfce> ifces = new ArrayList<>();

        for (VlanFixture rvf : c.getReserved().getCmp().getFixtures()) {
            if (rvf.getJunction().equals(rvj)) {
                Integer vlan = rvf.getVlan().getVlanId();

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
                TaggedIfce ti = TaggedIfce.builder()
                        .port(port)
                        .vlan(vlan)
                        .description("OSCARS:"+c.getConnectionId())
                        .build();
                ifces.add(ti);


            }
        }

        List<MxLsp> lsps = new ArrayList<>();

        List<MplsPath> paths = new ArrayList<>();

        List<MxQos> qos = new ArrayList<>();

        boolean isInPipes = false;
        for (VlanPipe p : cmp.getPipes()) {
            VlanJunction other_j = null;
            Integer mbps = null;
            List<EroHop> hops = null;

            if (p.getA().getDeviceUrn().equals(rvj.getDeviceUrn())) {
                hops = p.getAzERO();
                mbps = p.getAzBandwidth();
                other_j = p.getZ();
                isInPipes = true;
            } else if (p.getZ().getDeviceUrn().equals(rvj.getDeviceUrn())) {
                other_j = p.getA();
                mbps = p.getZaBandwidth();
                hops = p.getZaERO();
                isInPipes = true;
            }
            if (hops != null) {
                MxPipeResult pr = this.makePipe(other_j, hops, p, mbps, c);
                paths.add(pr.getPath());
                lsps.add(pr.getMxLsp());
                qos.add(pr.getQos());
            }
        }

        if (isInPipes) {
            if (loopbackInt == null) {
                throw new PSSException("no loopback reserved for "+rvj.getDeviceUrn());
            }
            IPv4Address address = new IPv4Address(loopbackInt);
            vpls.setLoopback(address.toString());
        }

        return MxParams.builder()
                .ifces(ifces)
                .qos(qos)
                .paths(paths)
                .lsps(lsps)
                .mxVpls(vpls)
                .build();
    }

    @Data
    private class MxPipeResult {
        private MxLsp mxLsp;
        private MplsPath path;
        private MxQos qos;
    }


    public MxPipeResult makePipe(VlanJunction otherJ, List<EroHop> hops, VlanPipe p, Integer mbps, Connection c) throws PSSException {
        List<MplsHop>  mplsHops = MiscHelper.mplsHops(hops, topoService);

        MplsPath path = MplsPath.builder()
                .hops(mplsHops)
                .name(c.getConnectionId()+"-PATH-"+p.getZ().getDeviceUrn())
                .build();

        Integer otherLoopbackInt = null;
        for (CommandParam rpr : otherJ.getCommandParams()) {
            if (rpr.getParamType().equals(CommandParamType.VPLS_LOOPBACK)) {
                otherLoopbackInt = rpr.getResource();
            }
        }
        if (otherLoopbackInt == null) {
            log.error("no loopback found for "+otherJ.getDeviceUrn());
            throw new PSSException("no loopback found for "+otherJ.getDeviceUrn());
        }
        IPv4Address otherLoopback = new IPv4Address(otherLoopbackInt);

        TopoUrn otherDevice = topoService.getTopoUrnMap().get(otherJ.getDeviceUrn());
        if (otherDevice == null || !otherDevice.getUrnType().equals(UrnType.DEVICE)) {
            throw new PSSException("invalid other device");
        }
        String otherAddr = otherDevice.getDevice().getIpv4Address();

        Lsp lsp = Lsp.builder()
                .holdPriority(5)
                .setupPriority(5)
                .metric(65000)
                .to(otherLoopback.toString())
                .pathName(path.getName())
                .name(c.getConnectionId()+"-LSP-"+p.getZ().getDeviceUrn())
                .build();

        // TODO: customize these...
        String filterName = "OSCARS-"+c.getConnectionId()+"-"+otherDevice.getUrn();

        MxQos qos = MxQos.builder()
                .createPolicer(true)
                .filterName(filterName)
                .forwarding(MxQosForwarding.EXPEDITED)
                .mbps(mbps)
                .policerName("OSCARS-"+c.getConnectionId()+"-"+otherDevice.getUrn())
                .policing(Policing.STRICT)
                .build();

        MxLsp mxLsp = MxLsp.builder()
                .lsp(lsp)
                .neighbor(otherAddr)
                .policeFilter(filterName)
                .build();

        MxPipeResult pr = new MxPipeResult();
        pr.setMxLsp(mxLsp);
        pr.setPath(path);
        pr.setQos(qos);
        return pr;

    }
}