package net.es.oscars.cuke;

import cucumber.api.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.pop.TopoPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.transaction.Transactional;

@Slf4j
@Transactional
public class TopologySteps extends CucumberSteps {

    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private CucumberWorld world;

    @Autowired
    private TopoPopulator topoPopulator;

    @Given("^I import devices from \"([^\"]*)\"$")
    public void i_import_devices_from(String path) throws Throwable {
        topoPopulator.importDevices(true, path);
    }

    @Given("^I import adjacencies from \"([^\"]*)\"$")
    public void i_import_adjacencies_from(String path) throws Throwable {
        topoPopulator.importAdjacencies(true, path);
    }

}