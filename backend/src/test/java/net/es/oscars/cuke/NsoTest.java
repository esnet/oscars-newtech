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

}