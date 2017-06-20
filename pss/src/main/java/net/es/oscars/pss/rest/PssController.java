package net.es.oscars.pss.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandResponse;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.GenerateResponse;
import net.es.oscars.dto.pss.cp.ControlPlaneHealth;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.beans.UrnMappingException;
import net.es.oscars.pss.svc.HealthService;
import net.es.oscars.pss.svc.RouterConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
public class PssController {

    private HealthService healthService;
    private RouterConfigBuilder routerConfigBuilder;

    @Autowired
    public PssController(HealthService healthService, RouterConfigBuilder routerConfigBuilder) {
        this.healthService = healthService;
        this.routerConfigBuilder = routerConfigBuilder;
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        // LOG.warn("user requested a strResource which didn't exist", ex);
    }

    @ResponseBody
    @ExceptionHandler(ConfigException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConfigException(ConfigException ex) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("error_message", ex.getMessage());
        result.put("error", true);
        log.error(ex.getMessage());
        return result;
    }

    @RequestMapping(value = "/command", method = RequestMethod.POST)
    public CommandResponse command(@RequestBody Command cmd) {
        log.info("received a command, connId: " + cmd.getConnectionId() + " device: " + cmd.getDevice());

        return CommandResponse.builder().build();

    }

    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public GenerateResponse generate(@RequestBody Command cmd)
            throws ConfigException, UrnMappingException, JsonProcessingException {
        log.info("generating router config");

        ObjectMapper mapper = new ObjectMapper();
        String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cmd);
        log.info(pretty);

        String generated = routerConfigBuilder.generate(cmd);
        return GenerateResponse.builder()
                .connectionId(cmd.getConnectionId())
                .device(cmd.getDevice())
                .commandType(cmd.getType())
                .generated(generated)
                .build();
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public CommandStatus commandStatus(@PathVariable("commandId") String commandId) {
        return CommandStatus.builder().build();
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ControlPlaneHealth health() {
        return healthService.getHealth();
    }




}
