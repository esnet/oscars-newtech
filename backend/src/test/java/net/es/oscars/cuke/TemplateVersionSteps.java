package net.es.oscars.cuke;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ctg.UnitTests;
import org.junit.experimental.categories.Category;

import java.util.HashMap;

@Slf4j
@Category({UnitTests.class})
public class TemplateVersionSteps extends CucumberSteps {

    @Given("^I set the template directory to \"([^\"]*)\"$")
    public void i_set_the_template_directory_to(String arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^I load the template \"([^\"]*)\"$")
    public void i_load_the_template(String arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the \"([^\"]*)\" for loaded template\\(s\\) \"([^\"]*)\" contain a version tag$")
    public void the_for_loaded_template_s_contain_a_version_tag(String arg1, String arg2) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Then("^the version tag for loaded template\\(s\\) \"([^\"]*)\" consistent$")
    public void the_version_tag_for_loaded_template_s_consistent(String arg1) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^I load all templates in the template directory$")
    public void i_load_all_templates_in_the_template_directory() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }


}