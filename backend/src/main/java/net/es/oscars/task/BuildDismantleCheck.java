package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.util.DbAccess;
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
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
@Component
public class BuildDismantleCheck {
    @Autowired
    private PSSAdapter pssAdapter;

    @Autowired
    private ConnectionRepository connRepo;
    @Autowired
    private Startup startup;

    @Autowired
    private DbAccess dbAccess;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void buildDismantleOnSchedule() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            // log.info("application in startup or shutdown; skipping state transitions");
            return;
        }

        ReentrantLock connLock = dbAccess.getConnLock();
        boolean gotLock = connLock.tryLock();
        if (gotLock) {
            try {
                // log.debug("got connection lock");

                Set<Connection> shouldBeBuilt = new HashSet<>();
                Set<Connection> shouldBeDismantled = new HashSet<>();
                List<Connection> conns = connRepo.findByPhase(Phase.RESERVED);
                for (Connection c : conns) {
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

                if (shouldBeBuilt.size() == 0 && shouldBeDismantled.size() == 0) {
                    return;
                }

                for (Connection c : shouldBeBuilt) {
                    try {
                        State s = this.pssAdapter.build(c);
                        c.setState(s);
                    } catch (PSSException ex) {
                        c.setState(State.FAILED);
                        log.error(ex.getMessage(), ex);
                    }
                    connRepo.saveAndFlush(c);
                }
                for (Connection c : shouldBeDismantled) {
                    try {
                        State s = this.pssAdapter.dismantle(c);
                        c.setState(s);
                    } catch (PSSException ex) {
                        c.setState(State.FAILED);
                        log.error(ex.getMessage(), ex);
                    }
                    connRepo.saveAndFlush(c);
                }

            } finally {
                // log.debug("unlocking connections");
                connLock.unlock();
            }
        } else {
            log.debug("unable to lock; waiting for next run ");
        }


    }

}