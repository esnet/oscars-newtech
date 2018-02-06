package net.es.oscars.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.VerifyException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.Startup;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.cmd.*;
import net.es.oscars.pss.svc.PSSProxy;
import net.es.oscars.pss.svc.PssHealthChecker;
import net.es.oscars.topo.ent.Device;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;


@Slf4j
@Component
public class VerifyConfigCheck {

    @Autowired
    private Startup startup;

    @Autowired
    private PssHealthChecker checker;

    @Autowired
    private PSSProxy pssProxy;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processingLoop() {
        if (startup.isInStartup() || startup.isInShutdown()) {
            log.info("application in startup or shutdown; will perform verifications later");
            return;
        }
        ObjectMapper m = new ObjectMapper();

        Set<Device> verified = new HashSet<>();
        for (Device d: checker.getDevicesToVerify()) {
            try {
                VerifyRequest req = checker.verifyDeviceFacts(d);
                log.info("verifying device "+req.getDevice());
                VerifyResponse resp = pssProxy.verify(req);
                log.info(m.writerWithDefaultPrettyPrinter().writeValueAsString(resp.getPresent()));
//                log.info(resp.getConfig());
                verified.add(d);
            } catch (VerifyException | PSSException | JsonProcessingException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        checker.getDevicesToVerify().removeAll(verified);

    }

}