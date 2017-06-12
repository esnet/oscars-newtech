package net.es.oscars.resv.rest;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.BasicCircuitSpecification;
import net.es.oscars.dto.resv.CircuitSpecification;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ReservationDetails;
import net.es.oscars.resv.svc.ConnectionGenerationService;
import net.es.oscars.resv.svc.ConnectionSimplificationService;
import net.es.oscars.resv.svc.ResvService;
import net.es.oscars.st.resv.ResvState;
import net.es.oscars.webui.cont.ReservationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * Provides a simplified endpoint for submitting and committing circuit reservations.
 */
@Slf4j
@Controller
public class SimpleResvController {
    @Autowired
    public SimpleResvController(ConnectionSimplificationService connectionSimplificationService,
                                ConnectionGenerationService connectionGenerationService,
                                ReservationController resvController,
                                ResvService resvService) {
        this.resvController = resvController;
        this.resvService = resvService;
        this.connectionGenerationService = connectionGenerationService;
        this.connectionSimplificationService = connectionSimplificationService;

    }

    private ConnectionSimplificationService connectionSimplificationService;

    private ConnectionGenerationService connectionGenerationService;

    private ReservationController resvController;
    private ResvService resvService;

    @RequestMapping(value = "/resv_simple/get/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public ReservationDetails getDetails(@PathVariable("connectionId") String connectionId) {
        log.info("Retrieving reservation information...");

        Connection conn = resvController.resv_get_details(connectionId);
        return simplifyResponse(conn, connectionId);
    }

    @RequestMapping(value = "/resv_simple/connection/add", method = RequestMethod.POST)
    @ResponseBody
    public ReservationDetails submitSpec(@RequestBody CircuitSpecification spec) {

        log.info("Received Specification from Client. Submitting (must commit later).");
        log.info("Specification Params: " + spec);

        Connection conn = connectionGenerationService.generateConnection(spec);
        // Submit, do not commit
        try {
            conn = resvController.holdConnection(conn);
        } catch (Exception e) {
            conn = null;
        }
        return simplifyResponse(conn, spec.getConnectionId());
    }

    @RequestMapping(value = "/resv_simple/commit/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public ReservationDetails commit(@PathVariable("connectionId") String connectionId) {
        log.info("Committing Reservation " + connectionId);

        // Commit
        Connection conn = resvController.resv_get_details(connectionId);
        try {
            resvController.commitConnection(connectionId);
        } catch (Exception e) {
            conn = null;
        }

        return simplifyResponse(conn, connectionId);
    }

    @RequestMapping(value = "/resv_simple/connection/add_commit", method = RequestMethod.POST)
    @ResponseBody
    public ReservationDetails submitCommitSpec(@RequestBody CircuitSpecification spec) {

        log.info("Received Specification from Client. Submitting and committing (on success).");
        log.info("Specification Params: " + spec);

        Connection conn = connectionGenerationService.generateConnection(spec);

        // Submit
        try {
            conn = resvController.holdConnection(conn);
        } catch (Exception e) {
            conn = null;
        }

        // Commit if submit was successful
        if (conn != null && conn.getStates().getResv().equals(ResvState.HELD)) {
            resvController.commitConnection(conn.getConnectionId());
        }
        return simplifyResponse(conn, spec.getConnectionId());
    }

    @RequestMapping(value = "/resv_simple/connection/add_commit_basic", method = RequestMethod.POST)
    @ResponseBody
    public ReservationDetails submitCommitBasicSpec(@RequestBody BasicCircuitSpecification spec) {

        log.info("Received Basic Specification from Client. Submitting and committing (on success).");
        log.info("Specification Params: " + spec);

        Connection conn = connectionGenerationService.generateConnection(spec);
        // Submit
        try {
            conn = resvController.holdConnection(conn);
        } catch (Exception e) {
            conn = null;
        }
        // Commit if submit was successful
        if (conn != null && conn.getStates().getResv().equals(ResvState.HELD)) {
            resvController.commitConnection(conn.getConnectionId());
        }
        return simplifyResponse(conn, spec.getConnectionId());
    }

    @RequestMapping(value = "/resv_simple/abort/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public ReservationDetails abort(@PathVariable("connectionId") String connectionId) {
        log.info("Aborting Reservation " + connectionId);

        // Abort
        Connection conn = resvController.convertConnToDto(resvService.findByConnectionId(connectionId).orElseThrow(NoSuchElementException::new));
        return simplifyResponse(conn, connectionId);

    }

    private ReservationDetails simplifyResponse(Connection conn, String connectionId) {
        ReservationDetails resDetails = connectionSimplificationService.simplifyConnection(conn);
        resDetails.setConnectionId(connectionId);
        log.info("Response Details: " + resDetails.toString());
        return resDetails;
    }


}
