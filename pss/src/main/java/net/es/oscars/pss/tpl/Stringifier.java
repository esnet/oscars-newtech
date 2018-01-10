package net.es.oscars.pss.tpl;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.prop.PssProps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class Stringifier {

    private Configuration fmCfg;

    private PssProps props;

    @Autowired
    public Stringifier(PssProps props) {
        this.props = props;
        this.configureTemplates();
    }

    public void configureTemplates() {

        fmCfg = new Configuration(Configuration.VERSION_2_3_22);
        fmCfg.setDefaultEncoding("UTF-8");
        fmCfg.setObjectWrapper(new DefaultObjectWrapper(Configuration.VERSION_2_3_22));
        fmCfg.setNumberFormat("computer");

        List<TemplateLoader> loaderList = new ArrayList<>();


        for (String templatePath : this.props.getTemplateDirs()) {
            try {
                FileTemplateLoader ftl = new FileTemplateLoader(new File(templatePath));
                loaderList.add(ftl);

            } catch (IOException ex) {
                log.error("IO exception for "+templatePath, ex);
            }
            log.info("will load templates from "+templatePath);

        }

        MultiTemplateLoader mtl = new MultiTemplateLoader(loaderList.toArray(new TemplateLoader[0]));
        fmCfg.setTemplateLoader(mtl);
    }

    public String stringify(Map<String, Object> root, String templateFilename) throws IOException, TemplateException {

        Writer writer = new StringWriter();
        Template tpl = fmCfg.getTemplate(templateFilename);
        tpl.process(root, writer);
        writer.flush();

        String output = writer.toString();
        List<String> lines = Arrays.asList(output.split("[\\r\\n]+"));

        return String.join("\n", lines);

    }
}
