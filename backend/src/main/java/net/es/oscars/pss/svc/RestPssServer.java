package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.dto.pss.cmd.*;
import net.es.oscars.rest.RestConfigurer;
import net.es.oscars.rest.RestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
@Slf4j
public class RestPssServer implements PSSProxy {
    private PssProperties props;
    private RestTemplate restTemplate;

    @Autowired
    public RestPssServer(PssProperties props, RestProperties restProperties, RestConfigurer restConfigurer) {

        this.props = props;
        try {
            this.restTemplate = new RestTemplate(restConfigurer.getRestConfig(restProperties));
            String u = restProperties.getInternalUsername();
            String p = restProperties.getInternalPassword();
            restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(u, p));

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        log.info("PSS server URL: " + props.getUrl());
    }


    public CommandResponse submitCommand(Command cmd) {
        if (cmd.getType().equals(CommandType.CONTROL_PLANE_STATUS)) {
            log.info("submit command - device " + cmd.getDevice());

        } else {
            log.info("submit command - conn id: " + cmd.getConnectionId() + " , dev: " + cmd.getDevice());

        }
        String pssUrl = props.getUrl();
        String submitUrl = "/command";
        String restPath = pssUrl + submitUrl;
        return restTemplate.postForObject(restPath, cmd, CommandResponse.class);

    }

    public DeviceConfigResponse getConfig(DeviceConfigRequest request) {
        log.info("getConfig - device " + request.getDevice());
        String pssUrl = props.getUrl();
        String submitUrl = "/getConfig";
        String restPath = pssUrl + submitUrl;
        return restTemplate.postForObject(restPath, request, DeviceConfigResponse.class);
    }


    public CommandStatus status(String commandId) {
        log.info("status - cmd id " + commandId);
        String pssUrl = props.getUrl();
        String submitUrl = "/status/" + commandId;
        String restPath = pssUrl + submitUrl;
        return restTemplate.getForObject(restPath, CommandStatus.class);
    }

}