package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.NsiException;
import net.es.oscars.app.props.TopoProperties;
import net.es.oscars.nsi.svc.NsiPopulator;
import net.es.oscars.topo.beans.TopoException;
import net.es.oscars.topo.beans.VersionDelta;
import net.es.oscars.topo.db.VersionRepository;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.pop.ConsistencyException;
import net.es.oscars.topo.pop.TopoPopulator;
import net.es.oscars.topo.svc.ConsistencyService;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;


@Slf4j
@Component
public class RefreshNsiConfig {

    @Value("${nsi.peerings}")
    private File peeringsFile;
    @Value("${nsi.filter}")
    private File filterFile;

    private Instant lastLoad = Instant.MIN;

    @Autowired
    private NsiPopulator nsiPopulator;
    @Autowired
    private Startup startup;



    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processingLoop() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            return;
        }
        try {
            Instant peerLastMod = Instant.ofEpochMilli(peeringsFile.lastModified());
            Instant filtLastMod = Instant.ofEpochMilli(filterFile.lastModified());
            if (peerLastMod.isAfter(lastLoad) || filtLastMod.isAfter(lastLoad)) {
                log.info("reloading NSI peering / filter files");
                nsiPopulator.loadNsiConfig(peeringsFile, filterFile);
            }
        } catch (IOException | NsiException ex) {
            log.error(ex.getMessage());
        } finally {
            this.lastLoad = Instant.now();
        }

    }

}