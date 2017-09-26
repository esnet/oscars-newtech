package net.es.oscars.topo.pop;

import lombok.extern.slf4j.Slf4j;

import net.es.oscars.app.StartupComponent;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.topo.beans.TopoException;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Component
@Transactional
public class ConsistencyChecker implements StartupComponent {
    private PortAdjcyRepository adjcyRepo;

    private DeviceRepository deviceRepo;

    private UIPopulator ui;

    private TopoService ts;

    @Autowired
    public ConsistencyChecker(PortAdjcyRepository adjcyRepo,
                              DeviceRepository deviceRepo,
                              UIPopulator ui,
                              TopoService ts) {
        this.adjcyRepo = adjcyRepo;
        this.deviceRepo = deviceRepo;
        this.ui = ui;
        this.ts = ts;
    }

    // TODO: check version


    public void startup() throws StartupException {
        try {
            this.checkConsistency();

            ts.updateTopo();
        } catch (ConsistencyException | TopoException ex) {
            log.error(ex.getMessage());
            throw new StartupException(ex.getMessage());
        }
    }

    public void checkConsistency() throws ConsistencyException {
        log.info("checking data consistency..");

    }
}
