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



}
