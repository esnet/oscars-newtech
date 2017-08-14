package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
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
public class ClearHeld {

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private HeldRepository heldRepo;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processingLoop() {
        List<Held> held = heldRepo.findAll();


        List<Connection> conns = connRepo.findAll();
        List<Connection> deleteThese = new ArrayList<>();
        for (Connection c : conns) {
            if (c.getPhase().equals(Phase.HELD)) {
                if (c.getHeld().getExpiration().isBefore(Instant.now())) {
                    log.info("will delete connection with expired held resources: "+c.getConnectionId());
                    deleteThese.add(c);
                }
            }
        }
        connRepo.delete(deleteThese);

        for (Held h : held) {
            if (h.getExpiration().isBefore(Instant.now())) {
                log.info("will delete expired held resources: "+h.getConnectionId());
                heldRepo.delete(h);
            }
        }


    }

}