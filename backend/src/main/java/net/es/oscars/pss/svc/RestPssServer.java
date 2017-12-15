package net.es.oscars.pss.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.dto.pss.cmd.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
@Slf4j
public class RestPssServer implements PSSProxy {
    private PssProperties props;
    private RestTemplate restTemplate;

    @Autowired
    public RestPssServer(PssProperties props, RestTemplate restTemplate) {

        this.props = props;
        this.restTemplate = restTemplate;
        log.info("PSS server URL: "+props.getUrl());
    }

    public CommandResponse submitCommand(Command cmd) {
        if (cmd.getType().equals(CommandType.CONTROL_PLANE_STATUS)) {
            log.info("submit command - device "+cmd.getDevice());

        } else {
            log.info("submit command - conn id: "+cmd.getConnectionId()+" , dev: "+cmd.getDevice());

        }
        String pssUrl = props.getUrl();
        String submitUrl = "/command";
        String restPath = pssUrl + submitUrl;
        return restTemplate.postForObject(restPath, cmd, CommandResponse.class);

    }

    public GenerateResponse generate(Command cmd) {
        log.info("generate - conn id "+cmd.getConnectionId());
        String pssUrl = props.getUrl();
        String submitUrl = "/generate";
        String restPath = pssUrl + submitUrl;
        return restTemplate.postForObject(restPath, cmd, GenerateResponse.class);
    }

    public CommandStatus status(String commandId) {
        log.info("status - cmd id "+commandId);
        String pssUrl = props.getUrl();
        String submitUrl = "/status/"+commandId;
        String restPath = pssUrl + submitUrl;
        return restTemplate.getForObject(restPath, CommandStatus.class);
    }

}