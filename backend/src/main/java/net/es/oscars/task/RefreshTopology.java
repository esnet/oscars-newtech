package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.topo.beans.TopoException;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.svc.ConsistencyService;
import net.es.oscars.topo.pop.ConsistencyException;
import net.es.oscars.topo.pop.TopoPopulator;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


@Slf4j
@Component
public class RefreshTopology {

    @Autowired
    private Startup startup;

    @Autowired
    private TopoPopulator topoPopulator;

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processingLoop() {
        if (startup.isInStartup() || startup.isInShutdown()) {
//            log.info("application in startup or shutdown; will start refreshing topology later");
            return;
        }
        try {
            topoPopulator.refresh(true);

        } catch (TopoException ex) {
            log.error("Topology import error!", ex);
        } catch (IOException ex) {
            log.error("I/O error!", ex);
        } catch (ConsistencyException ex) {
            log.error("Consistency error!", ex);
        }

    }

}