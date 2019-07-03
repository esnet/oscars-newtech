package net.es.oscars.pss.equip;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.pss.params.ex.ExParams;
import net.es.oscars.pss.params.ex.ExVlan;
import net.es.oscars.pss.beans.*;
import net.es.oscars.pss.svc.KeywordValidator;
import net.es.oscars.pss.tpl.Assembler;
import net.es.oscars.pss.tpl.Stringifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class ExCommandGenerator {
    private Stringifier stringifier;
    private Assembler assembler;
    private KeywordValidator validator;

    @Autowired
    public ExCommandGenerator(Stringifier stringifier, Assembler assembler, KeywordValidator validator) {
        this.stringifier = stringifier;
        this.assembler = assembler;
        this.validator = validator;
    }

    public String dismantle(ExParams params) throws PSSException {
        this.protectVsNulls(params);
        this.verifyParams(params);
        ExTemplatePaths exp = ExTemplatePaths.builder()
                .vlan("ex/dismantle-ex-vlan.ftl")
                .build();
        return fill(exp, params);
    }

    public String build(ExParams params) throws PSSException {
        this.protectVsNulls(params);
        this.verifyParams(params);
        ExTemplatePaths exp = ExTemplatePaths.builder()
                .vlan("ex/build-ex-vlan.ftl")
                .build();
        return fill(exp, params);
    }

    private String fill(ExTemplatePaths etp, ExParams params) throws PSSException {

        String top = "ex/ex-top.ftl";

        List<String> fragments = new ArrayList<>();

        try {
            Map<String, Object> root = new HashMap<>();
            root.put("vlans", params.getVlans());
            String vlanConfig = stringifier.stringify(root, etp.getVlan());
            fragments.add(vlanConfig);
            return assembler.assemble(fragments, top);
        } catch (IOException | TemplateException ex) {
            log.error("templating error", ex);
            throw new PSSException("template system error");
        }
    }


    private void protectVsNulls(ExParams params) throws PSSException {

        if (params == null) {
            log.error("whoa whoa whoa there, no passing null params!");
            throw new PSSException("null Juniper EX params");
        }
        if (params.getVlans() == null) {
            throw new PSSException("null VLANs in Juniper EX params");
        }
        for (ExVlan vlan : params.getVlans()) {
            if (vlan.getIfces() == null) {
                throw new PSSException("null ifces in Juniper EX VLAN");
            }
            if (vlan.getDescription() == null) {
                vlan.setDescription("");
            }
            if (vlan.getName() == null) {
                throw new PSSException("null name in Juniper EX VLAN");
            }
            if (vlan.getVlanId() == null) {
                throw new PSSException("null vlan id in Juniper EX VLAN");
            }
        }
    }

    private void verifyParams(ExParams params) throws PSSException {
        StringBuilder errorStr = new StringBuilder("");
        Boolean hasError = false;

        Map<KeywordWithContext, KeywordValidationCriteria> keywordMap = new HashMap<>();
        KeywordValidationCriteria alphanum_criteria = KeywordValidationCriteria.builder()
                .format(KeywordFormat.ALPHANUMERIC_DASH_UNDERSCORE)
                .length(32)
                .build();

        if (params.getVlans().size() == 0) {
            errorStr.append("Empty VLAN list\n");
            hasError = true;
        }

        for (ExVlan vlan : params.getVlans()) {
            if (vlan.getIfces().size() == 0) {
                errorStr.append("Empty ifce list\n");
                hasError = true;
            }
            KeywordWithContext kwc_path = KeywordWithContext.builder()
                    .context("VLAN name").keyword(vlan.getName())
                    .build();
            keywordMap.put(kwc_path, alphanum_criteria);

            if (vlan.getVlanId() < 2 || vlan.getVlanId() > 4094) {
                String err = " vlan id " + vlan.getVlanId() + " out of range (2-4094)\n";
                errorStr.append(err);
                hasError = true;
            }
        }

        Map<KeywordWithContext, KeywordValidationResult> results = validator.validate(keywordMap);
        KeywordValidationResult overall = validator.gatherErrors(results);
        errorStr.append(overall.getError());
        if (!overall.getValid()) {
            hasError = true;
        }

        if (hasError) {
            log.error(errorStr.toString());
            throw new PSSException(errorStr.toString());
        }

    }


}
