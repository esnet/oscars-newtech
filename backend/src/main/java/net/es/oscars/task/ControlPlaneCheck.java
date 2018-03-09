package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandResponse;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.st.ControlPlaneStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.svc.PSSProxy;
import net.es.oscars.pss.svc.PssHealthChecker;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Slf4j
@Component
public class ControlPlaneCheck {

    private Set<String> waitingForStatus = new HashSet<>();
    private Map<String, CommandStatus> gotStatus = new HashMap<>();

    @Autowired
    private TopoService topoService;

    @Autowired
    private Startup startup;

    @Autowired
    private PssHealthChecker checker;

    @Autowired
    private PSSProxy pssProxy;

    @Autowired
    private PssProperties pssProperties;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processingLoop() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            log.info("application in startup or shutdown; will perform health check later");
            return;
        }

        // first, pull the status for previously submitted commands
        for (String commandId: waitingForStatus) {
            try {
                CommandStatus cs = pssProxy.status(commandId);
                if (cs.getLifecycleStatus().equals(LifecycleStatus.DONE)) {
                    log.debug("done with command: " + commandId);
                    gotStatus.put(commandId, cs);
                }
                TopoUrn urn = topoService.getTopoUrnMap().get(cs.getDevice());
                Device d = urn.getDevice();
                checker.getStatuses().put(cs.getDevice(), cs);
                if (!cs.getControlPlaneStatus().equals(ControlPlaneStatus.OK)) {
                    Integer attempts = checker.getCheckAttempts().get(d);
                    if (attempts <= pssProperties.getControlPlaneCheckMaxTries()) {
                        log.info("retrying a failed control plane check, attempt # "+attempts+" for "+d.getUrn());
                        checker.getDevicesToCheck().add(d);
                        checker.getCheckAttempts().put(d, attempts + 1);
                    }
                }

            } catch (PSSException ex) {
                log.error("error getting status for "+commandId, ex);
            }
        }
        for (String commandId: gotStatus.keySet()) {
            waitingForStatus.remove(commandId);
        }

        // now, submit new commands for the list of devices in the checker
        Set<Device> submittedOk = new HashSet<>();

        for (Device d: checker.getDevicesToCheck()) {

            Command command = Command.builder()
                    .device(d.getUrn())
                    .model(d.getModel())
                    .profile(pssProperties.getProfile())
                    .type(CommandType.CONTROL_PLANE_STATUS)
                    .build();
            try {
                CommandResponse cr = pssProxy.submitCommand(command);
                waitingForStatus.add(cr.getCommandId());
                submittedOk.add(d);
            } catch (PSSException ex) {
                log.error("error submitting for "+d.getUrn(), ex);
            }
        }
        for (Device d: submittedOk) {
            log.debug("submitted a cp check for: "+d.getUrn());
            checker.getDevicesToCheck().remove(d);
        }


    }

}