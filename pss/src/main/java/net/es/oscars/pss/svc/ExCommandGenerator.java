package net.es.oscars.pss.svc;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.params.ex.ExParams;
import net.es.oscars.dto.pss.params.ex.ExVlan;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.beans.ExTemplatePaths;
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

    @Autowired
    public ExCommandGenerator(Stringifier stringifier, Assembler assembler) {
        this.stringifier = stringifier;
        this.assembler = assembler;
    }

    public String dismantle(ExParams params) throws ConfigException {
        this.protectVsNulls(params);
        this.verifyVlans(params);
        ExTemplatePaths exp = ExTemplatePaths.builder()
                .vlan("ex/dismantle-ex-vlan.ftl")
                .build();
        return fill(exp, params);
    }

    public String build(ExParams params) throws ConfigException {
        this.protectVsNulls(params);
        this.verifyVlans(params);
        ExTemplatePaths exp = ExTemplatePaths.builder()
                .vlan("ex/build-ex-vlan.ftl")
                .build();
        return fill(exp, params);
    }

    private String fill(ExTemplatePaths etp, ExParams params) throws ConfigException {

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
            throw new ConfigException("template system error");
        }
    }


    private void protectVsNulls(ExParams params) throws ConfigException {

        if (params == null) {
            log.error("whoa whoa whoa there, no passing null params!");
            throw new ConfigException("null Juniper EX params");
        }
        if (params.getVlans() == null) {
            throw new ConfigException("null VLANs in Juniper EX params");
        }
        for (ExVlan vlan : params.getVlans()) {
            if (vlan.getIfces() == null ) {
                throw new ConfigException("null ifces in Juniper EX VLAN");
            }
            if (vlan.getDescription() == null) {
                vlan.setDescription("");
            }
            if (vlan.getName() == null) {
                throw new ConfigException("null name in Juniper EX VLAN");
            }
            if (vlan.getVlanId() == null) {
                throw new ConfigException("null vlan id in Juniper EX VLAN");
            }
        }
    }

    private void verifyVlans(ExParams params) throws ConfigException {
        if (params.getVlans().size() == 0) {
            throw new ConfigException("Empty VLAN list for Juniper EX vlan");
        }
        for (ExVlan vlan : params.getVlans()) {
            if (vlan.getIfces().size() == 0) {
                throw new ConfigException("empty ifce list Juniper EX VLAN");
            }
            if (vlan.getName().length() == 0) {
                throw new ConfigException("empty name in Juniper EX VLAN");
            }
            if (vlan.getVlanId() < 2 || vlan.getVlanId() > 4094) {
                throw new ConfigException("vlan id out of range in Juniper EX VLAN");
            }
        }
    }


}
