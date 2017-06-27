package net.es.oscars.pss.task;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.svc.CommandQueuer;
import net.es.oscars.pss.svc.CommandRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



@Slf4j
@Component
public class CommandProcessor {
    private CommandQueuer queuer;
    private CommandRunner runner;

    @Autowired
    public CommandProcessor(CommandQueuer queuer, CommandRunner runner) {

        this.queuer = queuer;
        this.runner = runner;
    }

    @Scheduled(fixedDelay = 1000)
    public void processsCommands() throws InterruptedException {

        // serially process everything
        queuer.ofLifecycleStatus(LifecycleStatus.INITIAL_STATE).entrySet().forEach(e -> {
            String commandId = e.getKey();
            CommandStatus status = e.getValue();
            log.info("processing a command with id "+commandId);
            status.setLifecycleStatus(LifecycleStatus.PROCESSING);
            log.info("running command "+commandId);
            queuer.getCommand(commandId).ifPresent(cmd -> runner.run(status, cmd));
            log.info("completed command "+commandId);
            status.setLifecycleStatus(LifecycleStatus.DONE);
            queuer.setCommandStatus(commandId, status);
        });

    }

    @Scheduled(fixedDelay = 60000)
    public void sayHi() throws InterruptedException {
        log.info("PSS is alive");

    }



}
