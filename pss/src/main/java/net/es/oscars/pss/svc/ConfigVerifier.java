package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.VerifyRequest;
import net.es.oscars.dto.pss.cmd.VerifyResponse;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.ConfigCacheEntry;
import net.es.oscars.pss.prop.VerifierProps;
import net.es.oscars.pss.rancid.RancidArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ConfigVerifier {
    private RancidRunner rancidRunner;
    private RouterConfigBuilder rcb;
    private VerifierProps verifierProps;
    private Map<String, ConfigCacheEntry> cache = new HashMap<>();

    @Autowired
    public ConfigVerifier(RancidRunner rancidRunner, VerifierProps verifierProps, RouterConfigBuilder rcb) {
        this.rancidRunner = rancidRunner;
        this.verifierProps = verifierProps;
        this.rcb = rcb;
    }

    public VerifyResponse verify(VerifyRequest verifyRequest) {
        VerifyResponse response = VerifyResponse.builder()
                .build();
        return response;
    }

}
