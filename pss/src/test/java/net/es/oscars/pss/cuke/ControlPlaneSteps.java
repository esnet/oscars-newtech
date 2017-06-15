package net.es.oscars.pss.cuke;

import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.ControlPlaneStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.beans.DeviceEntry;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.help.PssTestConfig;
import net.es.oscars.pss.svc.CommandQueuer;
import net.es.oscars.pss.svc.HealthService;
import org.apache.commons.lang3.StringUtils;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Category({UnitTests.class})
@Slf4j
public class ControlPlaneSteps extends CucumberSteps {
    @Autowired
    private CucumberWorld world;
    @Autowired
    private CommandQueuer queuer;

    @Autowired
    private PssTestConfig pssTestConfig;

    @Autowired
    private HealthService healthService;
    Map<DeviceEntry, String> entryCommands;
    Map<String, ControlPlaneStatus> statusMap;
    Set<String> commandIds;
    Set<String> waitingFor;

    @Then("^I will add the control plane test commands for \"([^\"]*)\" to the queue$")
    public void i_will_add_the_control_plane_test_commands_for_to_the_queue(String testcase) throws Throwable {
        String prefix = pssTestConfig.getCaseDirectory()+"/"+testcase;
        entryCommands = healthService.queueControlPlaneCheck(queuer, prefix+"/control-plane-check.json");

        statusMap = new HashMap<>();

        commandIds = new HashSet<>();
        waitingFor = new HashSet<>();

        entryCommands.entrySet().forEach(e -> {
            waitingFor.add(e.getKey().getDevice());
            commandIds.add(e.getValue());
        });
    }

    @Then("^I will wait up to (\\d+) ms for the control plane commands to complete$")
    public void i_will_wait_up_to_ms_for_the_control_plane_commands_to_complete(int millis) throws Throwable {
        int totalMs = 0;
        while (waitingFor.size() > 0 && totalMs < millis) {
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
            String timedOut = StringUtils.join(waitingFor, ", ");
            ControlPlaneException ex = new ControlPlaneException("timed out waiting for "+timedOut);
            world.add(ex);
            throw ex;
        }
    }

    @Then("^I have verified the control plane to all the devices$")
    public void i_have_verified_the_control_plane_to_all_the_devices() throws Throwable {
        List<String> notVerified = new ArrayList<>();
        for (String device : statusMap.keySet()) {
            ControlPlaneStatus st = statusMap.get(device);
            if (!st.equals(ControlPlaneStatus.OK)) {
                notVerified.add(device);
            }
        }
        if (!notVerified.isEmpty()) {
            String complaint = StringUtils.join(waitingFor, ", ");
            ControlPlaneException ex = new ControlPlaneException("could not verify "+complaint);
            world.add(ex);
            throw ex;
        }
    }

}

