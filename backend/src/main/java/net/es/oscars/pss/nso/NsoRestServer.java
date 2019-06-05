package net.es.oscars.pss.nso;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.props.NSOProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.HashMap;

@Slf4j
@Component
public class NsoRestServer {

    private NSOProperties props;

    private RestTemplate restTemplate;

    @Autowired
    public NsoRestServer(NSOProperties props) {

        this.props = props;
        try {
            this.restTemplate = new RestTemplate();
            restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(props.getUsername(), props.getPassword()));

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("NSO server URL: " + props.getUrl());
    }


    public void getOscars() {
        String req = "running/oscars?deep";
        String restPath = props.getUrl() + req;
        ResponseEntity<String> response
                = restTemplate.getForEntity(restPath, String.class);

        log.info(response.getBody());
    }

    public String postOscars(String xml) {
        log.info("\n"+xml);
        String req = "running";
        String restPath = props.getUrl() + req;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application","vnd.yang.data+xml"));


        HttpEntity<String> request = new HttpEntity<>(xml, headers);


        ResponseEntity<String> response
                = restTemplate.postForEntity(restPath, request, String.class);
        return response.getBody();
    }

    public void deleteOscars(String path) {
        String req = "running/oscars/"+path;
        String restPath = props.getUrl() + req;

        restTemplate.delete(restPath, new HashMap<>());

    }

}
