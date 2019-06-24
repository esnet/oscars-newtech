package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.util.DbAccess;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.pss.beans.QueueName;
import net.es.oscars.pss.db.RouterCommandsRepository;
import net.es.oscars.pss.svc.ConfigGenService;
import net.es.oscars.pss.svc.PSSQueuer;
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
public class PssOnSchedule {
    @Autowired
    private PSSQueuer pssQueuer;

    @Autowired
    private ConfigGenService cgs;


    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private RouterCommandsRepository rcRepo;

    @Autowired
    private Startup startup;

    @Autowired
    private DbAccess dbAccess;

    private Map<String, Integer> attempts = new HashMap<>();

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void genConfigsBuildDismantle() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            // log.info("application in startup or shutdown; skipping state transitions");
            return;
        }

        ReentrantLock connLock = dbAccess.getConnLock();

        List<Connection> conns = connRepo.findByPhase(Phase.RESERVED);

        List<Connection> needConfigs = new ArrayList<>();
        for (Connection c : conns) {
            if (rcRepo.findByConnectionId(c.getConnectionId()).isEmpty()) {
                log.info("connection " + c.getConnectionId() + " needs router configs to be generated");
                needConfigs.add(c);
            }
        }

        for (Connection c : needConfigs) {
            Integer tried = 0;
            Integer maxTries = 3;
            if (attempts.containsKey(c.getConnectionId())) {
                tried = attempts.get(c.getConnectionId());
            }
            if (tried < maxTries) {
                tried = tried + 1;
                try {
                    cgs.generateConfig(c);
                    attempts.remove(c.getConnectionId());
                } catch (PSSException e) {
                    attempts.put(c.getConnectionId(), tried);
                    e.printStackTrace();
                }

            } else if (tried.equals(maxTries)) {
                log.error(" stopping trying to generate config for " + c.getConnectionId());
                attempts.put(c.getConnectionId(), maxTries + 1);
            }

        }


        Set<Connection> shouldBeBuilt = new HashSet<>();
        Set<Connection> shouldBeDismantled = new HashSet<>();
        // log.debug("got connection lock");

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
                        boolean shouldBuild = true;
                        if (attempts.containsKey(c.getConnectionId()) ) {
                            if (attempts.get(c.getConnectionId()) >= 3)  {
                                shouldBuild = false;
                            }


                        }
                        if (shouldBuild) {
                            shouldBeBuilt.add(c);
                        }
                    }
                }
            }
        }
        pssQueuer.clear(QueueName.DONE);

        // do the PSS work
        for (Connection c : shouldBeBuilt) {
            pssQueuer.add(CommandType.BUILD, c.getConnectionId(), State.ACTIVE);
        }
        for (Connection c : shouldBeDismantled) {
            pssQueuer.add(CommandType.DISMANTLE, c.getConnectionId(), State.FINISHED);
        }

        // run the PSS queue
        pssQueuer.process();

    }

}