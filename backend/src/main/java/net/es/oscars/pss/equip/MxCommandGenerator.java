package net.es.oscars.pss.equip;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.pss.params.MplsHop;
import net.es.oscars.pss.params.MplsPath;
import net.es.oscars.pss.params.mx.*;
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
public class MxCommandGenerator {
    private Stringifier stringifier;
    private Assembler assembler;
    private KeywordValidator validator;


    @Autowired
    public MxCommandGenerator(Stringifier stringifier, Assembler assembler, KeywordValidator validator) {
        this.stringifier = stringifier;
        this.assembler = assembler;
        this.validator = validator;
    }

    public String dismantle(MxParams params) throws PSSException {
        this.protectVsNulls(params);
        this.verifyParams(params);

        MxTemplatePaths mtp = MxTemplatePaths.builder()
                .lsp("mx/dismantle-mx-mpls-lsp.ftl")
                .qos("mx/dismantle-mx-qos.ftl")
                .sdp("mx/dismantle-mx-sdp.ftl")
                .ifces("mx/dismantle-mx-ifces.ftl")
                .path("mx/dismantle-mx-mpls-path.ftl")
                .vpls("mx/dismantle-mx-vpls-service.ftl")
                .build();
        return fill(mtp, params);
    }

    public String build(MxParams params) throws PSSException {
        this.protectVsNulls(params);
        this.verifyParams(params);

        MxTemplatePaths mtp = MxTemplatePaths.builder()
                .lsp("mx/build-mx-mpls-lsp.ftl")
                .qos("mx/build-mx-qos.ftl")
                .sdp("mx/build-mx-sdp.ftl")
                .ifces("mx/build-mx-ifces.ftl")
                .path("mx/build-mx-mpls-path.ftl")
                .vpls("mx/build-mx-vpls-service.ftl")
                .build();
        return fill(mtp, params);
    }

    public String show(MxParams params) throws PSSException {
        String top = "mx/show.ftl";

        Map<String, Object> root = new HashMap<>();
        root.put("paths", params.getPaths());
        root.put("lsps", params.getLsps());
        root.put("ifces", params.getIfces());
        root.put("vpls", params.getMxVpls());
        try {
            return stringifier.stringify(root, top);
        } catch (IOException | TemplateException ex) {
            log.error("templating error", ex);
            throw new PSSException("template system error");
        }

    }
    private String fill(MxTemplatePaths tp, MxParams params) throws PSSException {

        String top = "mx/mx-top.ftl";

        Map<String, Object> root;
        List<String> fragments = new ArrayList<>();
        try {
            root = new HashMap<>();
            root.put("paths", params.getPaths());
            String pathConfig = stringifier.stringify(root, tp.getPath());
            fragments.add(pathConfig);

            root = new HashMap<>();
            root.put("lsps", params.getLsps());
            String lspConfig = stringifier.stringify(root, tp.getLsp());
            fragments.add(lspConfig);

            root = new HashMap<>();
            root.put("ifces", params.getIfces());
            root.put("vpls", params.getMxVpls());
            String ifcesConfig = stringifier.stringify(root, tp.getIfces());
            fragments.add(ifcesConfig);

            root = new HashMap<>();
            root.put("qoses", params.getQos());
            String qosConfig = stringifier.stringify(root, tp.getQos());
            fragments.add(qosConfig);

            root = new HashMap<>();
            root.put("mxLsps", params.getLsps());
            root.put("vpls", params.getMxVpls());
            String sdpConfig = stringifier.stringify(root, tp.getSdp());
            fragments.add(sdpConfig);

            root = new HashMap<>();
            root.put("vpls", params.getMxVpls());
            root.put("ifces", params.getIfces());
            String vplsServiceConfig = stringifier.stringify(root, tp.getVpls());
            fragments.add(vplsServiceConfig);


            return assembler.assemble(fragments, top);
        } catch (IOException | TemplateException ex) {
            log.error("templating error", ex);
            throw new PSSException("template system error");
        }
    }


    private void protectVsNulls(MxParams params) throws PSSException {

        if (params == null) {
            log.error("whoa whoa whoa there, no passing null params!");
            throw new PSSException("null Juniper MX params");
        }
        if (params.getMxVpls() == null) {
            throw new PSSException("null Juniper MX VPLS");
        }
        if (params.getPaths() == null) {
            params.setPaths(new ArrayList<>());
        }
        if (params.getQos() == null) {
            params.setQos(new ArrayList<>());
        }
        if (params.getLsps() == null) {
            params.setLsps(new ArrayList<>());
        }

    }

