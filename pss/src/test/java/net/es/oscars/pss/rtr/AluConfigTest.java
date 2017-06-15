package net.es.oscars.pss.rtr;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.AbstractPssTest;
import net.es.oscars.pss.ctg.AluTests;
import net.es.oscars.pss.ctg.RouterTests;
import net.es.oscars.pss.help.PssTestConfig;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.help.ParamsLoader;
import net.es.oscars.pss.help.RouterTestSpec;
import net.es.oscars.pss.svc.CommandRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

@Slf4j
public class AluConfigTest {

    @Autowired
    private CommandRunner runner;
    @Autowired
    private ParamsLoader loader;
    @Autowired
    private RancidProps rancidProps;
    @Autowired
    private PssTestConfig pssTestConfig;

    @Before
    public void before() throws InterruptedException {
        System.out.println("==============================================================================");
        System.out.println("Ready to run build / dismantle tests! These WILL attempt to configure routers.");
        System.out.println("Make sure you have configured test.properties correctly. ");
        System.out.println("Starting in 3 seconds. Ctrl-C to abort.");
        System.out.println("==============================================================================");
        Thread.sleep(3000);
        rancidProps.setExecute(true);

    }

    @Category({RouterTests.class, AluTests.class})
    public void singleAluTest() throws IOException {
        log.info("starting single ALU test");
        String prefix = pssTestConfig.getCaseDirectory();


        RouterTestSpec setupRts = loader.loadSpec(prefix + "/build-alu-single.json");


        Command setupCmd = Command.builder()
                .device(setupRts.getDevice())
                .model(setupRts.getModel())
                .type(CommandType.BUILD)
                .alu(setupRts.getAluParams())
                .mx(setupRts.getMxParams())
                .ex(setupRts.getExParams())
                .build();
        CommandStatus setupStatus = CommandStatus.builder()
                .configStatus(ConfigStatus.NONE)
                .lifecycleStatus(LifecycleStatus.PROCESSING)
                .device(setupRts.getDevice())
                .type(CommandType.BUILD)
                .lastUpdated(new Date())
                .commands("")
                .output("")

                .build();

        runner.run(setupStatus, setupCmd);

        RouterTestSpec tdRts = loader.loadSpec(prefix + "/dismantle-alu-single.json");


        Command tdCmd = Command.builder()
                .device(tdRts.getDevice())
                .model(tdRts.getModel())
                .type(CommandType.DISMANTLE)
                .alu(tdRts.getAluParams())
                .mx(tdRts.getMxParams())
                .ex(tdRts.getExParams())
                .build();
        CommandStatus tdStatus = CommandStatus.builder()
                .type(CommandType.DISMANTLE)
                .configStatus(ConfigStatus.NONE)
                .device(tdRts.getDevice())
                .commands("")
                .output("")
                .lastUpdated(new Date())
                .lifecycleStatus(LifecycleStatus.PROCESSING)
                .build();

        runner.run(tdStatus, tdCmd);


    }

    @Category({RouterTests.class, AluTests.class})
    public void twoAluTest() throws IOException, InterruptedException {
        String prefix = pssTestConfig.getCaseDirectory();


        log.info("starting two ALU test: build");
        List<String> setups = new ArrayList<>();
        setups.add(prefix + "/build-2_a-z.json");
        setups.add(prefix + "/build-2_z-a.json");

        for (String setup : setups) {
            RouterTestSpec setupRts = loader.loadSpec(setup);
            Command setupCmd = Command.builder()
                    .device(setupRts.getDevice())
                    .model(setupRts.getModel())
                    .type(CommandType.BUILD)
                    .alu(setupRts.getAluParams())
                    .mx(setupRts.getMxParams())
                    .ex(setupRts.getExParams())
                    .build();
            CommandStatus setupStatus = CommandStatus.builder()
                    .configStatus(ConfigStatus.NONE)
                    .lifecycleStatus(LifecycleStatus.PROCESSING)
                    .device(setupRts.getDevice())
                    .type(CommandType.BUILD)
                    .lastUpdated(new Date())
                    .commands("")
                    .output("")
                    .build();

            runner.run(setupStatus, setupCmd);


        }
        Thread.sleep(10000);

        List<String> teardowns = new ArrayList<>();
        teardowns.add(prefix + "/dismantle-2_a-z.json");
        teardowns.add(prefix + "/dismantle-2_z-a.json");

        for (String teardown : teardowns) {
            RouterTestSpec setupRts = loader.loadSpec(teardown);

            Command setupCmd = Command.builder()
                    .device(setupRts.getDevice())
                    .model(setupRts.getModel())
                    .type(CommandType.DISMANTLE)
                    .alu(setupRts.getAluParams())
                    .mx(setupRts.getMxParams())
                    .ex(setupRts.getExParams())
                    .build();
            CommandStatus setupStatus = CommandStatus.builder()
                    .configStatus(ConfigStatus.NONE)
                    .lifecycleStatus(LifecycleStatus.PROCESSING)
                    .device(setupRts.getDevice())
                    .type(CommandType.DISMANTLE)
                    .lastUpdated(new Date())
                    .commands("")
                    .output("")
                    .build();

            runner.run(setupStatus, setupCmd);

        }
    }

}
