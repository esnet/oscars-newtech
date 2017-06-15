package net.es.oscars.pss.cuke;

import cucumber.api.CucumberOptions;
import net.es.oscars.pss.AbstractPssTest;
import net.es.oscars.pss.ctg.UnitTests;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(format = {
        "pretty",
        "html:target/site/cucumber/cucumber",
        "json:target/failsafe-reports/cucumber.json" },
        strict = true)
@Category({UnitTests.class})
public class CucumberTest extends AbstractPssTest {

}

