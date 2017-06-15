package net.es.oscars.pss.rtr;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.ControlPlaneStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.AbstractPssTest;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.beans.DeviceEntry;
import net.es.oscars.pss.ctg.ControlPlaneTests;
import net.es.oscars.pss.ctg.RouterTests;
import net.es.oscars.pss.help.PssTestConfig;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.svc.CommandQueuer;
import net.es.oscars.pss.svc.HealthService;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

@Slf4j
public class ControlPlaneTest {

    @Autowired
    private CommandQueuer queuer;

    @Autowired
    private RancidProps rancidProps;

    @Autowired
    private PssTestConfig pssTestConfig;

    @Autowired
    private HealthService healthService;

    @Before
    public void before() throws InterruptedException {
        System.out.println("==============================================================================");
        System.out.println("Ready to run control plane tests! These WILL attempt to contact routers.");
        System.out.println("Make sure you have configured test.properties correctly. ");
        System.out.println("Starting in 3 seconds. Ctrl-C to abort.");
        System.out.println("==============================================================================");
        Thread.sleep(3000);

        rancidProps.setExecute(true);
    }

    @Category({RouterTests.class, ControlPlaneTests.class})
    public void basicTest() throws NoSuchElementException, ControlPlaneException, InterruptedException, IOException {
        log.info("starting control plane test");
        String prefix = pssTestConfig.getCaseDirectory();

        Map<DeviceEntry, String> entryCommands = healthService.queueControlPlaneCheck(queuer, prefix+"/control-plane-check.json");

        Map<String, ControlPlaneStatus> statusMap = new HashMap<>();

        Set<String> commandIds = new HashSet<>();
        Set<String> waitingFor = new HashSet<>();

        entryCommands.entrySet().forEach(e -> {
            waitingFor.add(e.getKey().getDevice());
            commandIds.add(e.getValue());

        });

        int totalMs = 0;
        while (waitingFor.size() > 0 && totalMs < 60000) {
            Thread.sleep(2000);
            totalMs += 2000;
            waitingFor.clear();
            for (String commandId : commandIds) {
                log.debug("checking status for routerConfig " + commandId);
                CommandStatus status = queuer.getStatus(commandId).orElseThrow(NoSuchElementException::new);
                if (status.getLifecycleStatus().equals(LifecycleStatus.DONE)) {
                    ControlPlaneStatus st = status.getControlPlaneStatus();
                    log.debug("control plane status for " + status.getDevice() + " : " + st);
                    statusMap.put(status.getDevice(), st);
                } else {
                    log.debug(status.getDevice() + " still waiting for completion");
                    waitingFor.add(status.getDevice());
                }
            }
        }
        if (waitingFor.size() > 0) {
            log.error("timed out waiting for some devices");
            throw new ControlPlaneException("timed out");

        }


        log.debug("done collecting statuses");

        for (String device : statusMap.keySet()) {
            ControlPlaneStatus st = statusMap.get(device);
            if (!st.equals(ControlPlaneStatus.OK)) {
                throw new ControlPlaneException("Could not verify " + device);
            }

        }

    }

}