    private void verifyParams(MxParams params) throws PSSException {
        // verify ifces
        StringBuilder errorStr = new StringBuilder("");
        Boolean hasError = false;
        Map<KeywordWithContext, KeywordValidationCriteria> keywordMap = new HashMap<>();
        KeywordValidationCriteria alphanum_criteria = KeywordValidationCriteria.builder()
                .format(KeywordFormat.ALPHANUMERIC_DASH_UNDERSCORE)
                .length(32)
                .build();
        KeywordValidationCriteria ip_criteria = KeywordValidationCriteria.builder()
                .format(KeywordFormat.IPV4_ADDRESS)
                .length(32)
                .build();

        Set<String> pathNames = new HashSet<>();
        for (MplsPath path : params.getPaths()) {
            KeywordWithContext kwc_path = KeywordWithContext.builder()
                    .context("MPLS path name").keyword(path.getName())
                    .build();
            keywordMap.put(kwc_path, alphanum_criteria);
            pathNames.add(path.getName());
            for (MplsHop hop : path.getHops()) {
                KeywordWithContext kwc_hop_addr = KeywordWithContext.builder()
                        .context("MPLS hop address").keyword(hop.getAddress())
                        .build();
                keywordMap.put(kwc_hop_addr, ip_criteria);
            }
        }
        KeywordValidationResult kwr = validator.verifyIfces(params.getIfces());
        if (!kwr.getValid()) {
            hasError = true;
            errorStr.append(kwr.getError());
        }


        Set<String> qosFilters = new HashSet<>();
        if (params.getQos() == null) {
            hasError = true;
            errorStr.append("Null QoS");
        } else {
            for (MxQos qos : params.getQos()) {
                qosFilters.add(qos.getFilterName());
            }
        }


        Set<String> lspFilters = new HashSet<>();
        Set<String> lspPathNames = new HashSet<>();
        for (MxLsp lsp : params.getLsps()) {
            lspPathNames.add(lsp.getLsp().getPathName());
            if (!pathNames.contains(lsp.getLsp().getPathName())) {
                String err = " LSP path name " + lsp.getLsp().getPathName()+ " not defined in paths\n";
                errorStr.append(err);
                hasError = true;
            }
            KeywordWithContext kwc_lsp_path_name = KeywordWithContext.builder()
                    .context("LSP path name").keyword(lsp.getLsp().getPathName())
                    .build();
            KeywordWithContext kwc_lsp_name = KeywordWithContext.builder()
                    .context("LSP name").keyword(lsp.getLsp().getName())
                    .build();
            KeywordWithContext kwc_lsp_nei = KeywordWithContext.builder()
                    .context("LSP neighbor").keyword(lsp.getNeighbor())
                    .build();
            KeywordWithContext kwc_lsp_to = KeywordWithContext.builder()
                    .context("LSP to").keyword(lsp.getLsp().getTo())
                    .build();

            keywordMap.put(kwc_lsp_name, alphanum_criteria);
            keywordMap.put(kwc_lsp_path_name, alphanum_criteria);
            keywordMap.put(kwc_lsp_nei, ip_criteria);
            keywordMap.put(kwc_lsp_to, ip_criteria);

            if (!qosFilters.contains(lsp.getPoliceFilter())) {
                String err = "LSP to " + lsp.getNeighbor() + " :  uses a police filter not set in QoS\n";
                errorStr.append(err);
                hasError = true;
            }
            KeywordWithContext kwc_lsp_police_filter = KeywordWithContext.builder()
                    .context("LSP police filter").keyword(lsp.getPoliceFilter())
                    .build();

            keywordMap.put(kwc_lsp_police_filter, alphanum_criteria);

            lspFilters.add(lsp.getPoliceFilter());
        }
        for (String pathName : pathNames) {
            if (!lspPathNames.contains(pathName)) {
                String err = " path name " + pathName+ " not used by LSP\n";
                errorStr.append(err);
                hasError = true;
            }
        }

        for (MxQos qos : params.getQos()) {
            if (!lspFilters.contains(qos.getFilterName())) {
                String err = "QOS filter " + qos.getFilterName() + " is not used by any LSPs\n";
                errorStr.append(err);
                hasError = true;
            }
            KeywordWithContext qos_filter_name = KeywordWithContext.builder()
                    .context("QOS filter name").keyword(qos.getFilterName())
                    .build();
            KeywordWithContext qos_policer_name = KeywordWithContext.builder()
                    .context("QOS filter name").keyword(qos.getPolicerName())
                    .build();

            keywordMap.put(qos_filter_name, alphanum_criteria);
            keywordMap.put(qos_policer_name, alphanum_criteria);
        }

        MxVpls vpls = params.getMxVpls();

        // this is allowed to be null
        if (vpls.getLoopback() != null) {
            KeywordWithContext kwc_vpls_loopback = KeywordWithContext.builder()
                    .context("VPLS loopback").keyword(vpls.getLoopback())
                    .build();
            keywordMap.put(kwc_vpls_loopback, ip_criteria);
        }

        if (vpls.getProtectEnabled() == null) {
            errorStr.append("vpls protect enabled is null");
            hasError = true;

        } else{
            if (vpls.getProtectEnabled()) {
                if (vpls.getProtectVcId() == null) {
                    errorStr.append("vpls protect enabled but protect vc id is null");

                } else {
                    if (vpls.getProtectVcId() <= 0 || vpls.getProtectVcId() > 65534) {
                        errorStr.append("protect vcid out of range");
                        hasError = true;
                    }

                }
            }
        }
        if (vpls.getVcId() <= 0 || vpls.getVcId() > 65534) {
            errorStr.append("vcid out of range");
            hasError = true;
        }

        KeywordWithContext kwc_vpls_pol_name= KeywordWithContext.builder()
                .context("VPLS policy name").keyword(vpls.getPolicyName())
                .build();
        KeywordWithContext kwc_vpls_stats_filter = KeywordWithContext.builder()
                .context("VPLS stats filter").keyword(vpls.getStatsFilter())
                .build();
        KeywordWithContext kwc_vpls_svc_name = KeywordWithContext.builder()
                .context("VPLS service name").keyword(vpls.getServiceName())
                .build();

        keywordMap.put(kwc_vpls_pol_name, alphanum_criteria);
        keywordMap.put(kwc_vpls_stats_filter, alphanum_criteria);
        keywordMap.put(kwc_vpls_svc_name, alphanum_criteria);

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
