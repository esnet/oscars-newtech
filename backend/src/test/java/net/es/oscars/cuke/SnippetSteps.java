package net.es.oscars.cuke;

import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.snp.svc.CmpDeltaAPI;
import net.es.oscars.snp.svc.SnippetAPI;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class SnippetSteps {
    @Autowired
    private SnippetAPI snippetSvc;

    @Autowired
    private CmpDeltaAPI cmpDeltaSvc;



    @Then("^load components from \"([^\"]*)\"$")
    public void load_components_from(String arg1) throws Throwable {

        // TODO sartaj: load a JSON file that deserialiazes into a Components object
        // then keep it around in a class variable
        // Write code here that turns the phrase above into concrete actions
    }

    @Then("^I gen a delta for setting ip address \"([^\"]*)\" to \"([^\"]*)\" on junction \"([^\"]*)\"$")
    public void i_gen_a_delta_for_setting_ip_address_to_on_junction(String arg1, String arg2, String arg3) throws Throwable {
        // TODO sartaj: use the autowired cmpDeltaSvc to generate the delta
        // then keep it around in a class variable
    }

    @Then("^I gen snippets for the previous delta$")
    public void i_gen_snippets_for_the_previous_delta() throws Throwable {
        // TODO sartaj: use the autowired snippetSvc to gen the snippets
        // then keep them around in a class variable
    }

    @Then("^the latest set of generated snippets has (\\d+) members$")
    public void the_latest_set_of_generated_snippets_has_members(int arg1) throws Throwable {
        // TODO sartaj: verify the saved snippet sets has N members
    }

}
