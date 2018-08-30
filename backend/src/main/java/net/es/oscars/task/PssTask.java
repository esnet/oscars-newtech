package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.util.DbAccess;
import net.es.oscars.pss.db.RouterCommandsRepository;
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
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
@Component
public class PssTask {
    @Autowired
    private PSSAdapter pssAdapter;

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private RouterCommandsRepository rcRepo;

    @Autowired
    private Startup startup;

    @Autowired
    private DbAccess dbAccess;

    private Map<String, Integer> attempts = new HashMap<>();

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void genConfigsBuildDismantle() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            // log.info("application in startup or shutdown; skipping state transitions");
            return;
        }

        ReentrantLock connLock = dbAccess.getConnLock();

        connLock.lock();
        try {
            // log.debug("locking connections");

            List<Connection> conns = connRepo.findByPhase(Phase.RESERVED);

            List<Connection> needConfigs = new ArrayList<>();
            for (Connection c : conns) {
                if (rcRepo.findByConnectionId(c.getConnectionId()).isEmpty()) {
                    log.info("connection "+c.getConnectionId()+" needs router configs to be generated");
                    needConfigs.add(c);
                }
            }

            for (Connection c: needConfigs) {
                Integer tried = 0;
                Integer maxTries = 3;
                if (attempts.containsKey(c.getConnectionId())) {
                    tried = attempts.get(c.getConnectionId());
                }
                if (tried < maxTries) {
                    tried = tried + 1;
                    try {
                        pssAdapter.generateConfig(c);
                    } catch (PSSException e) {
                        attempts.put(c.getConnectionId(), tried);
                        e.printStackTrace();
                    }

                } else if (tried.equals(maxTries)){
                    log.error(" stopping trying to generate config for "+c.getConnectionId());
                    attempts.put(c.getConnectionId(), maxTries + 1);
                }

            }

        } finally {
            // log.debug("unlocking connections");
            connLock.unlock();
        }


        Set<Connection> shouldBeBuilt = new HashSet<>();
        Set<Connection> shouldBeDismantled = new HashSet<>();

        boolean gotLock = connLock.tryLock();
        if (gotLock) {
            try {
                // log.debug("got connection lock");

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
            } finally {
                // release lock now that we read everything we needed

                // log.debug("unlocking connections");
                connLock.unlock();
            }

            // do the PSS work

            if (shouldBeBuilt.size() == 0 && shouldBeDismantled.size() == 0) {
                return;
            }
            Map<String, State> newStates = new HashMap<>();

            for (Connection c : shouldBeBuilt) {
                try {
                    State s = this.pssAdapter.build(c);
                    newStates.put(c.getConnectionId(), s);
                } catch (PSSException ex) {
                    newStates.put(c.getConnectionId(), State.FAILED);
                    log.error(ex.getMessage(), ex);
                }
            }
            for (Connection c : shouldBeDismantled) {
                try {
                    State s = this.pssAdapter.dismantle(c);
                    newStates.put(c.getConnectionId(), s);
                } catch (PSSException ex) {
                    newStates.put(c.getConnectionId(), State.FAILED);
                    log.error(ex.getMessage(), ex);
                }
            }

            // lock for the updates
            connLock.lock();
            try {
                List<Connection> saveThese = new ArrayList<>();
                for (String connId : newStates.keySet()) {
                    Optional<Connection> maybeConn = connRepo.findByConnectionId(connId);
                    if (maybeConn.isPresent()) {
                        Connection c = maybeConn.get();
                        c.setState(newStates.get(connId));
                        saveThese.add(c);
                    } else {
                        log.error("Could not find connection! "+connId);
                    }
                }

                connRepo.save(saveThese);
                connRepo.flush();
            } finally {
                // log.debug("unlocking connections");
                connLock.unlock();
            }
        } else {
            log.debug("unable to lock; waiting for next run ");
        }


    }

}