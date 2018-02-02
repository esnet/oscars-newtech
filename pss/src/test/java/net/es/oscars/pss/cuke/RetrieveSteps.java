package net.es.oscars.pss.cuke;

import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.enums.DeviceModel;
import org.json.JSONObject;
import net.es.oscars.pss.beans.VerifyException;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.svc.ConfigVerifier;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;


@Category({UnitTests.class})
@Slf4j
public class RetrieveSteps extends CucumberSteps {
    @Autowired
    private CucumberWorld world;

    @Autowired
    private ConfigVerifier verifier;


    @When("^I retrieve config for \"([^\"]*)\" model \"([^\"]*)\"$")
    public void i_retrieve_config_for_model(String device, DeviceModel model) throws Throwable {
        try {
            JSONObject config = verifier.collectConfig(device, model);
        } catch (VerifyException ex) {
            world.add(ex);
            throw ex;

        }


    }


}

