package net.es.oscars.cuke;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.PendingException;
import cucumber.api.java.cs.A;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.Components;
import net.es.oscars.resv.ent.Design;
import net.es.oscars.snp.beans.CmpDelta;
import net.es.oscars.snp.ent.ConfigSnippet;
import net.es.oscars.snp.ent.DeviceConfigNode;
import net.es.oscars.snp.ent.DeviceConfigState;
import net.es.oscars.snp.ent.Modify;
import net.es.oscars.snp.svc.CmpDeltaAPI;
import net.es.oscars.snp.svc.SnippetAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.persistence.ElementCollection;
import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

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

    private Map<String, DeviceConfigState> configStateStore = new HashMap<>();
    private Components cmp;
    private CmpDelta delta;
    private List<DeviceConfigNode> rootNodes;
    private List<Modify> modifyList = new ArrayList<>();
    private Set<ConfigSnippet> result = new HashSet<>();

    @Given("^I load components from \"([^\"]*)\"$")
    public void i_load_the_component(String path) throws Throwable {
        ObjectMapper mapper = builder.build();
        File f = new File(path);
        try {
            this.cmp = mapper.readValue(f, Components.class);
            // log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(design));
        } catch (Exception ex) {
            // log.error("caught: "+ex.getMessage());
            // ex.printStackTrace();
            world.add(ex);
        }

        // Build configStateStore from components

        log.info(String.valueOf(this.cmp));

    }

    @Given("^I load the config state for \"([^\"]*)\" from \"([^\"]*)\"$")
    public void i_load_the_config_state(String connId, String path) throws Throwable {
        ObjectMapper mapper = builder.build();
        File f = new File(path);
        try {
            DeviceConfigState dcs = mapper.readValue(f, DeviceConfigState.class);
            snippetSvc.setDeviceConfigState(connId, dcs);
            // log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(design));
        } catch (Exception ex) {
            // log.error("caught: "+ex.getMessage());
            // ex.printStackTrace();
            world.add(ex);
        }

        // Build configStateStore from components

        log.info(String.valueOf(this.cmp));

    }
    @Then("^I generate a delta for building the new components$")
    public void i_generate_a_delta_for_building_the_new_components() throws Throwable {
        this.delta = cmpDeltaSvc.build("A", this.cmp);
        log.info(String.valueOf(this.delta));
    }

    @Then("^I generate a delta for adding a fixture with connectionId \"([^\"]*)\" on junction \"([^\"]*)\"$")
    public void i_generate_a_delta_for_adding_a_fixture_with_connectionId_on_junction(String connectionId, String refId) throws Throwable {
        this.delta = cmpDeltaSvc.addFixture(refId, connectionId, this.cmp);
        log.info(String.valueOf(this.delta));
    }

    @Then("^I generate a delta for adding a junction with refId \"([^\"]*)\" and connectionId \"([^\"]*)\"$")
    public void i_generate_a_delta_for_adding_a_junction_with_refId(String refId, String connectionId) throws Throwable {
        this.delta = cmpDeltaSvc.addJunction(refId, connectionId, this.cmp);
        log.info(String.valueOf(this.delta));
    }

    @Then("^based on the delta I generate a list of modifications$")
    public void based_on_the_delta_I_generate_a_list_of_modifications() throws Throwable {
        Pair<List<Modify>, List<DeviceConfigNode>> p = snippetSvc.decide(this.delta);
        this.modifyList = p.getKey();
        this.rootNodes = p.getValue();

        log.info(String.valueOf(this.modifyList));
        log.info(String.valueOf(this.rootNodes));
    }

    @Then("^I commit those modifications$")
    public void i_commit_those_modifications() throws Throwable {
        snippetSvc.commitModifications(this.rootNodes, this.modifyList);
    }

//    @Then("^I generate a delta for adding a new junction$")
//    public void i_generate_a_delta_for_adding_a_new_junction() throws Throwable {
//        // Write code here that turns the phrase above into concrete actions
//        throw new PendingException();
//    }
//
//    @Then("^I generate a delta for setting ip address \"([^\"]*)\" on junction \"([^\"]*)\"$")
//    public void i_generate_a_delta_for_setting_ip_address_to_on_junction(String ipAddress1, String deviceUrn) throws Throwable {
//
//        Set<String> ipAddresses = new HashSet<String>();
//        ipAddresses.add(ipAddress1);
//
//        this.delta = cmpDeltaSvc.setIpv4Addresses(deviceUrn, this.cmp, ipAddresses);
//
//        log.info(String.valueOf(delta));
//    }
//
//    @Then("^I generate snippets for the previous delta$")
//    public void i_generate_snippets_for_the_previous_delta() throws Throwable {
//        // TODO sartaj: use the autowired snippetSvc to gen the snippets
//
//        log.info(String.valueOf(this.cmp));
//        this.result = snippetSvc.generateNeededSnippets("a", delta);
//    }
//
//    @Then("^the latest set of generated snippets has (\\d+) members$")
//    public void the_latest_set_of_generated_snippets_has_members(int N) throws Throwable {
//        assert result.size() == N;
//    }
//
//    @Then("^I didn't receive an exception$")
//    public void i_did_not_receive_an_exception() throws Throwable {
//        assertThat(this.world.getExceptions().isEmpty(), is(true));
//    }
//
//    @Then("^I add a junction \"([^\"]*)\"$")
//    public void i_add_a_junction(String path) throws Throwable {
//        throw new PendingException();
//    }

}
