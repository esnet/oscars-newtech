package net.es.oscars.cuke;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.ctg.UnitTests;
import net.es.oscars.pss.beans.TemplateOutput;
import net.es.oscars.pss.svc.ConfigGenService;
import net.es.oscars.pss.tpl.Stringifier;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Category({UnitTests.class})
public class TemplateVersionSteps extends CucumberSteps {
    @Autowired
    private ConfigGenService cgs;

    @Autowired
    private Stringifier stringifier;

    private List<String> templateFilenames;

    private String templateDir = null;
    @Autowired
    private PssProperties pssProps;

    @Given("^I set the template directory to \"([^\"]*)\"$")
    public void i_set_the_template_directory_to(String dir) throws Throwable {
        String[] tds = {dir};
        templateDir = dir;
        pssProps.setTemplateDirs(tds);
        stringifier.configureTemplates();
    }

    @When("^I load the template \"([^\"]*)\"$")
    public void i_load_the_template(String tfn) throws Throwable {
        this.templateFilenames = new ArrayList<>();
        this.templateFilenames.add(tfn);
    }


    @Then("^the version tag for loaded template\\(s\\) \"([^\"]*)\" consistent$")
    public void the_version_tag_for_loaded_template_s_consistent(String maybe) throws Throwable {
        boolean encounteredException;
        try {
            String cv = cgs.consistentVersion(templateFilenames);
            encounteredException = false;
        } catch (PSSException ex) {
            encounteredException = true;
        }
        if (maybe.equals("is")) {
            if (encounteredException) {
                throw new PSSException("should not have encountered exception but did");
            }
        } else {
            if (!encounteredException) {
                throw new PSSException("should have encountered exception but did not");
            }
        }
    }

    @Then("^the template processed output does not contain the version tag$")
    public void the_template_processed_output_does_not_contain_the_version_tag() throws Throwable {
        for (String tfn : templateFilenames) {
            TemplateOutput to = stringifier.stringify(null, tfn);
            for (String line : to.getProcessedLines()) {
                if (line.contains("@version")) {
                    throw new PSSException("found @version in processed output");
                }
            }
        }
    }


    @When("^I load all templates in the template directory$")
    public void i_load_all_templates_in_the_template_directory() throws Throwable {
        this.templateFilenames = new ArrayList<>();
        Files.list(Paths.get(templateDir))
                .filter(Files::isRegularFile)
                .forEach(p -> this.templateFilenames.add(p.getFileName().toString()));
        for (String f: this.templateFilenames) {
            log.info(f);
        }
    }


}