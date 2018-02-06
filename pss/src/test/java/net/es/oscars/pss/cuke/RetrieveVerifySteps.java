package net.es.oscars.pss.cuke;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.VerifyRequest;
import net.es.oscars.dto.pss.cmd.VerifyResponse;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.VerifyException;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.svc.ConfigVerifier;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;


@Category({UnitTests.class})
@Slf4j
public class RetrieveVerifySteps extends CucumberSteps {
    @Autowired
    private CucumberWorld world;

    @Autowired
    private ConfigVerifier verifier;

    private String config;

    @When("^I retrieve config for \"([^\"]*)\" model \"([^\"]*)\"$")
    public void i_retrieve_config_for_model(String device, DeviceModel model) throws Throwable {
        try {
            config = verifier.collectConfig(device, model);
//            log.info(config);

        } catch (VerifyException ex) {
            world.add(ex);
            throw ex;

        }
    }

    @Then("^I verify that \"([^\"]*)\" must be \"([^\"]*)\"$")
    public void i_verify_that_must_be(String path, String present) throws Throwable {

        VerifyRequest req = VerifyRequest.builder()
                .device("foo")
                .model(DeviceModel.ALCATEL_SR7750)
                .mustBeAbsent(new ArrayList<>())
                .mustBePresent(new ArrayList<>())
                .mustContainValue(new HashMap<>())
                .build();
        if (present.equals("PRESENT")) {
            req.getMustBePresent().add(path);
        } else {
            req.getMustBeAbsent().add(path);
        }

        ObjectMapper m = new ObjectMapper();
        VerifyResponse resp = verifier.verifyConfigAgrees(config, req);
        log.info("p: "+m.writerWithDefaultPrettyPrinter().writeValueAsString(resp.getPresent()));
        assert resp.getPresent().containsKey(path);
        if (present.equals("PRESENT")) {
            assert resp.getPresent().get(path).equals(true);
        } else {
            assert resp.getPresent().get(path).equals(false);
        }
    }

    @Then("^I verify that \"([^\"]*)\" must contain \"([^\"]*)\"$")
    public void i_verify_that_must_contain(String path, String value) throws Throwable {
        // Write code here that turns the phrase above into concrete actions

        VerifyRequest req = VerifyRequest.builder()
                .device("foo")
                .model(DeviceModel.ALCATEL_SR7750)
                .mustBeAbsent(new ArrayList<>())
                .mustBePresent(new ArrayList<>())
                .mustContainValue(new HashMap<>())
                .build();
        req.getMustContainValue().put(path, value);
        ObjectMapper m = new ObjectMapper();
        VerifyResponse resp = verifier.verifyConfigAgrees(config, req);
        log.info("p: "+m.writerWithDefaultPrettyPrinter().writeValueAsString(resp.getPresent()));
        assert resp.getPresent().containsKey(path);
        assert resp.getPresent().get(path).equals(value);


    }

}

