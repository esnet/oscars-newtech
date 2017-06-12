package net.es.oscars.whatif.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.bwavail.svc.BandwidthAvailabilityGenerationService;
import net.es.oscars.bwavail.svc.BandwidthAvailabilityService;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.pce.exc.PCEException;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.svc.ConnectionGenerationService;
import net.es.oscars.resv.svc.DateService;
import net.es.oscars.webui.cont.ReservationController;
import net.es.oscars.whatif.dto.WhatifSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class SuggestionGenerator {


    private ConnectionGenerationService connectionGenerationService;

    private ReservationController resvController;

    private DateService dateService;

    private BandwidthAvailabilityGenerationService bwAvailGenService;

    private BandwidthAvailabilityService bwAvailService;

    @Autowired
    public SuggestionGenerator(ConnectionGenerationService connectionGenerationService, ReservationController resvController, DateService dateService,
                               BandwidthAvailabilityGenerationService bwAvailGenService, BandwidthAvailabilityService bwAvailService) {
        this.connectionGenerationService = connectionGenerationService;
        this.resvController = resvController;
        this.dateService = dateService;
        this.bwAvailGenService = bwAvailGenService;
        this.bwAvailService = bwAvailService;
    }


    /**
     * Generate a list of viable connections given a Start Date, End Date, and requested transfer Volume.
     * Provide the minimum needed bandwidth, and the maximum possible.
     *
     * @param spec - Submitted request specification.
     * @return A list of connections that could satisfy the demand.
     */
    public List<Connection> generateWithStartEndVolume(WhatifSpecification spec) {
        List<Connection> connections = new ArrayList<>();
        Date start = dateService.parseDate(spec.getStartDate());
        Date end = dateService.parseDate(spec.getEndDate());
        Integer volume = spec.getVolume();

        // Determine these values - Bandwidth from src -> dst (a -> z), and from dst -> src (z -> a)
        // Values may be the same, or different
        Integer secondsBetween = (int) ((end.getTime() - start.getTime()) / 1000);
        Integer minimumRequiredBandwidth = (int) Math.ceil(1.0 * volume / secondsBetween);

        // Get the bandwidth availability along a path from srcDevice to dstDevice
        BandwidthAvailabilityRequest bwAvailRequest = createBwAvailRequest(spec.getSrcDevice(), spec.getSrcPorts(),
                spec.getDstDevice(), spec.getDstPorts(), minimumRequiredBandwidth, minimumRequiredBandwidth, start, end);
        BandwidthAvailabilityResponse bwResponse = bwAvailService.getBandwidthAvailabilityMap(bwAvailRequest);

        Map<Instant, Integer> bwMap = bwResponse.getBwAvailabilityMap().get("Az1");
        List<Integer> possibleBandwidths = new ArrayList<>();
        Integer minimumBandwidth = null;

        // Go through all critical points in the bandwidth availability map
        // Confirm that the available bandwidth does not drop below our minimum required
        for (Instant instant : bwMap.keySet()) {
            Integer bandwidth = bwMap.get(instant);
            if (minimumBandwidth == null || bandwidth < minimumBandwidth) {
                minimumBandwidth = bandwidth;
            }
            if (bandwidth < minimumRequiredBandwidth) {
                return connections;  // OSCARS will generate results
            }
        }

        // If the program arrived at this point in the code, we have two possible values for bandwidth
        // The first is the minimum required
        if (minimumRequiredBandwidth > 0) {
            possibleBandwidths.add(minimumRequiredBandwidth);
        }
        // The second, if it is not the same as the previous bandwidth value,
        // is the minimum bandwidth found on the bandwidth availability map
        if (minimumBandwidth != minimumRequiredBandwidth) {
            possibleBandwidths.add(minimumBandwidth);
        }

        // Now we can create a connection for all of the possible bandwidths
        for (int i = 0; i < possibleBandwidths.size(); i++) {

            Integer band = possibleBandwidths.get(i);

            // For now, both directions on the path will have equal bandwidth
            // It is possible we will want to change this in the future
            Integer azMbps = band;
            Integer zaMbps = band;

            // Each connection must have a unique id
            String connectionId = "startEndVolume" + i;

            // Create an initial connection from parameters
            Connection conn = createInitialConnection(spec.getSrcDevice(), spec.getSrcPorts(), spec.getDstDevice(),
                    spec.getDstPorts(), azMbps, zaMbps, connectionId, start, end);

            // Run a precheck
            Connection result = null;
            try {
                result = resvController.preCheck(conn);
                if (result != null) {
                    // Determine if result is successful. If so, consider storing it as an option
                    if (result.getReserved().getVlanFlow().getAllPaths().size() > 0) {
                        // Success!  Now we can add this connection to our list
                        connections.add(result);
                    }
                }
            } catch (PCEException | PSSException e) {
                log.info("Connection precheck caused an exception.");
            }
        }


        return connections;
    }

    /**
     * Generate a list of viable connections given a Start Date, and End Date.
     * Get the maximum bandwidth possible.
     *
     * @param spec - Submitted request specification.
     * @return A list of connections that could satisfy the demand.
     */
    public List<Connection> generateWithStartEnd(WhatifSpecification spec) {
        List<Connection> connections = new ArrayList<>();
        Date start = dateService.parseDate(spec.getStartDate());
        Date end = dateService.parseDate(spec.getEndDate());

        BandwidthAvailabilityRequest bwAvailRequest = createBwAvailRequest(spec.getSrcDevice(), spec.getSrcPorts(),
                spec.getDstDevice(), spec.getDstPorts(), 0, 0, start, end);
        BandwidthAvailabilityResponse bwResponse = bwAvailService.getBandwidthAvailabilityMap(bwAvailRequest);

        Map<Instant, Integer> bwMap = bwResponse.getBwAvailabilityMap().get("Az1");
        Integer minimumBandwidth = null;

        // Go through all critical points in the bandwidth availability map
        // Calculate the minimum bandwidth available across the map
        for (Instant instant : bwMap.keySet()) {
            Integer bandwidth = bwMap.get(instant);
            if (minimumBandwidth == null || bandwidth < minimumBandwidth) {
                minimumBandwidth = bandwidth;
                if (minimumBandwidth == 0) {
                    return connections;
                }
            }
        }

        // For now, both directions on the path will have equal bandwidth
        // It is possible we will want to change this in the future
        Integer azMbps = minimumBandwidth;
        Integer zaMbps = minimumBandwidth;

        // Each connection must have a unique id
        String connectionId = "startEnd0";

        // Create an initial connection from parameters
        Connection conn = createInitialConnection(spec.getSrcDevice(), spec.getSrcPorts(), spec.getDstDevice(),
                spec.getDstPorts(), azMbps, zaMbps, connectionId, start, end);

        // Run a precheck
        Connection result = null;
        try {
            result = resvController.preCheck(conn);
            if (result != null) {
                // Determine if result is successful. If so, consider storing it as an option
                if (result.getReserved().getVlanFlow().getAllPaths().size() > 0) {
                    // Success!  Now we can add this connection to our list
                    connections.add(result);
                }
            }
        } catch (PCEException | PSSException e) {
            log.info("Connection precheck caused an exception.");
        }

        return connections;
    }

    /**
     * Generate a list of viable connections given a Start Date, and requested transfer volume.
     * Complete as early as possible.
     *
     * @param spec - Submitted request specification.
     * @return A list of connections that could satisfy the demand.
     */
    public List<Connection> generateWithStartVolume(WhatifSpecification spec) {
        return new ArrayList<>();
    }

    /**
     * Generate a list of viable connections given an End Date, and requested transfer volume.
     * Complete transfer by deadline.
     *
     * @param spec - Submitted request specification.
     * @return A list of connections that could satisfy the demand.
     */
    public List<Connection> generateWithEndVolume(WhatifSpecification spec) {
        return new ArrayList<>();
    }

    /**
     * Generate a list of viable connections given a Start Date, requested transfer volume, and desired bandwidth.
     * Complete as early as possible.
     *
     * @param spec - Submitted request specification.
     * @return A list of connections that could satisfy the demand.
     */
    public List<Connection> generateWithStartVolumeBandwidth(WhatifSpecification spec) {
        return new ArrayList<>();
    }

    /**
     * Generate a list of viable connections given an End Date, requested transfer volume, and desired bandwidth.
     * Complete transfer by deadline.
     *
     * @param spec - Submitted request specification.
     * @return A list of connections that could satisfy the demand.
     */
    public List<Connection> generateWithEndVolumeBandwidth(WhatifSpecification spec) {
        return new ArrayList<>();
    }

    /**
     * Generate a list of viable connections given a requested transfer volume, and a transfer duration.
     * Find contiguous time periods to perform transfer.
     *
     * @param spec - Submitted request specification.
     * @return A list of connections that could satisfy the demand.
     */
    public List<Connection> generateWithVolumeDuration(WhatifSpecification spec) {
        return new ArrayList<>();
    }

    /**
     * Generate a list of viable connections given just a requested transfer volume.
     * Complete transfer as early as possible.
     *
     * @param spec - Submitted request specification.
     * @return A list of connections that could satisfy the demand.
     */
    public List<Connection> generateWithVolume(WhatifSpecification spec) {
        return new ArrayList<>();
    }

    public Connection createInitialConnection(String srcDevice, Set<String> srcPorts, String dstDevice, Set<String> dstPorts,
                                              Integer azMbps, Integer zaMbps, String connectionId, Date startDate,
                                              Date endDate) {
        net.es.oscars.dto.spec.Specification spec = connectionGenerationService.generateSpecification(srcDevice, srcPorts, dstDevice, dstPorts,
                "any", "any", azMbps, zaMbps, new ArrayList<>(), new ArrayList<>(), new HashSet<>(),
                "PALINDROME", "NONE", 1, 1, 1, 1, connectionId,
                startDate, endDate);
        return connectionGenerationService.buildConnection(spec);
    }

    public BandwidthAvailabilityRequest createBwAvailRequest(String srcDevice, Set<String> srcPorts, String dstDevice,
                                                             Set<String> dstPorts, Integer minAzMbps, Integer minZaMbps,
                                                             Date startDate, Date endDate) {
        return bwAvailGenService.generateBandwidthAvailabilityRequest(srcDevice, srcPorts, dstDevice, dstPorts,
                new ArrayList<>(), new ArrayList<>(), minAzMbps, minZaMbps, 1, true, startDate, endDate);
    }


}
