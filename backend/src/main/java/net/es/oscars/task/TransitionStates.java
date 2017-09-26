package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.db.HeldRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.Held;
import net.es.oscars.resv.enums.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class TransitionStates {

    @Autowired
    private ConnectionRepository connRepo;
    @Autowired
    private Startup startup;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processingLoop() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            log.info("application in startup or shutdown; skipping state transitions");
            return;
        }

        List<Connection> conns = connRepo.findAll();
        List<Connection> deleteThese = new ArrayList<>();
        List<Connection> archiveThese = new ArrayList<>();

        for (Connection c : conns) {
            if (c.getPhase().equals(Phase.HELD)) {
                if (c.getHeld().getExpiration().isBefore(Instant.now())) {
                    log.info("will delete expired held connection: "+c.getConnectionId());
                    deleteThese.add(c);
                }
            }
            if (c.getPhase().equals(Phase.RESERVED)) {
                if (c.getReserved().getSchedule().getEnding().isBefore(Instant.now())) {
                    archiveThese.add(c);
                }
            }
        }
        connRepo.delete(deleteThese);

        archiveThese.forEach(c -> {
            c.setPhase(Phase.ARCHIVED);
            c.setReserved(null);
            connRepo.save(c);
        });



    }

}