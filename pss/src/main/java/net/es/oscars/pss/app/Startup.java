package net.es.oscars.pss.app;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.app.props.StartupProps;
import net.es.oscars.pss.app.syslog.Syslogger;
import net.es.oscars.pss.prop.PssProps;
import net.es.oscars.pss.svc.CommandQueuer;
import net.es.oscars.pss.svc.UrnMappingService;
import net.es.oscars.pss.svc.HealthService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Startup {

    private List<StartupComponent> components;
    private StartupProps startupProps;
    private CommandQueuer queuer;
    private PssProps props;
    private HealthService healthService;
    private UrnMappingService urnMappingService;
    private Syslogger syslogger;

    private boolean inStartup = true;
    private boolean inShutdown = false;

    public void setInStartup(boolean inStartup) {
        this.inStartup = inStartup;
    }

    public boolean isInShutdown() {
        return this.inShutdown;
    }

    public void setInShutdown(boolean inShutdown) {
        this.inShutdown = inShutdown;
    }

    public boolean isInStartup() {
        return this.inStartup;
    }

    @Bean
    public Executor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Autowired
    public Startup(StartupProps startupProps,
                   CommandQueuer queuer,
                   HealthService healthService,
                   UrnMappingService urnMappingService,
                   Syslogger syslogger,
                   PssProps props) {
        this.queuer = queuer;
        this.props = props;
        this.syslogger = syslogger;
        this.healthService = healthService;
        this.urnMappingService = urnMappingService;
        this.startupProps = startupProps;
    }

    public void onStart() throws IOException {
        System.out.println(startupProps.getBanner());

        this.setInStartup(true);
        if (startupProps.getExit()) {
            log.info("In Shutdown");
            this.setInStartup(false);
            this.setInShutdown(true);
            syslogger.sendSyslog("PSS STOPPED SUCCESSFULLY");
            System.out.println("Exiting PSS (startup.exit is true)");
            System.exit(0);
        }

        log.info("PSS startup successful.");
        syslogger.sendSyslog("PSS STARTED SUCCESSFULLY");

        this.setInStartup(false);

    }
}
