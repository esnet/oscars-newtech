package net.es.oscars.pss.unit;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.AbstractPssTest;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.ctg.ExTests;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.help.ParamsLoader;
import net.es.oscars.pss.help.RouterTestSpec;
import net.es.oscars.pss.svc.ExCommandGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ExGenerationTest extends AbstractPssTest {

    @Autowired
    private ParamsLoader loader;

    @Autowired
    private ExCommandGenerator commandGen;


    @Test
    @Category({UnitTests.class, ExTests.class})
    public void makeExConfigs() throws ConfigException, IOException {

        log.info("testing EX build");
        List<RouterTestSpec> specs = loader.loadSpecs(CommandType.BUILD);

        for (RouterTestSpec spec : specs) {
            if (spec.getModel().equals(DeviceModel.JUNIPER_EX)) {
                if (!spec.getShouldFail()) {
                    log.info("testing "+spec.getFilename());
                    String config = commandGen.build(spec.getExParams());
                    log.info("config generated: \n" + config);
                }
            }
        }

        log.info("testing EX dismantle");

        specs = loader.loadSpecs(CommandType.DISMANTLE);

        for (RouterTestSpec spec : specs) {
            if (spec.getModel().equals(DeviceModel.JUNIPER_EX)) {
                if (!spec.getShouldFail()) {
                    log.info("testing "+spec.getFilename());
                    String config = commandGen.dismantle(spec.getExParams());
                    log.info("config generated: \n" + config);
                }
            }
        }
        log.info("done testing EX configs");

    }

}
