package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.*;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.*;
import net.es.oscars.pss.rancid.RancidArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class CommandRunner {
    private RouterConfigBuilder builder;
    private RancidRunner rancidRunner;
    private HealthService healthService;

    @Autowired
    public CommandRunner(RancidRunner rancidRunner, RouterConfigBuilder builder, HealthService healthService) {
        this.rancidRunner = rancidRunner;
        this.builder = builder;
        this.healthService = healthService;
    }

    public void run(CommandStatus status, Command command) {
        ConfigResult confRes;
        RancidArguments args;
        try {

            switch (command.getType()) {
                case CONFIG_STATUS:
                    break;
                case OPERATIONAL_STATUS:
                    break;
                case CONTROL_PLANE_STATUS:
                    ControlPlaneResult res = cplStatus(command.getDevice(), command.getModel());
                    status.setControlPlaneStatus(res.getStatus());
                    break;
                case BUILD:
                    status.setConfigStatus(ConfigStatus.NONE);
                    args = builder.build(command);
                    confRes = configure(args);
                    status.setConfigStatus(confRes.getStatus());
                    break;
                case DISMANTLE:
                    status.setConfigStatus(ConfigStatus.NONE);
                    args = builder.dismantle(command);
                    confRes = configure(args);
                    status.setConfigStatus(confRes.getStatus());
                    break;

            }
        } catch (UrnMappingException | ConfigException ex) {
            log.error("error", ex);
            status.setControlPlaneStatus(ControlPlaneStatus.ERROR);
        }
    }

    private ConfigResult configure(RancidArguments args) {

        ConfigResult result = ConfigResult.builder().build();

        try {
            rancidRunner.runRancid(args);
            result.setStatus(ConfigStatus.OK);

        } catch (IOException | InterruptedException | TimeoutException | ControlPlaneException ex) {
            log.error("Rancid error", ex);
            result.setStatus(ConfigStatus.ERROR);

        }
        return result;
    }

    private ControlPlaneResult cplStatus(String device, DeviceModel model)
            throws UrnMappingException {

        ControlPlaneResult result = ControlPlaneResult.builder().build();

        try {
            RancidArguments args = builder.controlPlaneCheck(device, model);
            rancidRunner.runRancid(args);
            healthService.getHealth().getDeviceStatus().put(device, ControlPlaneStatus.OK);
            result.setStatus(ControlPlaneStatus.OK);

        } catch (IOException | InterruptedException | TimeoutException | ControlPlaneException | ConfigException ex) {
            log.error("Rancid error", ex);
            healthService.getHealth().getDeviceStatus().put(device, ControlPlaneStatus.ERROR);
            result.setStatus(ControlPlaneStatus.ERROR);
        }
        return result;

    }


}
