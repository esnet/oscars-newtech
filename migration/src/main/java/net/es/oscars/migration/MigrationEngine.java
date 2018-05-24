package net.es.oscars.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.cmd.CommandType;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.migration.input.*;
import net.es.oscars.pss.db.RouterCommandsRepository;
import net.es.oscars.pss.ent.RouterCommands;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.BuildMode;
import net.es.oscars.resv.enums.EthFixtureType;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.resv.enums.State;
import net.es.oscars.resv.svc.ConnService;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.enums.CommandParamType;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Component
@Slf4j
@Transactional
public class MigrationEngine {
    @Autowired
    protected ConnectionRepository connRepo;
    @Autowired
    protected RouterCommandsRepository rcRepo;
    @Autowired
    protected ConnService connSvc;
    @Autowired
    protected TopoService topoService;

    public void runEngine() throws Exception {
        topoService.updateTopo();

        List<InResv> resvs = this.readJson();
        int num = 0;
        int deleted = 0;
        int failed = 0;

        for (InResv resv : resvs) {
            // delete old
            Optional<Connection> maybeExists = connRepo.findByConnectionId(resv.getGri());
            if (maybeExists.isPresent()) {
                connRepo.delete(maybeExists.get());
                connRepo.flush();
                deleted++;
            }
            List<RouterCommands> existing = rcRepo.findByConnectionId(resv.getGri());
            if (existing.size() > 0) {
                rcRepo.delete(existing);
                rcRepo.flush();
            }

            Connection c = this.toConnection(resv);
            if (c != null) {
//            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(c);
//            log.debug(pretty);
                connRepo.save(c);
                List<RouterCommands> rc = this.toCommands(resv);
                rcRepo.save(rc);
                num++;

            } else {
                failed ++;
            }
        }
        log.info("deleted " + deleted + " previously migrated connections");
        log.info("migrated " + num + " reservations; "+failed+" failed ");

    }

