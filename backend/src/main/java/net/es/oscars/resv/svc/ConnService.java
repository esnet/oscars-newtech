package net.es.oscars.resv.svc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.db.ArchivedRepository;
import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.db.HeldRepository;
import net.es.oscars.resv.db.ReservedRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@Data
public class ConnService {

    @Autowired
    private ConnectionRepository connRepo;

    @Autowired
    private HeldRepository heldRepo;

    @Autowired
    private ArchivedRepository archivedRepo;

    @Autowired
    private ReservedRepository reservedRepo;

    public Connection connectionFromBits(String connectionId, String username) {
        archivedRepo.findByConnectionId(connectionId);

        Connection c = Connection.builder()
                .connectionId(connectionId)
                .phase(Phase.HELD)
                .username(username)
                .build();
        if (archivedRepo.findByConnectionId(connectionId).isPresent()) {
            c.setArchived(archivedRepo.findByConnectionId(connectionId).get());
        }
        if (heldRepo.findByConnectionId(connectionId).isPresent()) {
            c.setHeld(heldRepo.findByConnectionId(connectionId).get());
        }
        if (reservedRepo.findByConnectionId(connectionId).isPresent()) {
            c.setReserved(reservedRepo.findByConnectionId(connectionId).get());
        }
        connRepo.save(c);
        return c;

    }

    public Phase commit(Connection c) {
        c.setPhase(Phase.RESERVED);

        this.reservedFromHeld(c);
        this.archiveFromReserved(c);

        Held h = c.getHeld();

        c.setHeld(null);
        connRepo.save(c);
        heldRepo.delete(h);


        return Phase.RESERVED;

    }

    public Phase uncommit(Connection c) {

        connRepo.delete(c);

        Held h = this.heldFromReserved(c);
        heldRepo.save(h);

        return Phase.HELD;

    }

    public void reservedFromHeld(Connection c) {

        Components cmp = c.getHeld().getCmp();
        Schedule resvSch = this.copySchedule(c.getHeld().getSchedule());
        resvSch.setPhase(Phase.RESERVED);


        Components resvCmp = this.copyComponents(cmp, resvSch);
        Reserved reserved = Reserved.builder()
                .cmp(resvCmp)
                .connectionId(c.getConnectionId())
                .schedule(resvSch)
                .build();
        c.setReserved(reserved);
    }

    public Held heldFromReserved(Connection c) {

        Components cmp = c.getReserved().getCmp();
        Schedule sch = this.copySchedule(c.getReserved().getSchedule());
        sch.setPhase(Phase.HELD);

        Instant exp = Instant.now().plus(15L, ChronoUnit.MINUTES);

        Components heldCmp = this.copyComponents(cmp, sch);
        return Held.builder()
                .cmp(heldCmp)
                .connectionId(c.getConnectionId())
                .schedule(sch)
                .expiration(exp)
                .build();
    }

    public void archiveFromReserved(Connection c) {
        Components cmp = c.getReserved().getCmp();
        Schedule sch = this.copySchedule(c.getReserved().getSchedule());
        sch.setPhase(Phase.ARCHIVED);

        Components archCmp = this.copyComponents(cmp, sch);
        Archived archived = Archived.builder()
                .cmp(archCmp)
                .connectionId(c.getConnectionId())
                .schedule(sch)
                .build();
        c.setArchived(archived);
    }

    private Schedule copySchedule(Schedule sch) {
        return Schedule.builder()
                .beginning(sch.getBeginning())
                .ending(sch.getEnding())
                .connectionId(sch.getConnectionId())
                .refId(sch.getRefId())
                .phase(sch.getPhase())
                .build();
    }

    private Components copyComponents(Components cmp, Schedule sch) {
        List<VlanJunction> junctions = new ArrayList<>();
        Map<String, VlanJunction> jmap = new HashMap<>();
        for (VlanJunction j : cmp.getJunctions()) {
            VlanJunction jc = VlanJunction.builder()
                    .commandParams(copyCommandParams(j.getCommandParams(), sch))
                    .deviceUrn(j.getDeviceUrn())
                    .vlan(copyVlan(j.getVlan(), sch))
                    .schedule(sch)
                    .refId(j.getRefId())
                    .connectionId(j.getConnectionId())
                    .deviceUrn(j.getDeviceUrn())
                    .build();
            jmap.put(j.getDeviceUrn(), jc);
            junctions.add(jc);
        }

        List<VlanFixture> fixtures = new ArrayList<>();
        for (VlanFixture f: cmp.getFixtures()) {
            VlanFixture fc = VlanFixture.builder()
                    .connectionId(f.getConnectionId())
                    .ingressBandwidth(f.getIngressBandwidth())
                    .egressBandwidth(f.getEgressBandwidth())
                    .schedule(sch)
                    .ethFixtureType(f.getEthFixtureType())
                    .junction(jmap.get(f.getJunction().getDeviceUrn()))
                    .portUrn(f.getPortUrn())
                    .vlan(copyVlan(f.getVlan(), sch))
                    .commandParams(copyCommandParams(f.getCommandParams(), sch))
                    .build();
            fixtures.add(fc);
        }
        List<VlanPipe> pipes = new ArrayList<>();
        for (VlanPipe p : cmp.getPipes()) {
            VlanPipe pc = VlanPipe.builder()
                    .a(jmap.get(p.getA().getDeviceUrn()))
                    .z(jmap.get(p.getZ().getDeviceUrn()))
                    .azBandwidth(p.getAzBandwidth())
                    .zaBandwidth(p.getZaBandwidth())
                    .commandParams(copyCommandParams(p.getCommandParams(), sch))
                    .connectionId(p.getConnectionId())
                    .schedule(sch)
                    .azERO(copyEro(p.getAzERO()))
                    .zaERO(copyEro(p.getZaERO()))
                    .build();
            pipes.add(pc);
        }


        return Components.builder()
                .fixtures(fixtures)
                .junctions(junctions)
                .pipes(pipes)
                .build();
    }
    private List<EroHop> copyEro(List<EroHop> ero) {
        List<EroHop> res = new ArrayList<>();
        for (EroHop h : ero) {
            EroHop hc = EroHop.builder()
                    .urn(h.getUrn())
                    .build();
            res.add(hc);
        }

        return res;
    }

    private Set<CommandParam> copyCommandParams(Set<CommandParam> cps, Schedule sch) {
        Set<CommandParam> res = new HashSet<>();
        for (CommandParam cp: cps) {
            res.add(CommandParam.builder()
                    .connectionId(cp.getConnectionId())
                    .paramType(cp.getParamType())
                    .schedule(sch)
                    .resource(cp.getResource())
                    .refId(cp.getRefId())
                    .urn(cp.getUrn())
                    .build());
        }
        return res;
    }

    private Vlan copyVlan(Vlan v, Schedule sch) {
        if (v == null) {
            return null;
        }
        return Vlan.builder()
                .connectionId(v.getConnectionId())
                .schedule(sch)
                .urn(v.getUrn())
                .vlanId(v.getVlanId())
                .build();

    }


}
