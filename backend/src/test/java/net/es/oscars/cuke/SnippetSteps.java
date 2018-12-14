package net.es.oscars.cuke;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.cs.A;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.Components;
import net.es.oscars.resv.ent.Design;
import net.es.oscars.snp.beans.CmpDelta;
import net.es.oscars.snp.ent.ConfigSnippet;
import net.es.oscars.snp.svc.CmpDeltaAPI;
import net.es.oscars.snp.svc.SnippetAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.persistence.ElementCollection;
import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Slf4j
public class SnippetSteps {

    @Autowired
    private SnippetAPI snippetSvc;

    @Autowired
    private CmpDeltaAPI cmpDeltaSvc;

    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private CucumberWorld world;

    private Components cmp;
    private CmpDelta delta;
    private Set<ConfigSnippet> result = new HashSet<>();

    @Given("^I load design from \"([^\"]*)\"$")
    public void load_design(String path) throws Throwable {

        ObjectMapper mapper = builder.build();
        File f = new File(path);
        world.design = mapper.readValue(f, Design.class);
        // log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(world.design));
    }

    @Then("^I load the component$")
    public void i_load_the_component() throws Throwable {
        this.cmp = world.design.getCmp();

        // First, build a delta from basic component, prob should be done in the load_components_from
        // CmpDelta result = cmpDeltaSvc.build(refId, world.design.getCmp());
    }

    @Then("^I generate a delta for setting ip address \"([^\"]*)\" on junction \"([^\"]*)\"$")
    public void i_generate_a_delta_for_setting_ip_address_to_on_junction(String ipAddress1, String deviceUrn) throws Throwable {

        Set<String> ipAddresses = new HashSet<String>();
        ipAddresses.add(ipAddress1);

        this.delta = cmpDeltaSvc.setIpv4Addresses(deviceUrn, this.cmp, ipAddresses);

        log.info(String.valueOf(delta));
    }

    @Then("^I generate snippets for the previous delta$")
    public void i_generate_snippets_for_the_previous_delta() throws Throwable {
        // TODO sartaj: use the autowired snippetSvc to gen the snippets

        log.info(String.valueOf(this.cmp));
        this.result = snippetSvc.generateNeededSnippets("a", delta);
    }

    @Then("^the latest set of generated snippets has (\\d+) members$")
    public void the_latest_set_of_generated_snippets_has_members(int N) throws Throwable {
        assert result.size() == N;
    }

    @Then("^I didn't receive an exception$")
    public void i_did_not_receive_an_exception() throws Throwable {
        assertThat(this.world.getExceptions().isEmpty(), is(true));
    }

}
