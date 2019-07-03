package net.es.oscars.pss.tpl;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class Assembler {

    @Autowired
    private Stringifier stringifier;

    public String assemble(List<String> fragments, String templateFilename)
            throws IOException, TemplateException {

        Map<String, Object> root = new HashMap<>();
        root.put("fragments", fragments);

        return stringifier.stringify(root, templateFilename);

    }
}
