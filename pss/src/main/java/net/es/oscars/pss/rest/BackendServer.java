package net.es.oscars.pss.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.GeneratedCommands;
import net.es.oscars.pss.beans.PssProfile;
import net.es.oscars.pss.prop.PssProps;
import net.es.oscars.rest.RestConfigurer;
import net.es.oscars.rest.RestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
@Slf4j
public class BackendServer implements BackendProxy {

    private PssProps pssProps;

    private RestTemplate restTemplate;


    @Autowired
    public BackendServer(PssProps pssProps, RestProperties restProperties, RestConfigurer restConfigurer) {

        try {
            this.restTemplate = new RestTemplate(restConfigurer.getRestConfig(restProperties));

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        this.pssProps = pssProps;
    }

    public GeneratedCommands commands(String connectionId, String device, String profileName) {
        String submitUrl = "/api/pss/generated/" + connectionId + "/" + device;
        PssProfile profile = PssProfile.find(pssProps, profileName);

        String restPath = profile.getBackendUrl() + submitUrl;
        log.info("calling " + restPath);
        return restTemplate.getForObject(restPath, GeneratedCommands.class);

    }

}