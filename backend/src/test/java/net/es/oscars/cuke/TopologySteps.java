package net.es.oscars.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.Delta;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.beans.VersionDelta;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.db.PortRepository;
import net.es.oscars.topo.db.VersionRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.pop.ConsistencyException;
import net.es.oscars.topo.pop.TopoPopulator;
import net.es.oscars.topo.svc.TopoLibrary;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.topo.svc.UpdateSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Slf4j
@Transactional
public class TopologySteps extends CucumberSteps {
    @Autowired
    private TopoService topoService;
    @Autowired
    private UpdateSvc updateSvc;

    @Autowired
    private DeviceRepository deviceRepo;
    @Autowired
    private PortAdjcyRepository adjcyRepo;
    @Autowired
    private VersionRepository versionRepo;

    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private CucumberWorld world;

    @Autowired
    private PortRepository portRepo;

    @Autowired
    private TopoPopulator topoPopulator;

    private Topology t;
    private VersionDelta vd;

    @Given("^I load topology from \"([^\"]*)\" and \"([^\"]*)\"$")
    public void i_load_topology_from_and(String arg1, String arg2) throws Throwable {
        this.t = topoPopulator.loadTopology(arg1, arg2);
        this.vd = TopoLibrary.compare(topoService.currentTopology(), t);
    }

    @Given("^I update the topology URN map after import$")
    public void update_topology_map() throws Throwable {

        topoService.updateTopo();
        world.topoBaseline = topoService.getTopoUrnMap();
        // log.info(world.topoBaseline.toString());
    }


    @Given("^I clear the topology$")
    public void clear_topo() throws Throwable {

        log.info("clearing adjacencies");
        adjcyRepo.deleteAll();
        adjcyRepo.flush();

        log.info("clearing devices");
        for (Device d : deviceRepo.findAll()) {
            HashSet<Port> ports = new HashSet<>(d.getPorts());
            for (Port p : ports) {
                log.info("deleting "+p.getUrn());
                d.getPorts().remove(p);
                portRepo.delete(p);
            }
            deviceRepo.delete(d);
            log.info("deleting "+d.getUrn());
        }

        deviceRepo.flush();


        log.info("clearing ports");
        portRepo.deleteAll();
        portRepo.flush();

        log.info("clearing versions");
        versionRepo.deleteAll();
        versionRepo.flush();
        world.topoBaseline = new HashMap<>();
    }

    @Then("^the current topology is empty$")
    public void the_current_topology_is_empty() throws Throwable {
        Topology c = topoService.currentTopology();
        assert c.getDevices().values().size() == 0;
        assert c.getPorts().values().size() == 0;
        assert c.getAdjcies().size() == 0;
    }

    @Then("^the \"([^\"]*)\" delta has (\\d+) entries \"([^\"]*)\"$")
    public void the_latest_delta_has_entries(String which, int num, String action) throws Throwable {
        Delta delta;
        Collection list;

        if (which.equals("device")) {
            delta = vd.getDeviceDelta();
        } else if (which.equals("port")) {
            delta = vd.getPortDelta();

        } else if (which.equals("adjcy")) {
            delta = vd.getAdjcyDelta();
        } else {
            throw new RuntimeException("bad which");
        }

        if (action.equals("added")) {
            list = delta.getAdded().values();
        } else if (action.equals("modified")) {
            list = delta.getModified().values();
        } else if (action.equals("removed")) {
            list = delta.getRemoved().values();
        } else if (action.equals("unchanged")) {
            list = delta.getUnchanged().values();
        } else {
            throw new RuntimeException("bad action");
        }
        assert num == list.size();
    }

    @When("^I merge the new topology$")
    public void i_merge_the_new_topology() throws Throwable {
        Version newVersion = updateSvc.nextVersion();
        Version current = topoService.currentVersion().orElseThrow(NoSuchElementException::new);
        try {
            updateSvc.mergeVersionDelta(vd, newVersion);
        } catch (ConsistencyException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());

        }
    }

}