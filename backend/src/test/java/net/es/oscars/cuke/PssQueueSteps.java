package net.es.oscars.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ctg.UnitTests;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.pss.beans.QueueName;
import net.es.oscars.pss.svc.PSSQueuer;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@Category({UnitTests.class})
public class PssQueueSteps extends CucumberSteps {
    @Autowired
    private PSSQueuer queuer;

    @Given("^I clear all sets$")
    public void i_clear_all_sets() {
        queuer.clear();
    }

    @When("^I add a \"([^\"]*)\" task for \"([^\"]*)\" on \"([^\"]*)\"$")
    public void i_add_a_task_for_on(CommandType ct, String connId, String deviceUrn) {
        queuer.add(ct, connId, deviceUrn);
    }

    @Then("^the \"([^\"]*)\" set has (\\d+) entries$")
    public void the_set_has_entries(QueueName qn, int num) {
        assert queuer.entries(qn).size() == num;
    }

    @When("^I trigger the queue processor$")
    public void i_trigger_the_queue_processor() {
        queuer.process();
    }

    @When("^I make all running tasks complete$")
    public void i_make_all_running_tasks_complete() {
        queuer.forceCompletion();
    }

}