package net.es.oscars.cuke;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.Design;
import net.es.oscars.resv.db.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.transaction.Transactional;
import java.io.File;
import java.util.Optional;

@Slf4j
@Transactional
public class DesignSteps extends CucumberSteps {

    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private DesignRepository designRepo;

    @Autowired
    private CucumberWorld world;

    private Design design;

    @Given("^I load a design from \"([^\"]*)\"$")
    public void my_JSON_formatted_design_is_at(String path) {
        ObjectMapper mapper = builder.build();
        File f = new File(path);
        try {
            design = mapper.readValue(f, Design.class);
            // log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(design));
        } catch (Exception ex) {
            // log.error("caught: "+ex.getMessage());
            // ex.printStackTrace();
            world.add(ex);
        }
    }

    @Then("^I can persist the design")
    public void i_can_persist_the_design() throws Throwable {
        designRepo.save(design);
    }

    @When("^I load the design from the repository$")
    public void i_load_the_design() throws Throwable {
        Optional<Design> maybeDesign = designRepo.findByDesignId(design.getDesignId());
        assert maybeDesign.isPresent();

        // ObjectMapper mapper = builder.build();
        // log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(design));
    }



}