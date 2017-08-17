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
    private FixtureRepository fixtureRepo;

    @Autowired
    private JunctionRepository jnctRepo;

    @Autowired
    private PssProperties props;


    public void reserve(Connection conn) throws PSSException {
        log.info("starting PSS resource reservation");

        this.reserveGlobals(conn.getReserved().getCmp(), conn.getReserved().getSchedule());

        for (VlanJunction j : conn.getReserved().getCmp().getJunctions()) {

            this.reserveByJunction(j, conn.getReserved().getCmp(), conn.getReserved().getSchedule());
        }

        try {
            log.debug("allocated PSS resources, connection is now:");
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(conn);
            log.debug(pretty);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void reserveGlobals(Components cmp, Schedule sched) throws PSSException {
        Interval interval = Interval.builder()
                .beginning(sched.getBeginning())
                .ending(sched.getEnding())
                .build();

        Map<String, Set<ReservableCommandParam>> availableParams = resvService.availableParams(interval);


        List<TopoUrn> topoUrns = new ArrayList<>();
        for (VlanJunction j : cmp.getJunctions()) {
            TopoUrn urn = topoService.getTopoUrnMap().get(j.getDeviceUrn());
            if (!urn.getUrnType().equals(UrnType.DEVICE)) {
                throw new PSSException("invalid URN type");
            }
            topoUrns.add(urn);
        }

        Map<String, Set<IntRange>> availVcIds = new HashMap<>();
        Set<IntRange> allRanges = new HashSet<>();
        for (TopoUrn urn : topoUrns) {
            for (ReservableCommandParam rcp : availableParams.get(urn)) {
                if (rcp.getType().equals(CommandParamType.VC_ID)) {
                    availVcIds.put(urn.getUrn(), rcp.getReservableRanges());
                    allRanges.addAll(rcp.getReservableRanges());
                }
            }
        }
        Integer vcid = IntRange.leastInAll(availVcIds);
        if (vcid == null) {
            throw new PSSException("no vcid found!");
        }


        for (VlanJunction j : cmp.getJunctions()) {
            CommandParam vcCp = CommandParam.builder()
                    .connectionId(j.getConnectionId())
                    .paramType(CommandParamType.VC_ID)
                    .resource(vcid)
                    .schedule(sched)
                    .urn(j.getDeviceUrn())
                    .build();

            // same VC id as SVC id for ALUs
            CommandParam svcCp = CommandParam.builder()
                    .connectionId(j.getConnectionId())
                    .paramType(CommandParamType.ALU_SVC_ID)
                    .resource(vcid)
                    .schedule(sched)
                    .urn(j.getDeviceUrn())
                    .build();

            j.getCommandParams().add(vcCp);
            j.getCommandParams().add(svcCp);
            jnctRepo.save(j);
        }

    }


    private void reserveByJunction(VlanJunction j, Components cmp, Schedule sched) throws PSSException {
        log.info("reserving PSS resources by junction : " + j.getDeviceUrn());
        TopoUrn urn = topoService.getTopoUrnMap().get(j.getDeviceUrn());
        if (!urn.getUrnType().equals(UrnType.DEVICE)) {
            throw new PSSException("invalid URN type");
        }
        Device d = urn.getDevice();
    }

}