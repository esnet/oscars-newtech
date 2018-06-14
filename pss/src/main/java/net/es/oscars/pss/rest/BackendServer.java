package net.es.oscars.pss.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
@Slf4j
public class BackendServer implements BackendProxy {

    private String backendUrl;

    private RestTemplate restTemplate;

    @Autowired
    public BackendServer(RestTemplate restTemplate, @Value("${backend.url}") String backendUrl) {

        this.restTemplate = restTemplate;
        this.backendUrl = backendUrl;
        log.info("backend server URL: " + this.backendUrl);
    }

    public GeneratedCommands commands(String connectionId, String device) {
        String submitUrl = "/api/pss/generated/" + connectionId + "/" + device;
        String restPath = backendUrl + submitUrl;
        log.info("calling " + restPath);
        return restTemplate.getForObject(restPath, GeneratedCommands.class);

    }


}