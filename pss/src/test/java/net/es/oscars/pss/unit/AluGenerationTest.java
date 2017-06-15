package net.es.oscars.pss.unit;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.AbstractPssTest;
import net.es.oscars.pss.ctg.AluTests;
import net.es.oscars.pss.ctg.UnitTests;
import net.es.oscars.pss.help.ParamsLoader;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.help.RouterTestSpec;
import net.es.oscars.pss.svc.AluCommandGenerator;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

@Slf4j
public class AluGenerationTest {

    @Autowired
    private ParamsLoader loader;

    @Autowired
    private AluCommandGenerator commandGen;


    @Category({UnitTests.class, AluTests.class})
    public void makeAluConfigs() throws ConfigException, IOException {

        log.info("testing build");
        loader.loadSpecs(CommandType.BUILD);
        List<RouterTestSpec> specs = loader.getSpecs();

        for (RouterTestSpec spec : specs) {
            if (spec.getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                if (!spec.getShouldFail()) {
                    log.info("testing "+spec.getFilename());
                    String config = commandGen.build(spec.getAluParams());
                    log.info("config generated: \n" + config);
                }
            }
        }

        log.info("testing dismantle");

        loader.loadSpecs(CommandType.DISMANTLE);
        specs = loader.getSpecs();

        for (RouterTestSpec spec : specs) {
            if (spec.getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                if (!spec.getShouldFail()) {
                    log.info("testing "+spec.getFilename());
                    String config = commandGen.dismantle(spec.getAluParams());
                    log.info("config generated: \n" + config);
                }
            }
        }
        log.info("done testing alu configs");

    }

    @Category({UnitTests.class, AluTests.class})
    public void failToMakeAluConfig() throws ConfigException, IOException {
        log.info("testing things that should fail");

        loader.loadSpecs(CommandType.BUILD);
        List<RouterTestSpec> specs = loader.getSpecs();
        boolean anyFailScenariosFound = false;


        for (RouterTestSpec spec : specs) {
            if (spec.getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                if (spec.getShouldFail()) {
                    log.info("testing "+spec.getFilename());
                    commandGen.build(spec.getAluParams());
                    anyFailScenariosFound = true;
                }
            }
        }
        if (!anyFailScenariosFound) {
            throw new ConfigException("throwing an exception anyway");
        }
        log.info("done testing things that should fail");

    }
}
