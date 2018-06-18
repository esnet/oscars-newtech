package net.es.oscars.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestBean {

    private RestProperties restProperties;

    @Autowired
    public RestBean(RestProperties restProperties) {
        this.restProperties = restProperties;

    }

    @Bean
    public RestTemplate rest() throws Exception {
        return new RestTemplateBuilder().build(restProperties);
    }
}
