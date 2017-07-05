package net.es.oscars.tasks;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.svc.PSSAdapter;
import net.es.oscars.resv.svc.PickedVlansService;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;
import net.es.oscars.tasks.prop.ProcessingProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Component
public class ClearPicked {

    @Autowired
    public ClearPicked(PickedVlansService pickedVlansService) {
        this.pickedVlansService = pickedVlansService;
    }
    private PickedVlansService pickedVlansService;

    private boolean started = false;

    public void startup() {
        log.debug("startign check for outdated picked vlans...");
        this.started = true;
    }


    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processingLoop() {
        if (!started) {
            return;
        }
        pickedVlansService.deleteOutdated();

    }
}