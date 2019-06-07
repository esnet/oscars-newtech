package net.es.oscars.pss.nso;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.props.NSOProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;

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

    public void aluServiceStatus(String device, Integer svcId, List<Integer> sdpIds) {
        String req = "operational/devices/device/"+device+"/live-status/service/id/"+svcId;
        log.info(req);
        String restPath = props.getUrl() + req;
        ResponseEntity<String> response
                = restTemplate.getForEntity(restPath, String.class);

        log.info(response.getBody());

        req = "operational/devices/device/"+device+"/live-status/service/id/"+svcId+"/fdb";
        log.info(req);
        restPath = props.getUrl() + req;
        response = restTemplate.getForEntity(restPath, String.class);

        log.info(response.getBody());



        req = "operational/devices/device/"+device+"/live-status/service/sap";
        log.info(req);
        restPath = props.getUrl() + req;
        response = restTemplate.getForEntity(restPath, String.class);

        log.info(response.getBody());

        for (Integer sdpId: sdpIds) {
            req = "operational/devices/device/"+device+"/live-status/service/sdp/"+sdpId;
            log.info(req);
            restPath = props.getUrl() + req;
            response = restTemplate.getForEntity(restPath, String.class);

            log.info(response.getBody());

        }

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
