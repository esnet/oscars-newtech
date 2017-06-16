package net.es.oscars.resv;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.AbstractCoreTest;
import net.es.oscars.dto.spec.PalindromicType;
import net.es.oscars.dto.spec.SurvivabilityType;
import net.es.oscars.helpers.RequestedEntityBuilder;
import net.es.oscars.pce.exc.DuplicateConnectionIdException;
import net.es.oscars.pce.exc.InvalidUrnException;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.ent.ConnectionE;
import net.es.oscars.resv.ent.RequestedBlueprintE;
import net.es.oscars.resv.ent.RequestedVlanPipeE;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.st.resv.ResvState;
import net.es.oscars.topo.pop.TopoFileImporter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bmah on 6/13/17.
 */
@Slf4j
@Transactional
public class CreateResvTest extends AbstractCoreTest {

    @Autowired
    private TopoFileImporter topoFileImporter;

    @Autowired
    private RequestedEntityBuilder testBuilder;

    @Autowired
    private ResvService resvService;

    /**
     * Load the ESnet topology before starting any tests
     * @throws IOException
     */
    @Before
    public void startup() throws IOException {
        topoFileImporter.importFromFile(true, "config/topo/esnet-devices.json", "config/topo/esnet-adjcies.json");
    }

    /**
     * Create a connection request from lbl-mr2:ge-9/0/1 to snla-rt3:ge-1/0/3
     * @param cid Connection ID
     * @return
     */
    private ConnectionE makeConnection1(String cid) {
        // Set up a request for a connection
        RequestedBlueprintE requestedBlueprint; // overall connection topology
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>(); // the pipes involved in the connection
        ConnectionE conn; // the connection we're trying to build

        // Reservation/connection should start 15 minutes from now and run until 1 hour from now
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.HOURS).getEpochSecond());

        // Set up the topology.  There will be one pipe (it's a point-to-point circuit)
        // lbl-mr2:ge-9/0/1 to snla-rt3:ge-1/0/3
        String srcDevice = "lbl-mr2";
        String dstDevice = "snla-rt3";
        List<String> srcPorts = Stream.of("lbl-mr2:ge-9/0/1").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("snla-rt3:ge-1/0/3").collect(Collectors.toList());

        // 10Mbps between any VLANs, palindromic routing, no survivability configured
        //   String vlan = "any";
        //   PalindromicType p = PalindromicType.PALINDROME;
        //   SurvivabilityType s = SurvivabilityType.SURVIVABILITY_NONE;

        // Create the pipe and add it to the request
        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice,
                10, 10, PalindromicType.PALINDROME, SurvivabilityType.SURVIVABILITY_NONE,
                "any", 1, 1);
        reqPipes.add(pipeAZ);

        // Make the blueprint (topology) for the connection request
        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1, cid);

        // Create connection request with topology, schedule, etc.
        conn = testBuilder.buildConnection(requestedBlueprint, testBuilder.buildSchedule(startDate, endDate),
                cid, "my new connection");

        return conn;
    }

    /**
     * Create a bogus connection request with fake endpoints
     * Based on makeConnection1.
     * @param cid Connection ID
     * @return
     */
    private ConnectionE makeConnection2(String cid) {
        // Set up a request for a connection
        RequestedBlueprintE requestedBlueprint; // overall connection topology
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>(); // the pipes involved in the connection
        ConnectionE conn; // the connection we're trying to build

        // Reservation/connection should start 15 minutes from now and run until 1 hour from now
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.HOURS).getEpochSecond());

        // Set up the topology.  There will be one pipe (it's a point-to-point circuit)
        // lbl-mr2:ge-9/0/1 to snla-rt3:ge-1/0/3
        String srcDevice = "green-cr5";
        String dstDevice = "purple-rt1";
        List<String> srcPorts = Stream.of("green-cr5:ge-9/0/1").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("purple-rt1:ge-1/0/3").collect(Collectors.toList());

        // 10Mbps between any VLANs, palindromic routing, no survivability configured
        //   String vlan = "any";
        //   PalindromicType p = PalindromicType.PALINDROME;
        //   SurvivabilityType s = SurvivabilityType.SURVIVABILITY_NONE;

        // Create the pipe and add it to the request
        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice,
                10, 10, PalindromicType.PALINDROME, SurvivabilityType.SURVIVABILITY_NONE,
                "any", 1, 1);
        reqPipes.add(pipeAZ);

        // Make the blueprint (topology) for the connection request
        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1, cid);

        // Create connection request with topology, schedule, etc.
        conn = testBuilder.buildConnection(requestedBlueprint, testBuilder.buildSchedule(startDate, endDate),
                cid, "my new connection");

        return conn;
    }

    /**
     * Create a connection request with ludicrous bitrate
     * This connection request should fail
     * @param cid Connection ID
     * @return
     */
    private ConnectionE makeConnection3(String cid) {
        // Set up a request for a connection
        RequestedBlueprintE requestedBlueprint; // overall connection topology
        Set<RequestedVlanPipeE> reqPipes = new HashSet<>(); // the pipes involved in the connection
        ConnectionE conn; // the connection we're trying to build

        // Reservation/connection should start 15 minutes from now and run until 1 hour from now
        Date startDate = new Date(Instant.now().plus(15L, ChronoUnit.MINUTES).getEpochSecond());
        Date endDate = new Date(Instant.now().plus(1L, ChronoUnit.HOURS).getEpochSecond());

        // Set up the topology.  There will be one pipe (it's a point-to-point circuit)
        // lbl-mr2:ge-9/0/1 to snla-rt3:ge-1/0/3
        String srcDevice = "lbl-mr2";
        String dstDevice = "snla-rt3";
        List<String> srcPorts = Stream.of("lbl-mr2:ge-9/0/1").collect(Collectors.toList());
        List<String> dstPorts = Stream.of("snla-rt3:ge-1/0/3").collect(Collectors.toList());

        // 10Mbps between any VLANs, palindromic routing, no survivability configured
        //   String vlan = "any";
        //   PalindromicType p = PalindromicType.PALINDROME;
        //   SurvivabilityType s = SurvivabilityType.SURVIVABILITY_NONE;

        // Create the pipe and add it to the request
        RequestedVlanPipeE pipeAZ = testBuilder.buildRequestedPipe(srcPorts, srcDevice, dstPorts, dstDevice,
                1000000, 1000000, PalindromicType.PALINDROME, SurvivabilityType.SURVIVABILITY_NONE,
                "any", 1, 1);
        reqPipes.add(pipeAZ);

        // Make the blueprint (topology) for the connection request
        requestedBlueprint = testBuilder.buildRequest(reqPipes, 1, 1, cid);

        // Create connection request with topology, schedule, etc.
        conn = testBuilder.buildConnection(requestedBlueprint, testBuilder.buildSchedule(startDate, endDate),
                cid, "my new connection");

        return conn;
    }

    @Test
    public void testMakeReservations() throws PSSException, PCEException {
        boolean ok;

        ConnectionE conn1 = makeConnection1("fred");

        // See how many reservations there are before we try to make one...
        int r0 = resvService.findAll().size();

        // Reservation attempt
        log.info("Attempting to create connection with " + r0);
        resvService.hold(conn1);

        // Check the number of reservations again.
        int r1 = resvService.findAll().size();
        log.info("Created reservation, now with " + r1);

        // Make sure there is now one more reservation than when we started.
        assert(r1 == r0 + 1);

        // Make sure we can find the reservation.
        Optional<ConnectionE> c = resvService.findByConnectionId(conn1.getConnectionId());
        assert(c.isPresent() && c.get().getConnectionId().equals(conn1.getConnectionId()));

        // Try to do a duplicate reservation.  This should fail.
        // So only continue if we get a particular exception.
        log.info("Attempting to create connection with " + r1);
        ok = false;
        try {
            resvService.hold(conn1);
        }
        catch (DuplicateConnectionIdException e) {
            log.info("Duplicate connection ID (this is expected)");
            ok = true;
        }
        assert(ok == true);

        // Make up another connection request but this time with invalid endpoints
        ConnectionE conn2 = makeConnection2("bogus");

        // Try to create this bogus connection.  This should fail.
        // So only continue if we get a particular exception.
        log.info("Attempting to create bogus connection with " + r1);
        ok = false;
        try {
            resvService.hold(conn2);
        }
        catch (InvalidUrnException e) {
            log.info("Invalid endpoint URN (this is expected)");
            ok = true;
        }
        assert(ok == true);

        // XXX Make another connection request

        // Do some weird outlandish bandwidth request
        // Make up another connection request but this time with invalid endpoints
        ConnectionE conn3 = makeConnection3("ludicrous");

        // Try to create this bogus connection.  This should fail.
        // So only continue if we get a particular exception.
        log.info("Attempting to create ludicrous connection with " + r1);
        resvService.hold(conn3);
/*
        ok = false;
        try {
            resvService.hold(conn3);
        }
        catch (InvalidUrnException e) {
            log.info("Invalid endpoint URN (this is expected)");
            ok = true;
        }
        assert(ok == true);
*/

        // Check the number of reservations again.
        int r2 = resvService.findAll().size();
        log.info("Created (?) reservation, now with " + r2);

        // Make sure there is now one more reservation than when we started.
        assert(r2 == r1 + 1);
        // But that reservation should be in ABORTING state.
        Optional<ConnectionE> c3 = resvService.findByConnectionId(conn3.getConnectionId());
        assert(c3.isPresent() && c3.get().getStates().getResv() == ResvState.ABORTING);
        log.info("Reservation is " + c3.get().getStates().getResv());

    }

    public void testGetResvs() {
        List<ConnectionE> r = resvService.findAll();
        log.info("Reservations: " + r.size());
    }
}