    public List<InResv> readJson() {
        String filename = "0_6.json";
        File jsonFile = new File(filename);
        ObjectMapper mapper = new ObjectMapper();
        List<InResv> resvs = new ArrayList<>();
        try {
            resvs = Arrays.asList(mapper.readValue(jsonFile, InResv[].class));
            log.info("deserialized " + resvs.size() + " reservations");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resvs;

    }


    public Connection toConnection(InResv inResv) {
        Map<String, TopoUrn> urnMap = topoService.getTopoUrnMap();
        boolean conversionError = false;
        /*
        try {
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(urnMap);
            log.debug(pretty);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        */


        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.builder().category("oscars").contents("migrated").build());

        if (inResv.getMisc().getProduction()) {
            tags.add(Tag.builder().category("production").contents("true").build());
        } else {
            tags.add(Tag.builder().category("production").contents("false").build());
        }
        Schedule s = Schedule.builder()
                .connectionId(inResv.getGri())
                .phase(Phase.RESERVED)
                .refId(inResv.getGri() + "-SCHEDULE")
                .beginning(Instant.ofEpochSecond(inResv.getSchedule().getStart()))
                .ending(Instant.ofEpochSecond(inResv.getSchedule().getEnd()))
                .build();

        List<VlanFixture> fixtures = new ArrayList<>();
        List<VlanJunction> junctions = new ArrayList<>();
        List<VlanPipe> pipes = new ArrayList<>();
        Map<String, VlanJunction> jMap = new HashMap<>();

        for (String inJunction : inResv.getCmp().getJunctions()) {
            // log.info("adding junction for " + inJunction);
            if (!urnMap.containsKey(inJunction)) {
                log.error("could not find device urn for " + inJunction);
                conversionError = true;
                continue;
            }
            TopoUrn deviceUrn = urnMap.get(inJunction);
            if (!deviceUrn.getUrnType().equals(UrnType.DEVICE)) {
                log.error("wrong urn type for " + inJunction);
                conversionError = true;
                continue;
            }
            Set<CommandParam> jcps = new HashSet<>();

            Collections.sort(inResv.getPss().getRes().getVplsId());
            for (Integer vplsId : inResv.getPss().getRes().getVplsId()) {
                jcps.add(CommandParam.builder()
                        .connectionId(inResv.getGri())
                        .schedule(s)
                        .resource(vplsId)
                        .paramType(CommandParamType.VC_ID)
                        .urn(inJunction)
                        .build());
                if (deviceUrn.getDevice().getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                    jcps.add(CommandParam.builder()
                            .connectionId(inResv.getGri())
                            .schedule(s)
                            .resource(vplsId)
                            .paramType(CommandParamType.ALU_SVC_ID)
                            .urn(inJunction)
                            .build());
                }
            }
            for (InPssResResource ipr : inResv.getPss().getRes().getResources()) {
                if (ipr.getDevice().equals(inJunction)) {
                    if (ipr.getWhat().equals("loopback")) {
                        jcps.add(CommandParam.builder()
                                .connectionId(inResv.getGri())
                                .schedule(s)
                                .resource(ipr.getResource())
                                .paramType(CommandParamType.VPLS_LOOPBACK)
                                .urn(inJunction)
                                .intent(inJunction)
                                .build());
                    }
                    if (ipr.getWhat().equals("sdp")) {
                        String otherJunction = inJunction;
                        for (String j : inResv.getCmp().getJunctions()) {
                            if (!j.equals(inJunction)) {
                                otherJunction = j;
                            }
                        }
                        jcps.add(CommandParam.builder()
                                .connectionId(inResv.getGri())
                                .schedule(s)
                                .resource(ipr.getResource())
                                .paramType(CommandParamType.ALU_SDP_ID)
                                .urn(inJunction)
                                .intent(otherJunction)
                                .build());
                    }

                }
            }

            VlanJunction vj = VlanJunction.builder()
                    .commandParams(jcps)
                    .connectionId(inResv.getGri())
                    .deviceUrn(inJunction)
                    .refId(inJunction)
                    .schedule(s)
                    .build();
            junctions.add(vj);
            jMap.put(inJunction, vj);
        }

        for (InFixture inFixture : inResv.getCmp().getFixtures()) {

            if (!jMap.containsKey(inFixture.getJunction())) {
                log.error("no junction for " + inFixture.getJunction());
                conversionError = true;
                continue;
            }
            VlanJunction vj = jMap.get(inFixture.getJunction());
            String portUrnStr = inFixture.getJunction() + ":" + inFixture.getPort();
            if (!urnMap.containsKey(portUrnStr)) {
                log.error("edge port urn not found in topo " + portUrnStr);
                conversionError = true;
                continue;
            }
            EthFixtureType et = EthFixtureType.ALU_SAP;
            TopoUrn deviceUrn = urnMap.get(inFixture.getJunction());
            if (!deviceUrn.getDevice().getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                et = EthFixtureType.JUNOS_IFCE;
            }
            Set<CommandParam> fcps = new HashSet<>();
            if (deviceUrn.getDevice().getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                for (InPssResResource ipr : inResv.getPss().getRes().getResources()) {
                    if (ipr.getDevice().equals(inFixture.getJunction())) {
                        if (ipr.getWhat().equals("qos")) {
                            fcps.add(CommandParam.builder()
                                    .connectionId(inResv.getGri())
                                    .schedule(s)
                                    .resource(ipr.getResource())
                                    .paramType(CommandParamType.ALU_QOS_POLICY_ID)
                                    .urn(ipr.getDevice())
                                    .build());
                        }
                    }
                }

            }

            Vlan v = Vlan.builder()
                    .connectionId(inResv.getGri())
                    .schedule(s)
                    .urn(inFixture.getJunction())
                    .vlanId(inFixture.getVlan())
                    .build();

            VlanFixture vf = VlanFixture.builder()
                    .connectionId(inResv.getGri())
                    .commandParams(fcps)
                    .ingressBandwidth(inResv.getMbps())
                    .egressBandwidth(inResv.getMbps())
                    .ethFixtureType(et)
                    .junction(vj)
                    .portUrn(portUrnStr)
                    .schedule(s)
                    .vlan(v)
                    .build();
            fixtures.add(vf);
        }

        if (junctions.size() == 2) {
            List<EroHop> azERO = new ArrayList<>();

            for (InHop inHop : inResv.getCmp().getPipe()) {
                String hopUrn = null;
                String renamed = renamedPorts(inHop.getPort());
                if (renamed != null) {
                    inHop.setPort(renamed);
                }
                hopUrn = inHop.getDevice() + ":" + inHop.getPort();

                if (!urnMap.containsKey(hopUrn)) {
                    boolean found = false;
                    if (!urnMap.containsKey(inHop.getDevice())) {
                        log.error("hop urn device component not found in topo " + inHop.getDevice());
                        conversionError = true;
                        continue;
                    }
                    TopoUrn dev = urnMap.get(inHop.getDevice());
                    if (!dev.getUrnType().equals(UrnType.DEVICE)) {
                        log.error("hop urn device component is wrong type in topo " + inHop.getDevice());
                        conversionError = true;
                        continue;
                    }
                    if (!inHop.getPort().equals("")) {
                        for (Port p : dev.getDevice().getPorts()) {
                            if (inHop.getPort().equals(p.getIfce())) {
                                found = true;
                                hopUrn = p.getUrn();
                            } else if (inHop.getAddr().equals(p.getIpv4Address())) {
                                found = true;
                                hopUrn = p.getUrn();
                            }
                        }
                    } else {
                        if (urnMap.containsKey(inHop.getDevice())) {
                            hopUrn = dev.getUrn();
                            found = true;

                        } else {
                            log.error("hop urn not found in topo " + inHop.getDevice());
                            conversionError = true;
                        }
                    }

                    if (!found) {
                        log.error("hop port urn not found in topo " + hopUrn);
                        conversionError = true;
                        continue;
                    }
                }


                EroHop h = EroHop.builder()
                        .urn(hopUrn)
                        .build();
                azERO.add(h);
            }


            List<EroHop> zaERO = new ArrayList<>();
            for (EroHop h : azERO) {
                zaERO.add(EroHop.builder().urn(h.getUrn()).build());
            }
            Collections.reverse(zaERO);

            VlanPipe vp = VlanPipe.builder()
                    .azBandwidth(inResv.getMbps())
                    .zaBandwidth(inResv.getMbps())
                    .a(junctions.get(0))
                    .z(junctions.get(1))
                    .connectionId(inResv.getGri())
                    .azERO(azERO)
                    .zaERO(zaERO)
                    .schedule(s)
                    .build();

            pipes.add(vp);
        }

        Components cmp = Components.builder()
                .fixtures(fixtures)
                .junctions(junctions)
                .pipes(pipes)
                .build();
        Reserved resv = Reserved.builder()
                .connectionId(inResv.getGri())
                .cmp(cmp)
                .schedule(s)
                .build();


        Connection c = Connection.builder()
                .connectionId(inResv.getGri())
                .description(inResv.getMisc().getDescription())
                .mode(BuildMode.AUTOMATIC)
                .phase(Phase.RESERVED)
                .state(State.ACTIVE)
                .tags(tags)
                .username(inResv.getMisc().getUser())
                .held(null)
                .archived(null)
                .reserved(resv)
                .build();

        connSvc.archiveFromReserved(c);

        if (conversionError) {
            log.error("Error(s) converting " + inResv.getGri());
            return null;
        }
        return c;
    }

    public List<RouterCommands> toCommands(InResv inResv) {
        List<RouterCommands> commands = new ArrayList<>();
        for (InPssConfig inPssConfig : inResv.getPss().getConfig()) {
            CommandType ct = CommandType.DISMANTLE;
            if (inPssConfig.getPhase().equals("BUILD")) {
                ct = CommandType.BUILD;
            }
            commands.add(RouterCommands.builder()
                    .connectionId(inResv.getGri())
                    .deviceUrn(inPssConfig.getDevice())
                    .contents(inPssConfig.getConfig())
                    .type(ct)
                    .build());
        }
        return commands;

    }

    public String renamedPorts(String port) {
        if (port.equals("to_lond-cr5_sdn-a")) {
            return "to_lond-cr5_ip-c";
        } else if (port.equals("to_aofa-cr5_sdn-a")) {
            return "to_aofa-cr5_ip-c";
        } else {
            return null;
        }
    }
}
