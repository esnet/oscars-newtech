package net.es.oscars.migration;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.db.RouterCommandsRepository;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@Slf4j
@Transactional
public class MigrationEngine {
    @Autowired
    protected ConnectionRepository connRepo;
    @Autowired
    protected RouterCommandsRepository rcRepo;
    @Autowired
    protected ConnService connSvc;
    @Autowired
    protected TopoService topoService;

    public void runEngine() throws Exception {

    }

}
