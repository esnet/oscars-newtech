package net.es.oscars.pss.cuke;

import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.help.ParamsLoader;
import net.es.oscars.pss.help.RouterTestSpec;
import net.es.oscars.pss.svc.AluCommandGenerator;
import net.es.oscars.pss.svc.ExCommandGenerator;
import net.es.oscars.pss.svc.MxCommandGenerator;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;


@Category({UnitTests.class})
@Slf4j
public class ModelSteps {

    @Autowired
    private MxCommandGenerator mxCommandGen;

    @Autowired
    private ExCommandGenerator exCommandGen;

    @Autowired
    private AluCommandGenerator aluCommandGen;

    @Autowired
    private ParamsLoader loader;
    @Autowired
    private CucumberWorld world;

    @When("^I \"([^\"]*)\" on the \"([^\"]*)\" command generator with the test commands$")
    public void i_EXEC_on_the_MODEL_command_generator_with_the_test_commands(CommandType t, DeviceModel m) {
        for (RouterTestSpec spec : loader.getSpecs()) {
            log.info("file: "+spec.getFilename());
            try {
                String config;
                if (t.equals(CommandType.BUILD)) {
                    switch (m) {
                        case ALCATEL_SR7750:
                            config = aluCommandGen.build(spec.getAluParams());
                            break;
                        case JUNIPER_MX:
                            config = mxCommandGen.build(spec.getMxParams());
                            break;
                        case JUNIPER_EX:
                            config = exCommandGen.build(spec.getExParams());
                            break;
                    }

                } else if (t.equals(CommandType.DISMANTLE)) {
                    switch (m) {
                        case ALCATEL_SR7750:
                            config = aluCommandGen.dismantle(spec.getAluParams());
                            break;
                        case JUNIPER_MX:
                            config = mxCommandGen.dismantle(spec.getMxParams());
                            break;
                        case JUNIPER_EX:
                            config = exCommandGen.dismantle(spec.getExParams());
                            break;
                    }
                }
            } catch (ConfigException ex) {
                world.add(ex);
            }
        }

    }

}

