package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.props.TopoProperties;
import net.es.oscars.topo.beans.TopoException;
import net.es.oscars.topo.beans.VersionDelta;
import net.es.oscars.topo.db.VersionRepository;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.svc.ConsistencySvc;
import net.es.oscars.topo.pop.ConsistencyException;
import net.es.oscars.topo.pop.TopoPopulator;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;


@Slf4j
@Component
public class RefreshTopology {

    @Autowired
    private Startup startup;

    @Autowired
    private TopoService topoService;
    @Autowired
    private TopoPopulator topoPopulator;

    @Autowired
    private TopoProperties topoProperties;

    @Autowired
    private ConsistencySvc consistencySvc;

    @Autowired
    private VersionRepository versionRepo;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void processingLoop() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            log.info("application in startup or shutdown; will start refreshing topology later");
            return;
        }
        try {
            Optional<Version> maybeV = topoService.currentVersion();
            if (maybeV.isPresent()) {
                String devicesFilename = "./config/topo/" + topoProperties.getPrefix() + "-devices.json";
                File devFile = new File(devicesFilename);
                Instant devLastMod = Instant.ofEpochMilli(devFile.lastModified());

                String adjciesFilename = "./config/topo/" + topoProperties.getPrefix() + "-adjcies.json";
                File adjFile = new File(adjciesFilename);
                Instant adjLastMod = Instant.ofEpochMilli(adjFile.lastModified());

                Instant latest = devLastMod;
                if (adjLastMod.isAfter(devLastMod)) {
                    latest = adjLastMod;
                }
                Version v = maybeV.get();

                if (latest.isAfter(v.getUpdated())) {
                    log.info("topology possibly modified since last update");
                    VersionDelta vd = topoPopulator.refreshTopology();
                    if (!vd.isChanged()) {
                        v.setUpdated(Instant.now());
                        versionRepo.save(v);
                    } else {
                        consistencySvc.checkConsistency(vd);
                        topoService.updateTopo();
                    }

                }


            } else {
                log.error("no current version for the topology!");
            }
        } catch (TopoException ex) {
            log.error("Topology import error!", ex);
        } catch (IOException ex) {
            log.error("Topology load I/O error!", ex);
        } catch (ConsistencyException ex) {
            log.error("Consistency error!", ex);
        }

    }

}