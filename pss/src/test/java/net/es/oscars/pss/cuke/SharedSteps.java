package net.es.oscars.pss.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.help.PssTestConfig;
import net.es.oscars.pss.prop.RancidProps;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Category({UnitTests.class})
public class SharedSteps {
    @Autowired
    private CucumberWorld world;

    @Autowired
    private RancidProps rancidProps;

    @Autowired
    private PssTestConfig pssTestConfig;


    @Given("^I have initialized the world$")
    public void i_have_initialized_the_world() throws Throwable {
        world.getExceptions().clear();
    }

    @Given("^The world is expecting an exception$")
    public void the_world_is_expecting_an_exception() throws Throwable {
        world.expectException();
    }

    @Then("^I did not receive an exception$")
    public void i_did_not_receive_an_exception() throws Throwable {
        assertThat(world.getExceptions().isEmpty(), is(true));
    }

    @Then("^I did receive an exception$")
    public void i_did_receive_an_exception() throws Throwable {
        assertThat(world.getExceptions().isEmpty(), is(false));
    }

    @Then("^I set the rancid execute property to true$")
    public void i_set_the_rancid_execute_property_to_true() throws Throwable {
        rancidProps.setExecute(true);
    }

    @Then("^I set the rancid proxy to \"([^\"]*)\"$")
    public void i_set_the_rancid_proxy_to(String host) throws Throwable {
        rancidProps.setHost(host);
    }

    @Then("^I set the test specification directory to \"([^\"]*)\"$")
    public void i_set_the_test_specification_directory_to(String directory) throws Throwable {
        pssTestConfig.setCaseDirectory(directory);
    }

    @Given("^I have warned the user this is a live test$")
    public void i_have_warned_the_user_this_is_a_live_test() throws Throwable {
        System.out.println("==============================================================================");
        System.out.println("Ready to run control plane tests! These WILL attempt to contact routers.");
        System.out.println("Make sure you have configured test.properties correctly. ");
        System.out.println("Starting in 3 seconds. Ctrl-C to abort.");
        System.out.println("==============================================================================");
        Thread.sleep(3000);

    }

}

