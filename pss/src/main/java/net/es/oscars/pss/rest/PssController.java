package net.es.oscars.pss.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.*;
import net.es.oscars.dto.pss.cp.ControlPlaneHealth;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.beans.UrnMappingException;
import net.es.oscars.pss.beans.VerifyException;
import net.es.oscars.pss.svc.CommandQueuer;
import net.es.oscars.pss.svc.ConfigCollector;
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
    private CommandQueuer commandQueuer;
    private ConfigCollector configCollector;

    @Autowired
    public PssController(HealthService healthService,
                         CommandQueuer commandQueuer,
                         ConfigCollector configCollector,
                         RouterConfigBuilder routerConfigBuilder) {
        this.healthService = healthService;
        this.routerConfigBuilder = routerConfigBuilder;
        this.commandQueuer = commandQueuer;
        this.configCollector = configCollector;
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
        log.info("received a command, type: "+cmd.getType()+
                 " connId: " + cmd.getConnectionId() + " device: " + cmd.getDevice());

        String commandId = commandQueuer.newCommand(cmd);

        return CommandResponse.builder()
                .commandId(commandId)
                .device(cmd.getDevice())
                .build();

    }


    @RequestMapping(value = "/getConfig", method = RequestMethod.POST)
    public DeviceConfigResponse getConfig(@RequestBody DeviceConfigRequest request) throws VerifyException {
        return this.configCollector.getConfig(request);
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

    @RequestMapping(value = "/status/{commandId}", method = RequestMethod.GET)
    public CommandStatus commandStatus(@PathVariable("commandId") String commandId) {
        if (commandQueuer.getStatus(commandId).isPresent()) {
            return commandQueuer.getStatus(commandId).get();
        } else {
            log.error("no status for "+commandId);
            throw new NoSuchElementException("command id not found: "+commandId);
        }
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public ControlPlaneHealth health() {
        return healthService.getHealth();
    }




}
