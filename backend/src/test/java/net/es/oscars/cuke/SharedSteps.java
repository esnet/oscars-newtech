package net.es.oscars.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import net.es.oscars.AbstractBackendTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SharedSteps {
    @Autowired
    private CucumberWorld world;


    @Given("^I have initialized the world$")
    public void i_have_initialized_the_world() throws Throwable {
        this.world.getExceptions().clear();
    }

    @Given("^The world is expecting an exception$")
    public void the_world_is_expecting_an_exception() throws Throwable {
        this.world.expectException();
    }

    @Then("^I did not receive an exception$")
    public void i_did_not_receive_an_exception() throws Throwable {
        assertThat(this.world.getExceptions().isEmpty(), is(true));
    }

    @Then("^I did receive an exception$")
    public void i_did_receive_an_exception() throws Throwable {
        assertThat(this.world.getExceptions().isEmpty(), is(false));
    }


}

