package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.dto.pss.params.MplsPath;
import net.es.oscars.dto.pss.params.mx.*;
import net.es.oscars.resv.ent.CommandParam;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.VlanFixture;
import net.es.oscars.resv.ent.VlanJunction;
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
public class MxParamsAdapter {

    @Autowired
    private TopoService topoService;

    @Autowired
    private PssProperties pssProperties;

    public MxParams params(Connection c, VlanJunction rvj) throws PSSException {
        Integer vcId = -1;
        for (CommandParam rpr : rvj.getCommandParams()) {
            if (rpr.getParamType().equals(CommandParamType.VC_ID)) {
                vcId = rpr.getResource();
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


        return MxParams.builder()
                .ifces(ifces)
                .qos(qos)
                .paths(paths)
                .lsps(lsps)
                .mxVpls(vpls)
                .build();
    }


}