package net.es.oscars.pss.nso;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.props.NSOProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
        String req = "running/oscars";
        String restPath = props.getUrl() + req;
        ResponseEntity<String> response
                = restTemplate.getForEntity(restPath, String.class);

        log.info(response.getBody());

    }

}
