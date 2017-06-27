package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.authnz.pop.AuthnzPopulator;
import net.es.oscars.helpers.StartupProperties;
import net.es.oscars.pss.pop.UrnAddressImporter;
import net.es.oscars.tasks.ResvProcessor;
import net.es.oscars.topo.pop.ConsistencyException;
import net.es.oscars.topo.pop.TopoFileImporter;
import net.es.oscars.topo.pop.ConsistencyChecker;
import net.es.oscars.topo.pop.UIPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class Startup {

    private TopoFileImporter importer;
    private UIPopulator uiPopulator;
    private UrnAddressImporter urnAddressImporter;
    private ResvProcessor processor;
    private AuthnzPopulator authnzPopulator;
    private ConsistencyChecker consistencyChecker;
    private StartupProperties startupProperties;

    @Bean
    public Executor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Autowired
    public Startup(TopoFileImporter importer, UIPopulator populator, UrnAddressImporter urnAddressImporter,
                   ResvProcessor processor, AuthnzPopulator authnzPopulator,
                   ConsistencyChecker consistencyChecker, StartupProperties startupProperties) {

        this.processor = processor;
        this.authnzPopulator = authnzPopulator;
        this.importer = importer;
        this.uiPopulator = populator;
        this.urnAddressImporter = urnAddressImporter;
        this.consistencyChecker = consistencyChecker;
        this.startupProperties = startupProperties;
    }

    void onStart() throws IOException, ConsistencyException {
        if (startupProperties.getExit()) {
            System.out.println("Exiting..");
            System.exit(0);
        }
        System.out.println(startupProperties.getBanner());

        importer.startup();
        uiPopulator.startup();
        urnAddressImporter.startup();
        processor.startup();
        authnzPopulator.startup();

        consistencyChecker.checkConsistency();
    }

}
