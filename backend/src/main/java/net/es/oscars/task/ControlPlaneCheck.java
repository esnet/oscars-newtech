package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandResponse;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.pss.svc.PSSProxy;
import net.es.oscars.pss.svc.PssHealthChecker;
import net.es.oscars.topo.ent.Device;
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
    private Startup startup;

    @Autowired
    private PssHealthChecker checker;

    @Autowired
    private PSSProxy pssProxy;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processingLoop() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            log.info("application in startup or shutdown; will perform health check later");
            return;
        }

        // first, pull status for previously submitted commands

        for (String commandId: waitingForStatus) {
            try {
                CommandStatus cs = pssProxy.status(commandId);
                gotStatus.put(cs.getDevice(), cs);
            } catch (PSSException ex) {
                log.error("error getting status for "+commandId, ex);
            }
        }
        for (String commandId : gotStatus.keySet()) {
            waitingForStatus.remove(commandId);
        }

        // now, submit new commands for the list of devices in the checker
        Set<Device> submittedOk = new HashSet<>();

        for (Device d: checker.getDevicesToCheck()) {
            Command command = Command.builder()
                    .device(d.getUrn())
                    .model(d.getModel())
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
            checker.getDevicesToCheck().remove(d);
        }


    }

}