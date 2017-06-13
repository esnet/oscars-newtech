package net.es.oscars.pss.svc;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.params.*;
import net.es.oscars.dto.pss.params.alu.*;
import net.es.oscars.pss.beans.AluTemplatePaths;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.tpl.Assembler;
import net.es.oscars.pss.tpl.Stringifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class AluCommandGenerator {
    private Stringifier stringifier;
    private Assembler assembler;

    @Autowired
    public AluCommandGenerator(Stringifier stringifier, Assembler assembler) {
        this.stringifier = stringifier;
        this.assembler = assembler;
    }

    public String dismantle(AluParams params) throws ConfigException {
        this.protectVsNulls(params);
        this.verifyAluQosParams(params);
        this.verifySdpIds(params);
        this.verifyPaths(params);

        AluTemplatePaths atp = AluTemplatePaths.builder()
                .lsp("alu/dismantle-alu-mpls-lsp.ftl")
                .qos("alu/dismantle-alu-qos.ftl")
                .sdp("alu/dismantle-alu-sdp.ftl")
                .path("alu/dismantle-alu-mpls-path.ftl")
                .vpls("alu/dismantle-alu-vpls-service.ftl")
                .loopback("alu/dismantle-alu-vpls-loopback.ftl")
                .build();
        return fill(atp, params, true);
    }

    public String build(AluParams params) throws ConfigException {
        if (params == null) {
            throw new ConfigException("null ALU params!");
        }
        this.protectVsNulls(params);
        this.verifyAluQosParams(params);
        this.verifySdpIds(params);
        this.verifyPaths(params);

        AluTemplatePaths atp = AluTemplatePaths.builder()
                .lsp("alu/build-alu-mpls-lsp.ftl")
                .qos("alu/build-alu-qos.ftl")
                .sdp("alu/build-alu-sdp.ftl")
                .path("alu/build-alu-mpls-path.ftl")
                .vpls("alu/build-alu-vpls-service.ftl")
                .loopback("alu/build-alu-vpls-loopback.ftl")
                .build();
        return fill(atp, params, false);
    }

    private String fill(AluTemplatePaths atp, AluParams params, boolean reverse) throws ConfigException {

        String top = "alu/alu-top.ftl";

        Map<String, Object> root = new HashMap<>();

        List<String> fragments = new ArrayList<>();

        try {

            if (params.getQoses() == null || params.getQoses().isEmpty()) {
                log.info("No QOS config (weird!)");
            } else {
                root.put("qosList", params.getQoses());
                root.put("apply", params.getApplyQos());
                String qosConfig = stringifier.stringify(root, atp.getQos());
                if (reverse) {
                    fragments.add(0, qosConfig);
                } else {
                    fragments.add(qosConfig);
                }
            }

            if (params.getPaths() == null || params.getPaths().isEmpty()) {
                log.info("No paths, skipping..");

            } else {
                root = new HashMap<>();
                root.put("paths", params.getPaths());
                String pathConfig = stringifier.stringify(root, atp.getPath());
                if (reverse) {
                    fragments.add(0, pathConfig);
                } else {
                    fragments.add(pathConfig);
                }
            }

            if (params.getLsps() == null || params.getLsps().isEmpty()) {
                log.info("No LSPs, skipping..");
            } else {
                root = new HashMap<>();
                root.put("lsps", params.getLsps());
                String lspConfig = stringifier.stringify(root, atp.getLsp());
                if (reverse) {
                    fragments.add(0, lspConfig);
                } else {
                    fragments.add(lspConfig);
                }
            }

            if (params.getSdps() == null || params.getSdps().isEmpty()) {
                log.info("No SDPs, skipping..");
            } else {
                root = new HashMap<>();
                root.put("sdps", params.getSdps());
                String sdpConfig = stringifier.stringify(root, atp.getSdp());
                if (reverse) {
                    fragments.add(0, sdpConfig);
                } else {
                    fragments.add(sdpConfig);
                }
            }

            if (params.getLoopbackInterface() == null) {
                log.info("No loopback, skipping..");
            } else {
                root = new HashMap<>();
                root.put("loopback_ifce_name", params.getLoopbackInterface());
                root.put("loopback_address", params.getLoopbackAddress());
                String loopbackCfg = stringifier.stringify(root, atp.getLoopback());
                if (reverse) {
                    fragments.add(0, loopbackCfg);
                } else {
                    fragments.add(loopbackCfg);
                }
            }


            root = new HashMap<>();
            root.put("vpls", params.getAluVpls());
            String vplsServiceConfig = stringifier.stringify(root, atp.getVpls());
            if (reverse) {
                fragments.add(0, vplsServiceConfig);
            } else {
                fragments.add(vplsServiceConfig);
            }
            return assembler.assemble(fragments, top);
        } catch (IOException | TemplateException ex) {
            log.error("templating error", ex);
            throw new ConfigException("template system error");
        }
    }


    private void protectVsNulls(AluParams params) {

        if (params == null) {
            log.error("whoa whoa whoa there, no passing null params!");
            params = AluParams.builder().build();
        }
        if (params.getAluVpls() == null) {
            params.setAluVpls(AluVpls.builder().build());
        }
        if (params.getSdps() == null) {
            params.setSdps(new ArrayList<>());
        }
        if (params.getAluVpls().getSdpToVcIds() == null) {
            params.getAluVpls().setSdpToVcIds(new ArrayList<>());
        }
        if (params.getSdps() == null) {
            params.setSdps(new ArrayList<>());
        }
        if (params.getPaths() == null) {
            params.setPaths(new ArrayList<>());
        }
        if (params.getLsps() == null) {
            params.setLsps(new ArrayList<>());
        }
    }

    private void verifyPaths(AluParams params) throws ConfigException {


        Set<String> lspNamesFromSdps = new HashSet<>();
        Set<String> lspNamesFromLsps = new HashSet<>();

        Set<String> pathNamesFromLsps = new HashSet<>();
        Set<String> pathNamesFromPaths = new HashSet<>();


        for (AluSdp sdp : params.getSdps()) {
            lspNamesFromSdps.add(sdp.getLspName());
        }
        for (Lsp lsp : params.getLsps()) {
            lspNamesFromLsps.add(lsp.getName());
            pathNamesFromLsps.add(lsp.getPathName());
        }
        for (MplsPath path : params.getPaths()) {
            pathNamesFromPaths.add(path.getName());
        }
        if (!lspNamesFromLsps.equals(lspNamesFromSdps)) {
            throw new ConfigException("LSP name mismatch");
        }
        if (!pathNamesFromLsps.equals(pathNamesFromPaths)) {
            throw new ConfigException("Path name mismatch");
        }


    }

    private void verifySdpIds(AluParams params) throws ConfigException {
        List<AluSdpToVcId> aluSdpToVcIds = params.getAluVpls().getSdpToVcIds();
        Set<Integer> sdpIdsA = new HashSet<>();
        Set<Integer> sdpIdsB = new HashSet<>();
        for (AluSdpToVcId aluSdpToVcId : aluSdpToVcIds) {
            sdpIdsA.add(aluSdpToVcId.getSdpId());
        }
        for (AluSdp sdp : params.getSdps()) {
            sdpIdsB.add(sdp.getSdpId());
        }
        if (!sdpIdsA.equals(sdpIdsB)) {
            throw new ConfigException("SDP ID mismatch!");
        }

    }

    private void verifyAluQosParams(AluParams params) throws ConfigException {
        List<AluSap> saps = params.getAluVpls().getSaps();
        List<AluQos> qoses = params.getQoses();
        Set<Integer> sapInQosIds = new HashSet<>();
        Set<Integer> sapEgQosIds = new HashSet<>();
        saps.forEach(sap -> {
            if (sap.getIngressQosId() != null) {
                sapInQosIds.add(sap.getIngressQosId());
            }
            if (sap.getEgressQosId() != null) {
                sapEgQosIds.add(sap.getEgressQosId());
            }
        });
        Set<Integer> inQosIds = new HashSet<>();
        Set<Integer> egQosIds = new HashSet<>();
        for (AluQos qos : qoses) {
            if (qos.getPolicyId() == null) {
                throw new ConfigException("qos policy id missing");
            }
            if (qos.getType() == null) {
                throw new ConfigException("qos type missing");
            }
            if (qos.getType().equals(AluQosType.SAP_INGRESS)) {
                if (inQosIds.contains(qos.getPolicyId())) {
                    throw new ConfigException("duplicate ingress qos policy id " + qos.getPolicyId());
                }
                inQosIds.add(qos.getPolicyId());
            } else if (qos.getType().equals(AluQosType.SAP_EGRESS)) {
                if (egQosIds.contains(qos.getPolicyId())) {
                    throw new ConfigException("duplicate egress qos policy id" + qos.getPolicyId());
                }
                egQosIds.add(qos.getPolicyId());
            }
        }
        boolean ok = true;
        String error = "";
        if (!sapInQosIds.equals(inQosIds)) {
            ok = false;
            error = "Ingress qos id mismatch";
        }
        if (!sapEgQosIds.equals(egQosIds)) {
            ok = false;
            error += " Egress qos id mismatch";
        }
        if (!ok) {
            throw new ConfigException(error);
        }


    }


}
