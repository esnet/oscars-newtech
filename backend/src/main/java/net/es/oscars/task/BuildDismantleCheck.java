package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.pss.svc.PSSAdapter;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.ent.Schedule;
import net.es.oscars.resv.enums.BuildMode;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Component
public class BuildDismantleCheck {
    @Autowired
    private PSSAdapter pssAdapter;

    @Autowired
    private ConnectionRepository connRepo;
    @Autowired
    private Startup startup;


    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void buildDismantleOnSchedule() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            log.info("application in startup or shutdown; skipping state transitions");
            return;
        }

        Set<Connection> shouldBeBuilt = new HashSet<>();
        Set<Connection> shouldBeDismantled = new HashSet<>();
        List<Connection> conns = connRepo.findAll();
        for (Connection c : conns) {
            if (c.getPhase().equals(Phase.RESERVED)) {
                Schedule s = c.getReserved().getSchedule();
                // this has already ended, so if active it needs to be added to the dismantle list
                if (s.getEnding().isBefore(Instant.now())) {
                    if (c.getState().equals(State.ACTIVE)) {
                        shouldBeDismantled.add(c);
                    }

                } else if (s.getBeginning().isBefore(Instant.now())) {
                    // we are past the beginning, so we need to set it up if
                    // a. it is not in manual mode
                    // b. AND it is not already set up or failed
                    if (c.getMode().equals(BuildMode.AUTOMATIC)) {
                        if (c.getState().equals(State.WAITING)) {
                            shouldBeBuilt.add(c);
                        }
                    }
                }
            }
        }
        for (Connection c: shouldBeBuilt) {
            try {
                State s = this.pssAdapter.build(c);
                c.setState(s);
            } catch (PSSException ex) {
                c.setState(State.FAILED);
                log.error(ex.getMessage(), ex);
            }
            connRepo.save(c);
        }
        for (Connection c: shouldBeDismantled) {
            try {
                State s = this.pssAdapter.dismantle(c);
                c.setState(s);
            } catch (PSSException ex) {
                c.setState(State.FAILED);
                log.error(ex.getMessage(), ex);
            }
            connRepo.save(c);
        }

    }

}