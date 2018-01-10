package net.es.oscars.pss.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.*;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.help.PssTestConfig;
import net.es.oscars.pss.prop.PssProps;
import net.es.oscars.pss.prop.RancidProps;
import net.es.oscars.pss.prop.UrnMappingProps;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Category({UnitTests.class})
public class SharedSteps {
    @Autowired
    private CucumberWorld world;

    @Autowired
    private PssProps pssProps;


    @Autowired
    private PssTestConfig pssTestConfig;

    @Given("^I create profile \"([^\"]*)\"$")
    public void i_create_profile(String name) throws Throwable {
        UrnMappingProps urnMappingProps = UrnMappingProps.builder()
                .method(UrnMappingMethod.IDENTITY)
                .match(new ArrayList<>())
                .suffix("")
                .build();

        RancidCheck check = RancidCheck.builder()
                .devices(new ArrayList<>())
                .perform(false)
                .build();
        RancidProps rancid = RancidProps.builder()
                .cloginrc("")
                .perform(false)
                .dir("")
                .host("")
                .identityFile("")
                .username("")
                .sshOptions(new ArrayList<>())
                .build();

        PssProfile pssProfile = PssProfile.builder()
                .profile(name)
                .check(check)
                .rancid(rancid)
                .urnMapping(urnMappingProps)
                .build();
        pssProps.getProfiles().add(pssProfile);
    }

    @Given("^I clear all profiles")
    public void i_clear_all_profiles() throws Throwable {
        pssProps.getProfiles().clear();
        pssProps.getMatching().clear();
    }

    @Given("^I configure a match for urn \"([^\"]*)\" to profile \"([^\"]*)\"$")
    public void i_configure_a_match_for_urn_to_profile(String urn, String profile) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        List<String> criteria = new ArrayList<>();
        criteria.add(urn);
        ProfileMatch pm = ProfileMatch.builder().criteria(criteria).profile(profile).build();
        pssProps.getMatching().add(pm);
    }

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
    @Then("^I set rancid perform to true on profile \"([^\"]*)\"$")
    public void i_set_the_rancid_perform_property_to_true_on_profile(String profile) throws Throwable {
        PssProfile pssProfile = PssProfile.findProfile(pssProps.getProfiles(), profile);
        pssProfile.getRancid().setPerform(true);

    }

    @Then("^I set rancid host to \"([^\"]*)\" on profile \"([^\"]*)\"$")
    public void i_set_the_rancid_host_to_on_profile(String host, String profile) throws Throwable {
        PssProfile pssProfile = PssProfile.findProfile(pssProps.getProfiles(), profile);
        pssProfile.getRancid().setHost(host);
    }


    @Then("^I set rancid dir to \"([^\"]*)\" on profile \"([^\"]*)\"$")
    public void i_set_rancid_dir_to_on_profile(String dir, String profile) throws Throwable {
        PssProfile pssProfile = PssProfile.findProfile(pssProps.getProfiles(), profile);
        pssProfile.getRancid().setDir(dir);
    }

    @Then("^I set rancid username to \"([^\"]*)\" on profile \"([^\"]*)\"$")
    public void i_set_rancid_username_to_on_profile(String username, String profile) throws Throwable {
        PssProfile pssProfile = PssProfile.findProfile(pssProps.getProfiles(), profile);
        pssProfile.getRancid().setUsername(username);
    }

    @Then("^I set rancid cloginrc to \"([^\"]*)\" on profile \"([^\"]*)\"$")
    public void i_set_rancid_cloginrc_to_on_profile(String cloginrc, String profile) throws Throwable {
        PssProfile pssProfile = PssProfile.findProfile(pssProps.getProfiles(), profile);
        pssProfile.getRancid().setCloginrc(cloginrc);
    }



    @Then("^I set the check perform property to true on profile \"([^\"]*)\"$")
    public void i_set_the_check_perform_property_to_true_on_profile(String profile) throws Throwable {
        PssProfile pssProfile = PssProfile.findProfile(pssProps.getProfiles(), profile);
        pssProfile.getCheck().setPerform(true);

    }

    @Then("^I add a check entry for \"([^\"]*)\" model \"([^\"]*)\" on profile \"([^\"]*)\"$")
    public void i_add_a_check_entry_for_model_on_profile(String device, DeviceModel model, String profile) throws Throwable {
        PssProfile pssProfile = PssProfile.findProfile(pssProps.getProfiles(), profile);
        DeviceEntry e = DeviceEntry.builder().device(device).model(model).build();
        pssProfile.getCheck().getDevices().add(e);
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

