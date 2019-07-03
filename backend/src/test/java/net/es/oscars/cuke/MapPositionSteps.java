package net.es.oscars.cuke;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ctg.UnitTests;
import net.es.oscars.dto.topo.DeviceModel;
import net.es.oscars.topo.beans.DevicePositions;
import net.es.oscars.topo.beans.MapNode;
import net.es.oscars.topo.beans.Position;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.enums.DeviceType;
import net.es.oscars.web.rest.MapController;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.File;
import java.util.*;

@Slf4j
@Category({UnitTests.class})
public class MapPositionSteps extends CucumberSteps {
    @Autowired
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private CucumberWorld world;

    @Autowired
    private MapController mapController;

    private List<MapNode> mapNodes = new ArrayList<>();

    private List<Device> devices = new ArrayList<>();

    private Map<String, Position> positionMap;

    @Given("^I load my positionMap from \"([^\"]*)\"$")
    public void my_position_map(String path) {
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(path);

        try {
            positionMap = mapper.readValue(jsonFile, DevicePositions.class).getPositions();
        } catch (Exception ex) {
            world.add(ex);
        }
    }

    @Given("^I add nodes for devices \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void my_devices(String a, String b, String c) {

        Device aDevice = Device.builder()
                .urn(a)
                .model(DeviceModel.JUNIPER_MX)
                .type(DeviceType.ROUTER)
                .ipv4Address("10.0.0.1")
                .build();

        Device bDevice = Device.builder()
                .urn(b)
                .model(DeviceModel.JUNIPER_MX)
                .type(DeviceType.ROUTER)
                .ipv4Address("10.0.0.1")
                .build();

        Device cDevice = Device.builder()
                .urn(c)
                .model(DeviceModel.JUNIPER_MX)
                .type(DeviceType.ROUTER)
                .ipv4Address("10.0.0.1")
                .build();


        MapNode n1 = mapController.addNode(aDevice, positionMap);
        MapNode n2 = mapController.addNode(bDevice, positionMap);
        MapNode n3 = mapController.addNode(cDevice, positionMap);

        Collections.addAll(mapNodes, n1, n2, n3);
        Collections.addAll(devices, aDevice, bDevice, cDevice);
    }

    @Then("^I can verify my results$")
    public void verify() {
        MapNode n1 = mapNodes.get(0);
        MapNode n2 = mapNodes.get(1);
        MapNode n3 = mapNodes.get(2);

        Device a = devices.get(0);
        Device b = devices.get(0);
        Device c = devices.get(0);

        assert n1.getX().equals(positionMap.get(a.getUrn().split("-")[0]).getX());
        assert n1.getY().equals(positionMap.get(a.getUrn().split("-")[0]).getY());

        assert n2.getX().equals(positionMap.get(b.getUrn().split("-")[0]).getX());
        assert n2.getY().equals(positionMap.get(b.getUrn().split("-")[0]).getY());

        assert n3.getX().equals(positionMap.get(c.getUrn().split("-")[0]).getX());
        assert n3.getY().equals(positionMap.get(c.getUrn().split("-")[0]).getY());
    }
}