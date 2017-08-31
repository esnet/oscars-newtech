package net.es.oscars.task;

import lombok.extern.slf4j.Slf4j;
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
import java.util.List;


@Slf4j
@Component
public class GenerateConfigs {

    @Autowired
    private ConnectionRepository connRepo;
    @Autowired
    private RouterCommandsRepository rcRepo;

    @Autowired
    private PSSAdapter pssAdapter;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processingLoop() {

        List<Connection> conns = connRepo.findAll();

        List<Connection> needConfigs = new ArrayList<>();
        for (Connection c : conns) {
            if (c.getPhase().equals(Phase.RESERVED)) {
                if (rcRepo.findByConnectionId(c.getConnectionId()).isEmpty()) {
                    needConfigs.add(c);
                }
            }
        }
        /*
        for (Connection c: needConfigs) {

            try {
                pssAdapter.generateConfig(c);
            } catch (PSSException e) {
                e.printStackTrace();
            }
        }
        */


    }

}