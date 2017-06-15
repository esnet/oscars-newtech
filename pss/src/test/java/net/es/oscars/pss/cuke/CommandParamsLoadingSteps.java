package net.es.oscars.pss.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.AbstractPssTest;
import net.es.oscars.pss.PssTestConfiguration;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.help.ParamsLoader;
import net.es.oscars.pss.help.RouterTestSpec;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@TestPropertySource(locations = "file:config/test/application.properties")
@ContextConfiguration(
        loader = SpringBootContextLoader.class,
        classes = PssTestConfiguration.class)
@Category({UnitTests.class})
@Slf4j
public class CommandParamsLoadingSteps {
    @Autowired
    private ParamsLoader loader;
    @Autowired
    private CucumberWorld world;


    @Given("^I have loaded the \"([^\"]*)\" test commands$")
    public void i_have_loaded_the_CommandType_test_commands(CommandType t) {
        try {
            loader.loadSpecs(t);
        } catch (ConfigException | IOException ex) {
            world.add(ex);
        }
    }

    @Given("^I choose the commands matching device model \"([^\"]*)\"$")
    public void i_choose_the_commands_matching_device_model_(DeviceModel model) {
        List<RouterTestSpec> specs = loader.getSpecs().stream().filter(t -> t.getModel().equals(model)).collect(Collectors.toList());
        loader.getSpecs().clear();
        loader.getSpecs().addAll(specs);
    }

    @Then("^all the test commands generated an exception$")
    public void all_the_test_commands_generated_an_exception() throws Throwable {
        assertThat(loader.getSpecs().size(), is(world.getExceptions().size()));
    }


    @Given("^I choose the commands that should \"([^\"]*)\"$")
    public void i_choose_the_commands_that_should(String succOrFail) {
        List<RouterTestSpec> specs;
        if (succOrFail.equals("SUCCEED")) {
            specs = loader.getSpecs().stream().filter(t -> t.getShouldFail().equals(false)).collect(Collectors.toList());
        } else {
            specs = loader.getSpecs().stream().filter(t -> t.getShouldFail().equals(true)).collect(Collectors.toList());
        }
        loader.getSpecs().clear();
        loader.getSpecs().addAll(specs);
    }

    @Then("^the command list is not empty$")
    public void the_command_list_is_not_empty() throws Throwable {
        assertThat(loader.getSpecs().isEmpty(), is(false));
    }

    @Then("^the command list is empty$")
    public void the_command_list_is_empty() throws Throwable {
        assertThat(loader.getSpecs().isEmpty(), is(true));
    }

}

