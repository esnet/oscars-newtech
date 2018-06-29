package net.es.oscars.app;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.app.props.StartupProperties;
import net.es.oscars.app.util.GitRepositoryState;
import net.es.oscars.app.util.GitRepositoryStatePopulator;
import net.es.oscars.pss.svc.PssHealthChecker;
import net.es.oscars.security.db.UserPopulator;
import net.es.oscars.topo.beans.TopoException;
import net.es.oscars.topo.pop.ConsistencyException;
import net.es.oscars.topo.pop.TopoPopulator;
import net.es.oscars.topo.pop.UIPopulator;
import net.es.oscars.topo.svc.TopoService;
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
    private TopoPopulator topoPopulator;
    private StartupProperties startupProperties;
    private GitRepositoryStatePopulator gitRepositoryStatePopulator;
    private PssHealthChecker pssHealthChecker;
    private TopoService topoService;

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
                   TopoService topoService,
                   TopoPopulator topoPopulator,
                   UserPopulator userPopulator,
                   UIPopulator uiPopulator,
                   PssHealthChecker pssHealthChecker,
                   GitRepositoryStatePopulator gitRepositoryStatePopulator) {
        this.startupProperties = startupProperties;
        this.topoPopulator = topoPopulator;
        this.topoService = topoService;
        this.gitRepositoryStatePopulator = gitRepositoryStatePopulator;
        components = new ArrayList<>();
        components.add(userPopulator);
        components.add(uiPopulator);
        components.add(gitRepositoryStatePopulator);
        components.add(pssHealthChecker);
    }

    public void onStart() throws IOException, ConsistencyException, TopoException {
        System.out.println(startupProperties.getBanner());

        this.setInStartup(true);
        if (startupProperties.getExit()) {
            this.setInStartup(false);
            this.setInShutdown(true);
            System.out.println("Exiting (startup.exit is true)");
            System.exit(0);
        }
        topoPopulator.refreshTopology();
        topoService.updateTopo();

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
