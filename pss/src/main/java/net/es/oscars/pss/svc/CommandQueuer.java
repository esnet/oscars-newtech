package net.es.oscars.pss.svc;

import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.*;
import org.hashids.Hashids;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CommandQueuer {
    private ConcurrentHashMap<String, Command> commands = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, CommandStatus> statuses = new ConcurrentHashMap<>();
    private Hashids hashids = new Hashids("ESnet salt");

    public String newCommand(Command command) {
        CommandStatus commandStatus = CommandStatus.builder()
                .lifecycleStatus(LifecycleStatus.INITIAL_STATE)
                .configStatus(ConfigStatus.NONE)
                .profile(command.getProfile())
                .operationalStatus(OperationalStatus.NONE)
                .controlPlaneStatus(ControlPlaneStatus.NONE)
                .commands("")
                .connectionId(command.getConnectionId())
                .device(command.getDevice())
                .lastUpdated(new Date())
                .output("")
                .type(command.getType())
                .build();

        Random rand = new Random();
        Integer id = rand.nextInt();
        if (id < 0 ) {
            id = -1 * id;
        }

        String commandId = hashids.encode(id);

        commands.put(commandId, command);
        statuses.put(commandId, commandStatus);

        return commandId;
    }


    public void setCommand(String commandId, Command command) {
        commands.put(commandId, command);
    }

    public void setCommandStatus(String commandId, CommandStatus status) {
        statuses.put(commandId, status);
    }

    public Optional<Command> getCommand(String commandId) {
        if (commands.containsKey(commandId)) {
            return Optional.of(commands.get(commandId));
        } else {
            return Optional.empty();
        }
    }

    public Optional<CommandStatus> getStatus(String commandId) {
        if (statuses.containsKey(commandId)) {
            return Optional.of(statuses.get(commandId));
        } else {
            return Optional.empty();
        }
    }

    public Map<String, CommandStatus> ofLifecycleStatus(LifecycleStatus status) {
        return statuses.entrySet().stream()
                .filter(map -> map.getValue().getLifecycleStatus().equals(status))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, CommandStatus> ofOperationalStatus(OperationalStatus status) {
        return statuses.entrySet().stream()
                .filter(map -> map.getValue().getOperationalStatus().equals(status))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, CommandStatus> ofConfigStatus(ConfigStatus status) {
        return statuses.entrySet().stream()
                .filter(map -> map.getValue().getConfigStatus().equals(status))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }



}
