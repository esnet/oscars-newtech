package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.pss.db.RouterCommandsRepository;
import net.es.oscars.pss.svc.PSSAdapter;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.Connection;
import net.es.oscars.resv.enums.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class GenerateConfigs {

    @Autowired
    private ConnectionRepository connRepo;
    @Autowired
    private RouterCommandsRepository rcRepo;
    @Autowired
    private Startup startup;

    @Autowired
    private PSSAdapter pssAdapter;

    private Map<String, Integer> attempts = new HashMap<>();

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processingLoop() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            log.info("application in startup or shutdown; skipping config generation");
            return;
        }

        List<Connection> conns = connRepo.findAll();

        List<Connection> needConfigs = new ArrayList<>();
        for (Connection c : conns) {
            if (c.getPhase().equals(Phase.RESERVED)) {
                if (rcRepo.findByConnectionId(c.getConnectionId()).isEmpty()) {
                    log.info("connection +"+c.getConnectionId()+" needs router configs to be generated");
                    needConfigs.add(c);
                }
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


    }

}