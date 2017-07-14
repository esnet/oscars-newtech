package net.es.oscars.cuke;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.PeriodBandwidth;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.beans.TopoUrn;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.*;

@Slf4j
@Transactional
public class BandwidthSteps extends CucumberSteps {
    private Map<String, Integer> baseline ;
    private Map<String, List<PeriodBandwidth>> periodBandwidthMap;

    @Given("^I set this bandwidth baseline$")
    public void i_set_this_bandwidth_baseline(Map<String, Integer> baselineBws) throws Throwable {
        baseline = baselineBws;
    }

    @Given("^I set these bandwidth reservations$")
    public void i_set_these_bandwidth_reservations(DataTable table) throws Throwable {
        periodBandwidthMap = new HashMap<>();
        List<List<String>> data = table.raw();
        for (List<String> row :data) {

            String urn = row.get(0);
            if (!periodBandwidthMap.keySet().contains(urn)) {
                periodBandwidthMap.put(urn, new ArrayList<>());
            }
            Integer bw = Integer.parseInt(row.get(1));
            Integer beg = Integer.parseInt(row.get(2));
            Integer end = Integer.parseInt(row.get(3));
            PeriodBandwidth periodBw = PeriodBandwidth.builder()
                    .bandwidth(bw)
                    .beginning(Instant.ofEpochSecond(beg))
                    .ending(Instant.ofEpochSecond(end))
                    .build();
            periodBandwidthMap.get(urn).add(periodBw);
        }
    }

    @Then("^the available bandwidth for \"([^\"]*)\" at (\\d+) is (\\d+)$")
    public void the_available_bandwidth_for_at_is(String urn, int when, int bw) throws Throwable {
        List<PeriodBandwidth> periodBws = periodBandwidthMap.get(urn);
        Integer baselineBw = this.baseline.get(urn);

        assert ResvService.availBandwidth(baselineBw, periodBws, Instant.ofEpochSecond(when)) == bw;
    }

    @Then("^the overall available bw for \"([^\"]*)\" is (\\d+)$")
    public void the_overall_available_bw_for_is(String urn, int bw) throws Throwable {
        List<PeriodBandwidth> periodBws = periodBandwidthMap.get(urn);
        Integer baselineBw = this.baseline.get(urn);
        assert ResvService.overallAvailBandwidth(baselineBw, periodBws) == bw;
    }


}