package net.es.oscars.cuke;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.Vlan;
import net.es.oscars.resv.svc.ResvLibrary;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.TopoUrn;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.*;

@Slf4j
@Transactional
public class VlanSteps extends CucumberSteps {
    @Autowired
    private CucumberWorld world;


    @Given("^I set this vlan baseline$")
    public void i_set_this_vlan_baseline(Map<String, String> baselineVlans) throws Throwable {
        world.vlanBaseline = new HashMap<>();
        baselineVlans.forEach((urn, vlanExpr)-> {
            Set<IntRange> reservable = IntRange.fromExpression(vlanExpr);
            TopoUrn tu = TopoUrn.builder().urn(urn).reservableVlans(reservable).build();
            world.vlanBaseline.put(urn, tu);
        });
    }

    @Given("^I set these eternal vlan reservations$")
    public void i_set_these_eternal_vlan_reservations(DataTable table) throws Throwable {
        world.reservedVlans = new ArrayList<>();
        List<List<String>> data = table.raw();
        for (List<String> row :data) {

            String urn = row.get(0);
            Integer vlan = Integer.parseInt(row.get(1));
            Vlan v = Vlan.builder().vlanExpression("").vlan(vlan).urn(urn).build();
            world.reservedVlans.add(v);
        }

    }

    @Then("^the available vlans for \"([^\"]*)\" are \"([^\"]*)\"$")
    public void the_available_vlans_for_are(String portUrn, String expr) throws Throwable {
        Map<String, Set<IntRange>> availVlanMap = ResvLibrary.availableVlanMap(world.vlanBaseline, world.reservedVlans);
        assert IntRange.asString(availVlanMap.get(portUrn)).equals(expr);

    }

}