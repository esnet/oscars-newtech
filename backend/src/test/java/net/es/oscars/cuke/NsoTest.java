package net.es.oscars.cuke;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.nso.NsoRestServer;
import net.es.oscars.resv.ent.Vlan;
import net.es.oscars.resv.svc.ResvLibrary;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.TopoUrn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Transactional
public class NsoTest extends CucumberSteps {
    @Autowired
    private CucumberWorld world;

    @Autowired
    private NsoRestServer nsoRestServer;

    @Then("^I can get the OSCARS NSO config$")
    public void i_can_get_the_OSCARS_NSO_config() throws Throwable {
        nsoRestServer.getOscars();
    }

    @Then("^I can submit a test oscars$")
    public void submit_oscars_create() {
        String create = "<oscars xmlns=\"http://net.es/oscars\">\n" +
                "    <name>SOLIS</name>\n" +
                "    <serviceId>6500</serviceId>\n" +
                "    <device xmlns=\"http://net.es/oscars\">\n" +
                "      <name>netlab-7750sr12-rt2-es1</name>\n" +
                "      <fixture xmlns=\"http://net.es/oscars\">\n" +
                "        <ifce>10/1/4</ifce>\n" +
                "        <vlan-id>500</vlan-id>\n" +
                "      </fixture>\n" +
                "      <fixture xmlns=\"http://net.es/oscars\">\n" +
                "        <ifce>10/1/4</ifce>\n" +
                "        <vlan-id>600</vlan-id>\n" +
                "      </fixture>\n" +
                "    </device>" +
                "</oscars>";
        nsoRestServer.postOscars(create);

    }
    @Then("^I can delete the test oscars$")
    public void submit_oscars_delete() {
        nsoRestServer.deleteOscars("SOLIS");

    }

}