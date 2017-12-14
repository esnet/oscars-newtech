package net.es.oscars.pss.svc;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.resv.db.FixtureRepository;
import net.es.oscars.resv.db.JunctionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.ReservableCommandParam;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.enums.CommandParamType;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;


@Service
@Transactional
@Slf4j
public class PssResourceService {
    @Autowired
    private TopoService topoService;

    @Autowired
    private ResvService resvService;

    @Autowired
    private FixtureRepository fixRepo;

    @Autowired
    private JunctionRepository jnctRepo;

    public void reserve(Connection conn) throws PSSException {
        log.info("starting PSS resource reservation");


        Schedule sched = conn.getReserved().getSchedule();
        Interval interval = Interval.builder()
                .beginning(sched.getBeginning())
                .ending(sched.getEnding())
                .build();

        try {
            Map<String, Set<ReservableCommandParam>> availableParams = resvService.availableParams(interval);
            log.debug("available params:");

            this.reserveGlobals(conn , sched, availableParams);

            for (VlanJunction j : conn.getReserved().getCmp().getJunctions()) {
                this.reserveByJunction(j, conn, sched, availableParams);
            }

            log.debug("allocated PSS resources, connection is now:");
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(conn);
            log.debug(pretty);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info("finished reserving pss");

    }

    @Transactional
    public void reserveGlobals(Connection conn, Schedule sched,
                                Map<String, Set<ReservableCommandParam>> availableParams) throws PSSException {

        log.info("reserving globals..");

        List<TopoUrn> topoUrns = new ArrayList<>();
        for (VlanJunction j : conn.getReserved().getCmp().getJunctions()) {
            TopoUrn urn = topoService.getTopoUrnMap().get(j.getDeviceUrn());
            if (!urn.getUrnType().equals(UrnType.DEVICE)) {
                throw new PSSException("invalid URN type");
            }
            topoUrns.add(urn);
        }

        Map<String, Set<IntRange>> availVcIds = new HashMap<>();
//        Set<IntRange> allRanges = new HashSet<>();
        for (TopoUrn urn : topoUrns) {
            for (ReservableCommandParam rcp : availableParams.get(urn.getUrn())) {
                if (rcp.getType().equals(CommandParamType.VC_ID)) {
                    availVcIds.put(urn.getUrn(), rcp.getReservableRanges());
//                    allRanges.addAll(rcp.getReservableRanges());
                }
            }
        }

        Integer vcid = IntRange.leastInAll(availVcIds);
        if (vcid == null) {
            throw new PSSException("no vcid found!");
        }

        log.info("found vc id "+ vcid);

        for (VlanJunction j : conn.getReserved().getCmp().getJunctions()) {
            CommandParam vcCp = CommandParam.builder()
                    .connectionId(conn.getConnectionId())
                    .paramType(CommandParamType.VC_ID)
                    .resource(vcid)
                    .schedule(sched)
                    .urn(j.getDeviceUrn())
                    .build();
            j.getCommandParams().add(vcCp);

            // for ALUs we also need to reserve an SVC ID globally. Right now: === the VC id
            // TODO: consider VC & SVC id for backup
            TopoUrn devUrn = topoService.getTopoUrnMap().get(j.getDeviceUrn());
            if (devUrn.getDevice().getModel().equals(DeviceModel.ALCATEL_SR7750)) {
                CommandParam svcCp = CommandParam.builder()
                        .connectionId(conn.getConnectionId())
                        .paramType(CommandParamType.ALU_SVC_ID)
                        .resource(vcid)
                        .schedule(sched)
                        .urn(j.getDeviceUrn())
                        .build();
                j.getCommandParams().add(svcCp);
            }
            jnctRepo.save(j);
        }

    }

    @Transactional
    public void reserveByJunction(VlanJunction j, Connection conn, Schedule sched,
                                   Map<String, Set<ReservableCommandParam>> availableParams) throws PSSException {
        log.info("reserving PSS resources by junction : " + j.getDeviceUrn());
        TopoUrn urn = topoService.getTopoUrnMap().get(j.getDeviceUrn());
        if (!urn.getUrnType().equals(UrnType.DEVICE)) {
            throw new PSSException("invalid URN type");
        }
        Device d = urn.getDevice();
        // for ALUs we need one qos id per fixture; QoS ids are reserved on each device
        if (d.getModel().equals(DeviceModel.ALCATEL_SR7750)) {

            // first find available QOS ids
            Set<IntRange> availQosIds = new HashSet<>();
            for (ReservableCommandParam rcp: availableParams.get(d.getUrn())) {
                if (rcp.getType().equals(CommandParamType.ALU_QOS_POLICY_ID)) {
                    availQosIds = rcp.getReservableRanges();
                }
            }

            for (VlanFixture f: conn.getReserved().getCmp().getFixtures()) {
                if (f.getJunction().getDeviceUrn().equals(urn.getUrn())) {
                    // this fixture does belong to this junction
                    if (f.getCommandParams() == null) {
                        f.setCommandParams(new HashSet<>());
                    }

                    if (availQosIds.size() == 0) {
                        throw new PSSException("No ALU QOS ids available");
                    }

                    Integer picked = IntRange.minFloor(availQosIds);
                    CommandParam qosCp = CommandParam.builder()
                            .connectionId(conn.getConnectionId())
                            .paramType(CommandParamType.ALU_QOS_POLICY_ID)
                            .resource(picked)
                            .schedule(sched)
                            .urn(d.getUrn())
                            .build();
                    f.getCommandParams().add(qosCp);

                    availQosIds = IntRange.subtractFromSet(availQosIds, picked);
                    jnctRepo.save(f.getJunction());
                    fixRepo.save(f);
                }
             }
        }

    }

}