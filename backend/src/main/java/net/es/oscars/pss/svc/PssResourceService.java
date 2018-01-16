package net.es.oscars.pss.svc;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.resv.db.FixtureRepository;
import net.es.oscars.resv.db.JunctionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.ReservableCommandParam;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.enums.CommandParamType;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.web.beans.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        log.info("reserving globals & per-device. deciding VC id:");
        Interval interval = Interval.builder()
                .beginning(sched.getBeginning())
                .ending(sched.getEnding())
                .build();


        /*
        VC ids are reserved like so:
        - each device has a set of VC ids reservable and some of these are reserved i.e.
          device A: avail: {2, 3, 4, 5, 6, 7, 8, 9, 10 } res: { 5 }
          device B: avail: {3, 4, 5, 6, 7, 8, 9, 10 } res: { 3, 6, 8 }
        - for each device, the available set is reservable minus reserved i.e.
          device A: { 2, 3, 4, 6, 7, 8, 9, 10 }
          device B: { 4, 7, 9, 10 }
         - from all those sets, pick the lowest id that exists in every one of them (so, 4)

         - then add it to the reserved set for each device
          device A: avail: {2, 3, 4, 5, 6, 7, 8, 9, 10 } res: { 4, 5 }
          device B: avail: {3, 4, 5, 6, 7, 8, 9, 10 } res: { 3, 4, 6, 8 }
         */

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

        /*
        loopbacks are reserved like so:

            in ResvService:
        0.  There is a global set of reservable loopback addresses, as per PssProperties.loopbackRange
        0.1 We remove from that set any addresses already reserved and we then have a reservable set.

            here:
        1. for each junction in a connection
        1.1 if it doesn't connect to pipes, no loopback needed
        1.2 otherwise, reserve a loopback from the reservable set starting from the lowest available

        - the IP address is handled as an integer, it will be decoded by the PSS params handler

         */


        Set<Integer> availLoopbacks = resvService.availableLoopbacks(interval);

        for (VlanJunction j : conn.getReserved().getCmp().getJunctions()) {

            for (VlanPipe p: conn.getReserved().getCmp().getPipes()) {
                if (p.getA().getDeviceUrn().equals(j.getDeviceUrn()) || p.getZ().getDeviceUrn().equals(j.getDeviceUrn())) {
                    log.info("loopback needed for device "+j.getDeviceUrn());


                    Integer loopback = Integer.MAX_VALUE;
                    Boolean found = false;
                    for (Integer i : availLoopbacks) {
                        if (i < loopback) {
                            loopback = i;
                            found = true;
                        }
                    }
                    if (!found) {
                        throw new PSSException("could not find loopback for "+j.getDeviceUrn()+" pool size: "+availLoopbacks.size());
                    }

                    availLoopbacks.remove(loopback);

                    CommandParam loopbackCp = CommandParam.builder()
                            .connectionId(conn.getConnectionId())
                            .paramType(CommandParamType.VPLS_LOOPBACK)
                            .resource(loopback)
                            .intent(j.getDeviceUrn())
                            .schedule(sched)
                            .urn(j.getDeviceUrn())
                            .build();

                    j.getCommandParams().add(loopbackCp);
                }
            }



            log.info("reserving VC id for device "+j.getDeviceUrn());


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
        Components cmp = conn.getReserved().getCmp();
        // for ALUs we need one qos id per fixture; QoS ids are reserved on each device
        if (d.getModel().equals(DeviceModel.ALCATEL_SR7750)) {

            // first find available QOS ids
            Set<IntRange> availQosIds = new HashSet<>();
            for (ReservableCommandParam rcp: availableParams.get(d.getUrn())) {
                if (rcp.getType().equals(CommandParamType.ALU_QOS_POLICY_ID)) {
                    availQosIds = rcp.getReservableRanges();
                }
            }

            for (VlanFixture f: cmp.getFixtures()) {
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
                    jnctRepo.save(j);
                    fixRepo.save(f);
                }
             }

            // also, reserve one SDP id per pipe
            // TODO: possibly more then one if protect paths


            Set<IntRange> availSdpIds = new HashSet<>();
            for (ReservableCommandParam rcp: availableParams.get(d.getUrn())) {
                if (rcp.getType().equals(CommandParamType.ALU_SDP_ID)) {
                    availSdpIds = rcp.getReservableRanges();
                }
            }

            for (VlanPipe p : cmp.getPipes()) {
                boolean junctionInPipe = false;
                String intent = null;

                // when looking at all pipes, if this junction is either an A or a Z, handle it
                if (p.getA().getDeviceUrn().equals(j.getDeviceUrn())) {
                    junctionInPipe = true;
                    intent = p.getZ().getDeviceUrn();
                } else if (p.getZ().getDeviceUrn().equals(j.getDeviceUrn())) {
                    junctionInPipe = true;
                    intent = p.getA().getDeviceUrn();
                }
                if (junctionInPipe) {
                    Integer picked = IntRange.minFloor(availSdpIds);

                    CommandParam sdpIdCp = CommandParam.builder()
                            .connectionId(conn.getConnectionId())
                            .paramType(CommandParamType.ALU_SDP_ID)
                            .intent(intent)
                            .resource(picked)
                            .schedule(sched)
                            .urn(d.getUrn())
                            .build();

                    j.getCommandParams().add(sdpIdCp);

                    availSdpIds = IntRange.subtractFromSet(availSdpIds, picked);
                    jnctRepo.save(j);
                }
            }

        }


    }

}