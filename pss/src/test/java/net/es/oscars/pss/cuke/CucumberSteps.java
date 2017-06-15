package net.es.oscars.pss.cuke;

import net.es.oscars.pss.PssTestConfiguration;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(locations = "file:config/test/application.properties")
@ContextConfiguration(
        loader = SpringBootContextLoader.class,
        classes = PssTestConfiguration.class)
public class CucumberSteps {

}

