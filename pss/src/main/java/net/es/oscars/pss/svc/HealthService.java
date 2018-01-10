package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.cp.ControlPlaneHealth;
import net.es.oscars.pss.beans.DeviceEntry;
import net.es.oscars.pss.beans.PssProfile;
import net.es.oscars.pss.prop.PssProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class HealthService {
    private ControlPlaneHealth health;

    @Autowired
    private PssProps props;

    public HealthService() {
        log.info("initialized health service");
        this.health = new ControlPlaneHealth();
        this.health.setDeviceStatus(new HashMap<>());
    }

    public ControlPlaneHealth getHealth() {
        return this.health;
    }



    public Map<DeviceEntry, String> queueControlPlaneCheck(CommandQueuer queuer) throws IOException {
        List<DeviceEntry> entries = new ArrayList<>();
        for (PssProfile pssProfile : props.getProfiles()) {
            if (pssProfile.getCheck().getPerform()) {
                entries.addAll(pssProfile.getCheck().getDevices());
            } else {
                log.info("not performing check for profile "+pssProfile.getProfile());
            }
        }


        Map<DeviceEntry, String> result = new HashMap<>();
        entries.forEach(e -> {
            log.info("adding check for "+e.getDevice());
            Command cmd = Command.builder()
                    .device(e.getDevice())
                    .model(e.getModel())
                    .type(CommandType.CONTROL_PLANE_STATUS)
                    .connectionId(null)
                    .refresh(false)
                    .ex(null)
                    .mx(null)
                    .alu(null)
                    .build();

            String commandId = queuer.newCommand(cmd);
            result.put(e, commandId);
            log.info("added a new command " + commandId);

        });
        return result;
    }
}
