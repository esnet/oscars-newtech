package net.es.oscars.pss.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.*;
import net.es.oscars.pss.beans.PssProfile;
import net.es.oscars.pss.prop.PssProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
@Slf4j
public class BackendServer implements BackendProxy {

    private PssProps pssProps;

    private RestTemplate restTemplate;

    @Autowired
    public BackendServer(RestTemplate restTemplate, PssProps pssProps) {
        this.pssProps = pssProps;

        this.restTemplate = restTemplate;
    }

    public GeneratedCommands commands(String connectionId, String device, String profileName) {
        String submitUrl = "/api/pss/generated/" + connectionId + "/" + device;
        PssProfile profile = PssProfile.find(pssProps, profileName);

        String restPath = profile.getBackendUrl()  + submitUrl;
        log.info("calling " + restPath);
        return restTemplate.getForObject(restPath, GeneratedCommands.class);

    }


}