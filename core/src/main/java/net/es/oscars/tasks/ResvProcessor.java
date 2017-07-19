package net.es.oscars.tasks;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.svc.PSSAdapter;
import net.es.oscars.pss.svc.PssResourceService;
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
public class ResvProcessor {

    @Autowired
    public ResvProcessor(ResvService resvService,
                         PSSAdapter pssAdapter,
                         ProcessingProperties processingProperties) {
        this.pssAdapter = pssAdapter;
        this.processingProperties = processingProperties;
        this.resvService = resvService;
    }

    private ResvService resvService;
    private ProcessingProperties processingProperties;
    private PSSAdapter pssAdapter;


    private boolean started = false;

    public void startup() {
        log.debug("started processing loop");
        this.started = true;
    }


    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processingLoop() {
        if (!started) {
            return;
        }

        // process aborting reservations
        resvService.ofResvState(ResvState.ABORTING).forEach(c -> {
                    log.info("detected connection being aborted" + c.getConnectionId());
                    resvService.abort(c);
                }
        );

        // time out expired HELD reservations
        Integer timeoutMs = processingProperties.getTimeoutHeldAfter() * 1000;
        resvService.ofHeldTimeout(timeoutMs).forEach(c -> {
                    log.info("reservation " + c.getConnectionId() + " timing out from HELD after " + timeoutMs + "ms");
                    resvService.timeout(c);
                }
        );

        // process committing reservations
        resvService.ofResvState(ResvState.COMMITTING).forEach(c -> {
                    log.info("detected connection being committed" + c.getConnectionId());
                    resvService.commit(c);
                }
        );

        // generate config for reservations
        resvService.ofProvState(ProvState.READY).forEach(c -> {
            // TODO: retries or something?
                    log.info("ready to generate config for " + c.getConnectionId());
                    try {
                        pssAdapter.generateConfig(c);
                        resvService.generated(c);
                    } catch (PSSException ex) {
                        log.error("PSS problem", ex);
                        resvService.provFailed(c);
                    }

                }
        );


        // process newly submitted reservations
        resvService.ofResvState(ResvState.SUBMITTED).forEach(c -> {
                    log.info("detected submitted connection " + c.getConnectionId());
                    try {
                        resvService.hold(c);
                    } catch (PSSException e) {
                        e.printStackTrace();
                        log.error("PSS Exception", e);

                    } catch (PCEException e) {
                        e.printStackTrace();
                        log.error("PCE Exception", e);
                    }
                }
        );


    }
}