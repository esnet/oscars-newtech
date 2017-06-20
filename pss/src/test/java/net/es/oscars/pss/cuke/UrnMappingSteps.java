package net.es.oscars.pss.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.st.ControlPlaneStatus;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.pss.beans.*;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.help.PssTestConfig;
import net.es.oscars.pss.prop.UrnMappingProps;
import net.es.oscars.pss.svc.CommandQueuer;
import net.es.oscars.pss.svc.HealthService;
import net.es.oscars.pss.svc.UrnMappingService;
import org.apache.commons.lang3.StringUtils;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Category({UnitTests.class})
@Slf4j
public class UrnMappingSteps extends CucumberSteps {
    @Autowired
    private CucumberWorld world;

    @Autowired
    private UrnMappingService mappingService;

    @Autowired
    private UrnMappingProps properties;


    @Given("^I have cleared all mappings$")
    public void i_have_cleared_all_mappings() {
        mappingService.getMapping().getEntryMap().clear();
    }

    @When("^I try to load URN mappings from \"([^\"]*)\"$")
    public void i_try_to_load_URN_mappings_from(String filename) {
        try {
            mappingService.loadFrom(filename);
        } catch (IOException ex) {
            world.add(ex);
        }

    }

    @Then("^I have loaded (\\d+) URN mappings$")
    public void i_have_loaded_URN_mappings(int num) {
        assertThat(mappingService.getMapping().getEntryMap().size(), is(num));
    }

    @When("^I set the control plane addressing method to \"([^\"]*)\"$")
    public void i_set_the_control_plane_addressing_method_to(UrnMappingMethod method) {
        properties.setMethod(method);
    }

    @When("^I set the DNS suffix to \"([^\"]*)\"$")
    public void i_set_the_DNS_suffix_to(String suffix) {
        properties.setDnsSuffix(suffix);
    }

    @Then("^the router address of \"([^\"]*)\" is \"([^\"]*)\"$")
    public void the_router_address_of_is(String urn, String addr) {
        try {
            assertThat(mappingService.getRouterAddress(urn), is(addr));
        } catch (UrnMappingException ex) {
            world.add(ex);
        }
    }

    @Given("^I added a configured mapping from \"([^\"]*)\" to \"([^\"]*)\" \"([^\"]*)\"$")
    public void i_added_a_configured_mapping_from_to(String urn, String dns, String addr) {
        UrnMappingEntry entry = UrnMappingEntry.builder()
                .ipv4Address(addr)
                .dns(dns)
                .build();
        mappingService.getMapping().getEntryMap().put(urn, entry);
    }

    @When("^I ask for the router address of \"([^\"]*)\"$")
    public void i_ask_for_the_router_address_of(String urn) throws Throwable {
        try {
            mappingService.getRouterAddress(urn);
        } catch (UrnMappingException ex) {
            world.add(ex);
        }
    }

}

