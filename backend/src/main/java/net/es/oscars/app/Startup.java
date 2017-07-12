package net.es.oscars.app;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.props.StartupProperties;
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

    @Bean
    public Executor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Autowired
    public Startup(StartupProperties startupProperties,
                   TopoPopulator topoPopulator,
                   UserPopulator userPopulator,
                   UIPopulator uiPopulator,
                   ConsistencyChecker consistencyChecker) {
        this.startupProperties = startupProperties;
        components = new ArrayList<>();
        components.add(userPopulator);
        components.add(topoPopulator);
        components.add(uiPopulator);
        components.add(consistencyChecker);
    }

    public void onStart() throws IOException {
        if (startupProperties.getExit()) {
            System.out.println("Exiting..");
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

    }

}
