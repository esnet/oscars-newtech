package net.es.oscars.app;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.app.props.StartupProperties;
import net.es.oscars.app.util.GitRepositoryState;
import net.es.oscars.app.util.GitRepositoryStatePopulator;
import net.es.oscars.pss.svc.PssHealthChecker;
import net.es.oscars.security.db.UserPopulator;
import net.es.oscars.topo.pop.ConsistencyChecker;
import net.es.oscars.topo.pop.TopoPopulator;
import net.es.oscars.topo.pop.UIPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class Startup {

    private List<StartupComponent> components;
    private StartupProperties startupProperties;
    private GitRepositoryStatePopulator gitRepositoryStatePopulator;
    private PssHealthChecker pssHealthChecker;

    private boolean inStartup = false;
    private boolean inShutdown = false;

    public void setInStartup(boolean inStartup) {
        this.inStartup = inStartup;
    }

    public boolean isInShutdown() {
        return inShutdown;
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
    public Startup(StartupProperties startupProperties,
                   TopoPopulator topoPopulator,
                   UserPopulator userPopulator,
                   UIPopulator uiPopulator,
                   ConsistencyChecker consistencyChecker,
                   PssHealthChecker pssHealthChecker,
                   GitRepositoryStatePopulator gitRepositoryStatePopulator) {
        this.startupProperties = startupProperties;
        this.gitRepositoryStatePopulator = gitRepositoryStatePopulator;
        components = new ArrayList<>();
        components.add(userPopulator);
        components.add(topoPopulator);
        components.add(uiPopulator);
        components.add(consistencyChecker);
        components.add(gitRepositoryStatePopulator);
        components.add(pssHealthChecker);
    }

    public void onStart() throws IOException {
        this.setInStartup(true);
        if (startupProperties.getExit()) {
            this.setInStartup(false);
            this.setInShutdown(true);
            System.out.println("Exiting (startup.exit is true)");
            System.exit(0);
        }

        System.out.println(startupProperties.getBanner());
        try {
            for (StartupComponent sc : this.components) {
                sc.startup();
            }
        } catch (StartupException ex) {
            ex.printStackTrace();;
            System.out.println("Exiting..");
            System.exit(1);
        }

        GitRepositoryState gitRepositoryState = this.gitRepositoryStatePopulator.getGitRepositoryState();
        log.info("OSCARS backend (" + gitRepositoryState.getDescribe() + " on " + gitRepositoryState.getBranch() + ")");
        log.info("Built by " + gitRepositoryState.getBuildUserEmail() + " on " + gitRepositoryState.getBuildHost() + " at " + gitRepositoryState.getBuildTime());

        log.info("OSCARS startup successful.");
        this.setInStartup(false);

    }

}
