package net.es.oscars.cuke;

import cucumber.api.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.pop.TopoPopulator;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.transaction.Transactional;
import java.util.Map;

@Slf4j
@Transactional
public class TopologySteps extends CucumberSteps {
    @Autowired
    private TopoService topoService;

    @Autowired
    private DeviceRepository deviceRepo;
    @Autowired
    private PortAdjcyRepository adjcyRepo;

    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private CucumberWorld world;

    @Autowired
    private TopoPopulator topoPopulator;


    @Given("^I update the topology URN map after import$")
    public void update_topology_map() throws Throwable {

        topoService.updateTopo(deviceRepo.findAll(), adjcyRepo.findAll());
        world.topoBaseline = topoService.getTopoUrnMap();
    }

    @Given("^I import devices from \"([^\"]*)\"$")
    public void i_import_devices_from(String path) throws Throwable {
        topoPopulator.importDevices(true, path);
    }

    @Given("^I import adjacencies from \"([^\"]*)\"$")
    public void i_import_adjacencies_from(String path) throws Throwable {
        topoPopulator.importAdjacencies(true, path);
    }

}