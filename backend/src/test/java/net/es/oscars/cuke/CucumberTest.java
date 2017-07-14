package net.es.oscars.cuke;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import net.es.oscars.AbstractBackendTest;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(format = {
        "pretty",
        "html:target/site/cucumber/cucumber",
        "json:target/failsafe-reports/cucumber.json" },
        strict = true)
public class CucumberTest extends AbstractBackendTest {

}

