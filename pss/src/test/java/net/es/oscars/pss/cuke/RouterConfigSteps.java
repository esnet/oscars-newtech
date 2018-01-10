package net.es.oscars.pss.cuke;

import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.pss.st.ConfigStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.help.ParamsLoader;
import net.es.oscars.pss.help.RouterTestSpec;
import net.es.oscars.pss.svc.CommandRunner;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Category({UnitTests.class})
@Slf4j
public class RouterConfigSteps extends CucumberSteps {
    @Autowired
    private CucumberWorld world;

    @Autowired
    private CommandRunner runner;

    @Autowired
    private ParamsLoader loader;

    @Then("^I will generate and run the \"([^\"]*)\" commands from test specs$")
    public void i_will_generate_and_run_the_commands_from_test_specs(CommandType t) {
        for (RouterTestSpec rts : loader.getSpecs()) {
            Command cmd = Command.builder()
                    .device(rts.getDevice())
                    .model(rts.getModel())
                    .type(t)
                    .alu(rts.getAluParams())
                    .mx(rts.getMxParams())
                    .ex(rts.getExParams())
                    .build();
            CommandStatus status = CommandStatus.builder()
                    .configStatus(ConfigStatus.NONE)
                    .lifecycleStatus(LifecycleStatus.PROCESSING)
                    .device(rts.getDevice())
                    .type(CommandType.BUILD)
                    .lastUpdated(new Date())
                    .commands("")
                    .output("")
                    .build();
            runner.run(status, cmd);

        }


    }

}

