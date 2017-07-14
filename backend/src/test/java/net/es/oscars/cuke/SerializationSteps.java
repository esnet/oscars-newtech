package net.es.oscars.cuke;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.Blueprint;
import net.es.oscars.resv.db.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.transaction.Transactional;
import java.io.File;
import java.util.List;

@Slf4j
@Transactional
public class SerializationSteps extends CucumberSteps {

    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private BlueprintRepository requestRepo;
    @Autowired
    private ScheduleRepository schedRepo;
    @Autowired
    private PipeRepository pipeRepo;
    @Autowired
    private FixtureRepository fixtureRepo;
    @Autowired
    private JunctionRepository junctionRepo;
    @Autowired
    private VlanRepository vlanRepo;

    private Blueprint req;

    @When("^I clear the request repository$")
    public void i_clear_the_request_repo() throws Throwable {
        requestRepo.deleteAll();
    }


    @Given("^I can load my JSON-formatted request from \"([^\"]*)\"$")
    public void my_JSON_formatted_request_is_at(String path) throws Throwable {
        ObjectMapper mapper = builder.build();
        File f = new File(path);
        req = mapper.readValue(f, Blueprint.class);
//        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(req));

    }

    @Then("^I can persist the request$")
    public void i_can_persist_the_request() throws Throwable {
        requestRepo.save(req);
    }

    @When("^I load the request from the repository$")
    public void i_load_the_request() throws Throwable {
        List<Blueprint> maybeReq = requestRepo.findByConnectionId(req.getConnectionId());
        assert maybeReq.size() == 1;
        req = maybeReq.get(0);

//        ObjectMapper mapper = builder.build();
//        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(req));
    }

    @Then("^the \"([^\"]*)\" repository has (\\d+) entries$")
    public void the_repository_has_entries(String repoName, int num) throws Throwable {
        if (repoName.equals("junction")) {
            assert junctionRepo.findAll().size() == num;
        }
        if (repoName.equals("pipe")) {
            assert pipeRepo.findAll().size() == num;
        }
        if (repoName.equals("schedule")) {
            assert schedRepo.findAll().size() == num;
        }
        if (repoName.equals("fixture")) {
            assert fixtureRepo.findAll().size() == num;
        }
        if (repoName.equals("vlan")) {
            assert vlanRepo.findAll().size() == num;
        }
    }


}