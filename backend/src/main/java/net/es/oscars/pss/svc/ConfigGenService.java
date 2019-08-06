package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.cmd.*;
import net.es.oscars.pss.params.alu.AluParams;
import net.es.oscars.pss.params.mx.MxParams;
import net.es.oscars.pss.db.RouterCommandsRepository;
import net.es.oscars.pss.ent.RouterCommands;
import net.es.oscars.pss.equip.*;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ConfigGenService {
    @Autowired
    private RouterCommandsRepository rcr;
    @Autowired
    private AluCommandGenerator acg;
    @Autowired
    private AluParamsAdapter apa;
    @Autowired
    private MxCommandGenerator mcg;
    @Autowired
    private MxParamsAdapter mpa;

    @Autowired
    private ExCommandGenerator ecg;

    @Autowired
    private TopoService topoSvc;


    public void generateConfig(Connection conn) throws PSSException {
        /*
        try {
            log.info("sleeping a bit to simulate slow config gen");
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        log.info("generating config for " + conn.getConnectionId());
        for (VlanJunction j : conn.getReserved().getCmp().getJunctions()) {
            Device d = topoSvc.getDeviceRepo().findByUrn(j.getDeviceUrn()).orElseThrow(PSSException::new);
            String build = null;
            String dismantle = null;
            String opStatus = null;
            switch (d.getModel()) {
                case ALCATEL_SR7750:
                    AluParams aluParams = apa.params(conn, j);
                    build = acg.build(aluParams);
                    dismantle = acg.dismantle(aluParams);
                    opStatus = acg.show(aluParams);
                    break;
                case JUNIPER_MX:
                    MxParams mxParams = mpa.params(conn, j);
                    build = mcg.build(mxParams);
                    dismantle = mcg.dismantle(mxParams);
                    opStatus = mcg.show(mxParams);
                    break;
                case JUNIPER_EX:
                    break;
            }
            RouterCommands rcb = RouterCommands.builder()
                    .connectionId(conn.getConnectionId())
                    .deviceUrn(j.getDeviceUrn())
                    .contents(build)
                    .type(CommandType.BUILD)
                    .build();
            rcr.save(rcb);
            RouterCommands rcd = RouterCommands.builder()
                    .connectionId(conn.getConnectionId())
                    .deviceUrn(j.getDeviceUrn())
                    .contents(dismantle)
                    .type(CommandType.DISMANTLE)
                    .build();
            rcr.save(rcd);
            RouterCommands rcs = RouterCommands.builder()
                    .connectionId(conn.getConnectionId())
                    .deviceUrn(j.getDeviceUrn())
                    .contents(opStatus)
                    .type(CommandType.OPERATIONAL_STATUS)
                    .build();
            rcr.save(rcs);
        }

        log.info("generated config for " + conn.getConnectionId());
    }


}