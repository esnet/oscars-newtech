package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.nso.NsoRestServer;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.CommandParam;
import net.es.oscars.resv.ent.Components;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.resv.enums.CommandParamIntent;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.enums.CommandParamType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class NSOStatusCheck {

    @Autowired
    private Startup startup;

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private NsoRestServer nsoRestServer;

    @Autowired
    private TopoService topoSvc;

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processingLoop() {
        if (startup.isInStartup() || startup.isInShutdown()) {
//            log.info("application in startup or shutdown; will perform verifications later");
            return;
        }

        for (Connection c : connRepo.findByPhase(Phase.RESERVED)) {
            log.info("NSO status for " + c.getConnectionId());
            if (c.getState().equals(State.ACTIVE)) {
                Components cmp = c.getReserved().getCmp();
                for (VlanJunction rvj : cmp.getJunctions()) {
                    Optional<Device> maybeD = topoSvc.getDeviceRepo().findByUrn(rvj.getDeviceUrn());
                    if (!maybeD.isPresent()) {
                        log.error("could not find device " + rvj.getDeviceUrn());
                        continue;
                    }
                    Device d = maybeD.get();

                    Integer vcId = null;
                    List<Integer> sdpIds = new ArrayList<>();

                    for (CommandParam rpr : rvj.getCommandParams()) {
                        if (rpr.getParamType().equals(CommandParamType.VC_ID)) {
                            if (rpr.getIntent().equals(CommandParamIntent.PRIMARY)) {
                                vcId = rpr.getResource();
                            }
                        } else if (rpr.getParamType().equals(CommandParamType.ALU_SDP_ID)) {
                            sdpIds.add(rpr.getResource());
                        }
                    }
                    if (vcId != null) {
                        if (d.getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                            nsoRestServer.aluServiceStatus(rvj.getDeviceUrn(), vcId, sdpIds);
                        }
                    } else {
                        log.error("could not find vcid for " + rvj.getDeviceUrn() + " conn " + c.getConnectionId());
                    }
                }
            }
        }
    }

}