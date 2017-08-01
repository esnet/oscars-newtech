package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.db.HeldRepository;
import net.es.oscars.resv.ent.Held;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;


@Slf4j
@Component
public class ClearHeld {

    @Autowired
    private HeldRepository heldRepo;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processingLoop() {
        List<Held> held = heldRepo.findAll();

        for (Held h : held) {
            if (h.getExpiration().isBefore(Instant.now())) {
                log.info("deleting expired held resources: "+h.getConnectionId());
                heldRepo.delete(h);
            }
        }

    }
}