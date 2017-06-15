package net.es.oscars.pss.cuke;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.pss.PssTestConfiguration;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.help.ParamsLoader;
import net.es.oscars.pss.help.RouterTestSpec;
import net.es.oscars.pss.svc.AluCommandGenerator;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;


@TestPropertySource(locations = "file:config/test/application.properties")
@ContextConfiguration(
        loader = SpringBootContextLoader.class,
        classes = PssTestConfiguration.class)
@Category({UnitTests.class})
@Slf4j
public class AluSteps {

    @Autowired
    private AluCommandGenerator commandGen;
    @Autowired
    private ParamsLoader loader;
    @Autowired
    private CucumberWorld world;

    @When("^I \"([^\"]*)\" on the alu command generator with the test commands$")
    public void i_EXEC_on_the_alu_command_generator_with_the_test_commands(CommandType t) {
        for (RouterTestSpec spec : loader.getSpecs()) {
            try {
                String config;
                if (t.equals(CommandType.BUILD)) {
                    config = commandGen.build(spec.getAluParams());

                } else if (t.equals(CommandType.DISMANTLE)) {
                    config = commandGen.dismantle(spec.getAluParams());
                }
            } catch (ConfigException ex) {
                world.add(ex);
            }
        }

    }

}

