package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.topo.enums.VertexType;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.resv.dao.ReservedVlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.dto.topo.enums.DeviceType;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.dto.topo.enums.UrnType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static net.es.oscars.helpers.IntRangeParsing.intRangesFromIntegers;

@Slf4j
@Service
@Component
public class VlanService {
    @Autowired
    private UrnRepository urnRepository;

    @Autowired
    private ReservedVlanRepository resvVlanRepo;

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
      BUILD RESERVED/AVAILABLE/REQUESTED VLAN COLLECTIONS/MAPS
      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Given a set of reserved junctions and reserved ethernet pipes, retrieve all reserved VLAN objects from Repo
     * within the specified schedule period and from the reserved junctions/pipes.
     *
     * @param reservedJunctions - Set of reserved junctions
     * @param reservedEthPipes  - Set of reserved ethernet pipes
     * @return
     */
    public List<ReservedVlanE> createReservedVlanListFromJunctionsPipesRepo(Set<ReservedVlanJunctionE> reservedJunctions,
                                                      Set<ReservedEthPipeE> reservedEthPipes, Date start, Date end) {

        // Retrieve all VLAN IDs reserved so far from junctions & pipes
        List<ReservedVlanE> rsvVlans = getReservedVlansFromJunctions(reservedJunctions);
        rsvVlans.addAll(getReservedVlansFromEthernetPipes(reservedEthPipes));
        rsvVlans.addAll(getReservedVlansFromRepo(start, end));

        return rsvVlans;
    }

    /**
     * Given a set of reserved junctions and reserved ethernet pipes, retrieve all reserved VLAN objects
     * within the specified schedule period.
     *
     * @param reservedJunctions - Set of reserved junctions
     * @param reservedEthPipes  - Set of reserved ethernet pipes
     * @return
     */
    public List<ReservedVlanE> createReservedVlanList(Set<ReservedVlanJunctionE> reservedJunctions,
                                                      Set<ReservedEthPipeE> reservedEthPipes) {

        // Retrieve all VLAN IDs reserved so far from junctions & pipes
        List<ReservedVlanE> rsvVlans = getReservedVlansFromJunctions(reservedJunctions);
        rsvVlans.addAll(getReservedVlansFromEthernetPipes(reservedEthPipes));

        return rsvVlans;
    }

    private Map<String, Set<Integer>> buildRequestedVlanIdMap(RequestedVlanPipeE reqPipe,
                                                              Map<String, Set<Integer>> availableVlanMap) {
        Map<String, Set<Integer>> requestedVlanIdMap = new HashMap<>();

        Set<RequestedVlanFixtureE> fixtures = new HashSet<>(reqPipe.getAJunction().getFixtures());
        fixtures.addAll(reqPipe.getZJunction().getFixtures());

        for (RequestedVlanFixtureE fix : fixtures) {
            String urn_s = fix.getPortUrn();
            Set<Integer> requestedVlans = getIntegersFromRanges(getIntRangesFromString(fix.getVlanExpression()));
            if (requestedVlans.isEmpty()) {
                requestedVlanIdMap.put(urn_s, availableVlanMap.getOrDefault(urn_s, new HashSet<>()));
            } else {
                requestedVlanIdMap.put(urn_s, requestedVlans);
            }

        }
        return requestedVlanIdMap;
    }

    private Map<String, Set<Integer>> buildRequestedVlanIdMap(Set<RequestedVlanFixtureE> fixtures,
                                                              Map<String, Set<Integer>> availableVlanMap) {
        Map<String, Set<Integer>> requestedVlanIdMap = new HashMap<>();

        for (RequestedVlanFixtureE fix : fixtures) {
            Set<Integer> requestedVlans = getIntegersFromRanges(getIntRangesFromString(fix.getVlanExpression()));
            if (requestedVlans.isEmpty()) {
                requestedVlanIdMap.put(fix.getPortUrn(), availableVlanMap.get(fix.getPortUrn()));
            } else {
                requestedVlanIdMap.put(fix.getPortUrn(), requestedVlans);
            }
        }
        return requestedVlanIdMap;
    }

    public Map<String, Set<Integer>> buildAvailableVlanIdMap(Map<String, UrnE> urnMap,
                                                             List<ReservedVlanE> reservedVlans,
                                                             Map<String, String> portToDeviceMap) {

        // Build empty map of available VLAN IDs per URN
        Map<String, Set<Integer>> availableVlanIdMap = new HashMap<>();

        // Get map of all reserved VLAN IDs per URN
        Map<String, Set<Integer>> reservedVlanIdMap = buildReservedVlanIdMap(urnMap, reservedVlans);
        String stringifyVlanMap = stringifyVlanMap(reservedVlanIdMap);
        //log.info("Reserved VLAN ID Map: " + stringifyVlanMap);

        // Get map of all reservable VLAN IDs per URN
        Map<String, Set<Integer>> reservableVlanIdMap = buildReservableVlanIdMap(urnMap);
        stringifyVlanMap = stringifyVlanMap(reservableVlanIdMap);
        //log.info("Reservable VLAN ID Map: " + stringifyVlanMap);

        if (urnMap == null) {
            log.error("null URN map!");
            return availableVlanIdMap;

        }

        urnMap.values().stream()
                .filter(urn -> urn.getUrnType().equals(UrnType.IFCE))
                .forEach(urn -> {
                    // if we don't get any vlans from the ifce urn,
                    if (urn.getReservableVlans() == null) {
                        // check on the device
                        UrnE deviceUrn = urnMap.get(portToDeviceMap.get(urn.getUrn()));
                        if (deviceUrn == null) {
                            log.error("could not find device for port: " + urn.getUrn());
                        } else {
                            if (deviceUrn.getReservableVlans() == null) {
                                // there aren't any on the device :(
                                availableVlanIdMap.put(urn.getUrn(), new HashSet<>());
                            } else {
                                // there are some on the device :)
                                Set<Integer> availableIds = reservableVlanIdMap.get(deviceUrn.getUrn());
                                availableIds.removeAll(reservedVlanIdMap.get(urn.getUrn()));
                                availableVlanIdMap.put(urn.getUrn(), availableIds);
                            }
                        }
                    } else {
                        // grab them from the ifce URN
                        Set<Integer> availableIds = reservableVlanIdMap.get(urn.getUrn());
                        availableIds.removeAll(reservedVlanIdMap.get(urn.getUrn()));
                        availableVlanIdMap.put(urn.getUrn(), availableIds);
                    }
                });

        stringifyVlanMap = stringifyVlanMap(availableVlanIdMap);
        //log.info("Available VLAN ID Map: " + stringifyVlanMap);

        return availableVlanIdMap;
    }

    public String stringifyVlanMap(Map<String, Set<Integer>> input) {
        Map<String, String> output = new HashMap<>();
        input.keySet().forEach(urn -> {
            List<IntRange> ranges = intRangesFromIntegers(input.get(urn));

            String row = ranges.toString();

            output.put(urn, row);
        });

        return output.toString();

    }


    private Map<String, Set<Integer>> buildReservableVlanIdMap(Map<String, UrnE> urnMap) {
        Map<String, Set<Integer>> reservableVlanIdMap = new HashMap<>();

        urnMap.values().stream()
                .filter(urn_e -> urn_e.getReservableVlans() != null)
                .forEach(urn_e -> {
                    List<IntRange> ranges = urn_e.getReservableVlans().getVlanRanges().stream().map(IntRangeE::toDtoIntRange).collect(Collectors.toList());
                    reservableVlanIdMap.put(urn_e.getUrn(), getIntegersFromRanges(ranges));
                });
        return reservableVlanIdMap;
    }

    private Map<String, Set<Integer>> buildReservedVlanIdMap(Map<String, UrnE> urnMap, List<ReservedVlanE> reservedVlans) {
        Map<String, Set<Integer>> reservedVlanIdMap = new HashMap<>();
        urnMap.values().stream()
                .filter(urn_e -> urn_e.getUrnType().equals(UrnType.IFCE))
                .forEach(urn_e -> reservedVlanIdMap.put(urn_e.getUrn(), new HashSet<>()));

        for (ReservedVlanE rsvVlan : reservedVlans) {
            Integer vlanId = rsvVlan.getVlan();
            String urn = rsvVlan.getUrn();
            reservedVlanIdMap.get(urn).add(vlanId);
        }
        return reservedVlanIdMap;
    }

    private Map<String, Set<Integer>> buildValidVlanIdMap(Map<String, Set<Integer>> requestedVlanMap,
                                                        Map<String, Set<Integer>> availableVlanMap) {

        Map<String, Set<Integer>> validVlanIdMap = new HashMap<>();


        for (String urn_s : requestedVlanMap.keySet()) {

            Set<Integer> availableVlans = availableVlanMap.get(urn_s);
            Set<Integer> requestedVlans = requestedVlanMap.get(urn_s);


            Set<Integer> validVlans = new HashSet<>();
            validVlans.addAll(requestedVlans);
            validVlans.retainAll(availableVlans);
            validVlanIdMap.put(urn_s, validVlans);
        }
        return validVlanIdMap;
    }


    /**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * GET VLAN COLLECTIONS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Get a list of all VLAN IDs reserved between a start and end date/time.
     *
     * @param start - The start of the time range
     * @param end   - The end of the time range
     * @return A list of reserved VLAN IDs
     */
    public List<ReservedVlanE> getReservedVlansFromRepo(Date start, Date end) {
        //Get all Reserved VLan between start and end
        Optional<List<ReservedVlanE>> optResvVlan = resvVlanRepo.findOverlappingInterval(start.toInstant(), end.toInstant());
        return optResvVlan.isPresent() ? optResvVlan.get() : new ArrayList<>();
    }

    /**
     * Retrieve all reserved VLAN IDs from a set of reserved junctions
     *
     * @param junctions - Set of reserved junctions.
     * @return A list of all VLAN IDs reserved at those junctions.
     */
    public List<ReservedVlanE> getReservedVlansFromJunctions(Set<ReservedVlanJunctionE> junctions) {
        return junctions
                .stream()
                .map(ReservedVlanJunctionE::getFixtures)
                .flatMap(Collection::stream)
                .map(ReservedVlanFixtureE::getReservedVlans)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all Reserved VLAN IDs from a set of reserved pipes.
     *
     * @param reservedPipes - Set of reserved pipes
     * @return A list of all reserved VLAN IDs within the set of reserved pipes.
     */
    public List<ReservedVlanE> getReservedVlansFromEthernetPipes(Set<ReservedEthPipeE> reservedPipes) {
        List<ReservedVlanE> reservedVlans = new ArrayList<>();
        Set<ReservedVlanJunctionE> junctions = new HashSet<>();
        for (ReservedEthPipeE pipe : reservedPipes) {
            junctions.add(pipe.getAJunction());
            junctions.add(pipe.getZJunction());
            reservedVlans.addAll(pipe.getReservedVlans());
        }
        reservedVlans.addAll(getReservedVlansFromJunctions(junctions));
        return reservedVlans;
    }


    /**
     * Get all VLAN IDs available at a URN based on the possible reservable ranges and the currently reserved IDs.
     *
     * @param urn         - The currently considered URN string
     * @param resvVlanMap - A mapping representing the Reserved VLANs at a URN
     * @return Set of VLAN IDs that are both supported and not reserved at a URN
     */
    public Set<Integer> getAvailableVlanIds(String urn, Map<String, Set<ReservedVlanE>> resvVlanMap,
                                            Map<String, Set<String>> deviceToPortMap, Map<String, UrnE> urnMap) {
        // Get the supported reservable VLAN IDs
        Set<Integer> reservableVlanIds = new HashSet<>();
        UrnE topoUrn = urnMap.get(urn);

        if (topoUrn.getReservableVlans() != null) {
            reservableVlanIds = getIntegersFromRanges(topoUrn.getReservableVlans().getVlanRanges().stream().map(IntRangeE::toDtoIntRange).collect(Collectors.toList()));
        }

        // Get reserved VLANs (if any) from URN
        Set<ReservedVlanE> reservedVlans = resvVlanMap.getOrDefault(urn, new HashSet<>());
        // Get reserved VLANs (if any) from URN's ports (if they exist)
        deviceToPortMap.getOrDefault(urn, new HashSet<>()).stream().filter(urnMap::containsKey).forEach(portName -> {
            reservedVlans.addAll(resvVlanMap.getOrDefault(urnMap.get(portName), new HashSet<>()));
        });
        // If nothing's reserved, return reservable
        if (reservedVlans.isEmpty()) {
            return reservableVlanIds;
        }
        // Otherwise, return the reservable IDs that are not reserved
        else {
            Set<Integer> reservedVlanIds = reservedVlans.stream().map(ReservedVlanE::getVlan).collect(Collectors.toSet());
            return reservableVlanIds
                    .stream()
                    .filter(id -> !reservedVlanIds.contains(id))
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Using the list of URNs and the URN string, find the matching UrnE object and retrieve all of its
     * reservable IntRanges.
     *
     * @param urnMap      - Map of URN name to UrnE object.
     * @param matchingUrn - String representation of the desired URN.
     * @return - All IntRanges supported at the URN.
     */
    public List<IntRange> getVlanRangesFromUrnString(Map<String, UrnE> urnMap, String matchingUrn) {
        if (urnMap.get(matchingUrn) == null || urnMap.get(matchingUrn).getReservableVlans() == null)
            return new ArrayList<>();
        return urnMap.get(matchingUrn)
                .getReservableVlans()
                .getVlanRanges()
                .stream()
                .map(IntRangeE::toDtoIntRange)
                .collect(Collectors.toList());
    }

    /**
     * Get the requested set of VLAN tags from a junction by streaming through the fixtures in the junction.
     *
     * @param junction - The requested VLAN junction.
     * @return The set of VLAN tags (Integers) requested for fixtures at that junction.
     */
    public List<IntRange> getVlansFromJunction(RequestedVlanJunctionE junction) {
        // Stream through the junction's fixtures, map the requested VLAN expression to a set of Integers
        return junction.getFixtures().stream()
                .map(RequestedVlanFixtureE::getVlanExpression)
                .map(this::getIntRangesFromString)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Select VLANs for Junctions/Pipes/Paths, Evaluate paths for sufficient VLANs
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Select a VLAN ID for a junction. All fixtures on the junction must use their requested VLAN tag.
     *
     * @param req_j           - The requested junction.
     * @param deviceToPortMap
     * @param portToDeviceMap @return A valid VLAN iD for each URN on this junction.
     */
    public Map<String, Set<Integer>> selectVLANsForJunction(RequestedVlanJunctionE req_j,
                                                            List<ReservedVlanE> rsvVlans,
                                                            Map<String, Set<String>> deviceToPortMap,
                                                            Map<String, String> portToDeviceMap,
                                                            Map<String, UrnE> urnMap) {

        //GOAL - Pick the minimum set of VLANs (V) that can satisfy the following requirements:
        // (1) Each fixture has one of the VLAN IDs it requested
        // (2) If switch, then all VLANs in V must be available on all ports (check the switch)
        // (3) If router, then VLAN assigned to port must be available on that port

        Map<String, Set<ReservedVlanE>> rsvVlanMap = new HashMap<>();
        for (ReservedVlanE rsvVlan : rsvVlans) {
            rsvVlanMap.putIfAbsent(rsvVlan.getUrn(), new HashSet<>());
            rsvVlanMap.get(rsvVlan.getUrn()).add(rsvVlan);
        }

        // All requested fixtures/ports
        Set<RequestedVlanFixtureE> reqFixtures = req_j.getFixtures();
        Map<String, Set<Integer>> reqVlanMap = new HashMap<>();

        Map<String, Set<Integer>> validVlanMap = new HashMap<>();


        // Use the device's available VLANs
        UrnE j_deviceUrn = urnMap.get(req_j.getDeviceUrn());

        if (j_deviceUrn.getReservableVlans() != null) {
            String deviceUrn = req_j.getDeviceUrn();
            Set<String> devicePortUrns = deviceToPortMap.get(req_j.getDeviceUrn()).stream().collect(Collectors.toSet());

            Set<Integer> availableVlans = getAvailableVlanIds(deviceUrn, rsvVlanMap, deviceToPortMap, urnMap);
            Map<String, Set<Integer>> availableVlanMap = devicePortUrns.stream().collect(Collectors.toMap(urn -> urn, urn -> availableVlans));

            reqVlanMap = buildRequestedVlanIdMap(reqFixtures, availableVlanMap);

            for (String devicePortUrn : devicePortUrns) {
                reqVlanMap.putIfAbsent(devicePortUrn, new HashSet<>());
            }

            // Get all of the requested VLANs per URN that are available
            for (String urn : reqVlanMap.keySet()) {
                Set<Integer> validVlans = reqVlanMap.get(urn);
                if (validVlans.isEmpty()) {
                    validVlans = availableVlans;
                } else {
                    validVlans.retainAll(availableVlans);
                }
                validVlanMap.put(urn, validVlans);
            }
            // First: Check if any URN does not have any valid VLANs - return an invalid map
            if (validVlanMap.values().stream().anyMatch(Set::isEmpty)) {
                return devicePortUrns.stream().collect(Collectors.toMap(urn -> urn, urn -> new HashSet<>()));
            }
            Set<Integer> chosenVlans = findSetCover(validVlanMap);
            return devicePortUrns.stream().collect(Collectors.toMap(urn -> urn, urn -> chosenVlans));
        }
        // Use the ports' available vlans
        else {
            Map<String, Set<Integer>> vlanIdPerPort = new HashMap<>();
            Map<String, Set<Integer>> availableVlanMap = reqFixtures.stream()
                    .map(RequestedVlanFixtureE::getPortUrn)
                    .collect(Collectors.toMap(urn -> urn, urn -> getAvailableVlanIds(urn, rsvVlanMap, deviceToPortMap, urnMap)));
            reqVlanMap = buildRequestedVlanIdMap(reqFixtures, availableVlanMap);
            // Get all of the requested VLANs per URN that are available
            for (String urn : reqVlanMap.keySet()) {
                Set<Integer> validVlans = reqVlanMap.get(urn);
                // There are no requested VLANs
                if (validVlans.isEmpty()) {
                    validVlans = availableVlanMap.get(urn);
                    // If there are none available, then fail
                    if (validVlans.isEmpty()) {
                        return reqVlanMap.keySet().stream().collect(Collectors.toMap(u -> u, u -> new HashSet<>()));
                    }
                } else {
                    validVlans.retainAll(availableVlanMap.get(urn));
                }
                validVlanMap.put(urn, validVlans);
            }

            // Choose the minimum set of VLANs that can cover all of the fixture URNs
            Set<Integer> chosenVlans = findSetCover(validVlanMap);
            for (Integer vlan : chosenVlans) {
                for (String urn : reqVlanMap.keySet()) {
                    vlanIdPerPort.putIfAbsent(urn, new HashSet<>());
                    if (reqVlanMap.get(urn).contains(vlan) && vlanIdPerPort.get(urn).isEmpty()) {
                        vlanIdPerPort.get(urn).add(vlan);
                    }
                }
            }

            return vlanIdPerPort;
        }

    }

    public Set<Integer> findSetCover(Map<String, Set<Integer>> validVlanMap) {
        // Build a map from each VLAN ID to the set of URNs that it covers
        Map<Integer, Set<String>> coverMap = new HashMap<>();
        for (String urn : validVlanMap.keySet()) {
            Set<Integer> vlans = validVlanMap.get(urn);
            for (Integer vlan : vlans) {
                coverMap.putIfAbsent(vlan, new HashSet<>());
                coverMap.get(vlan).add(urn);
            }
        }

        Long numCovered = 0L;
        Map<String, Boolean> isCoveredMap = validVlanMap.keySet().stream().collect(Collectors.toMap(urn -> urn, urn -> false));
        Set<Integer> chosenVlans = new HashSet<>();
        while (numCovered < validVlanMap.keySet().size()) {
            Integer bestVlan = -1;
            Long largestNewCovers = 0L;
            for (Integer vlan : coverMap.keySet()) {
                Set<String> thisVlanUrns = coverMap.get(vlan);
                Long numUncovered = thisVlanUrns.stream().filter(urn -> !isCoveredMap.get(urn)).count();
                if (numUncovered > largestNewCovers) {
                    bestVlan = vlan;
                    largestNewCovers = numUncovered;
                }
            }
            // No VLAN chosen, there was no way to increase the number of covered URNs
            if (bestVlan == -1) {
                return new HashSet<>();
            } else {
                chosenVlans.add(bestVlan);
                numCovered += largestNewCovers;
            }
        }

        return chosenVlans;
    }

    /**
     * Return a mapping of URN entity to VLAN Integer ID. This map will contain the URNs of all fixtures, junctions,
     * and ports in-between the junction.
     *
     * @param reqPipe         - The requested pipe.
     * @param urnMap          - A mapping of URN string to URN entity
     * @param reservedVlans   - List of all reserved VLANs
     * @param azERO           - The AZ edges
     * @param zaERO           - The ZA edges
     * @param deviceToPortMap
     * @param portToDeviceMap @return Map from URN entity to VLAN ID chosen for that entity.
     */
    public Map<String, Set<Integer>> selectVlansForPipe(RequestedVlanPipeE reqPipe,
                                                        Map<String, UrnE> urnMap,
                                                        List<ReservedVlanE> reservedVlans,
                                                        List<TopoEdge> azERO,
                                                        List<TopoEdge> zaERO,
                                                        Map<String, Set<String>> deviceToPortMap,
                                                        Map<String, String> portToDeviceMap) {

        // Confirm that there is at least one VLAN ID that can support every segment (given what has been reserved so far)
        // Get the available VLANs at all URNs
        Map<String, Set<Integer>> availableVlanMap = buildAvailableVlanIdMap(urnMap, reservedVlans, portToDeviceMap);

        // Get the requested VLANs per Fixture URN & Any Ports at Individual URNs
        Map<String, Set<Integer>> requestedVlanMap = buildRequestedVlanIdMap(reqPipe, availableVlanMap);
        //log.info("Requested Vlan Map: " + stringifyVlanMap(requestedVlanMap));

        // Get the "valid" VLANs per Fixture URN
        Map<String, Set<Integer>> validVlanMap = buildValidVlanIdMap(requestedVlanMap, availableVlanMap);
        //log.info("Valid Vlan Map: " + stringifyVlanMap(validVlanMap));
        // Create a map of URNs to chosen VLAN IDs
        return selectVlansForPath(azERO, zaERO, urnMap, availableVlanMap, validVlanMap, deviceToPortMap, portToDeviceMap, reqPipe);

    }


    /**
     * Given an AZ and ZA path, determine which VLAN IDs will work for those paths. Also needs a URN String -> Entity map,
     * a URN Entity -> Set of available VLANs map, and a URN Entity -> Set of valid VLANs map (available and requested per
     * fixture).
     *
     * @param azERO            - The AZ path (edges)
     * @param zaERO            - The ZA path (edges)
     * @param urnMap           - A map of URN String to Entity
     * @param availableVlanMap - A map of URN entity to available VLANs
     * @param validVlanMap     - A map of fixture URN entity to requested & available VLANs
     * @param deviceToPortMap
     * @param portToDeviceMap
     * @return A map containing the VLAN chosen for each URN (-1 if none possible)
     */
    private Map<String, Set<Integer>> selectVlansForPath(List<TopoEdge> azERO,
                                                         List<TopoEdge> zaERO,
                                                         Map<String, UrnE> urnMap,
                                                         Map<String, Set<Integer>> availableVlanMap,
                                                         Map<String, Set<Integer>> validVlanMap,
                                                         Map<String, Set<String>> deviceToPortMap,
                                                         Map<String, String> portToDeviceMap,
                                                         RequestedVlanPipeE reqPipe) {

        // Retrive port URNs for pipe from the AZ and ZA EROs

        String aJunctionUrn = reqPipe.getAJunction().getDeviceUrn();
        String zJunctionUrn = reqPipe.getZJunction().getDeviceUrn();
        UrnE aJunctionUrn_e = urnMap.get(aJunctionUrn);
        UrnE zJunctionUrn_e = urnMap.get(zJunctionUrn);

        // AND - If there are any switches, add their ports as well
        Set<String> pipeUrns = getUrnsFromListOfEdges(azERO, urnMap, deviceToPortMap);
        pipeUrns.addAll(getUrnsFromListOfEdges(zaERO, urnMap, deviceToPortMap));
        pipeUrns.remove(aJunctionUrn);
        pipeUrns.remove(zJunctionUrn);

        // Get the VLANs available across the AZ/ZA path
        Set<Integer> availableVlansAcrossPath = findAvailableVlansBidirectional(pipeUrns, availableVlanMap);
        if (availableVlansAcrossPath == null) {
            availableVlansAcrossPath = new HashSet<>();
        }

        // Get the valid VLANs across the fixtures
        Set<Integer> availableVlansAcrossFixtures = getVlanOverlapAcrossMap(validVlanMap);

        // Get the VLANs available across non-fixture ports at both junctions
        Map<String, Set<String>> nonFixPortUrnMap = new HashMap<>();
        Map<String, Set<Integer>> nonFixPortVlansMap = new HashMap<>();
        populateDeviceVlanMaps(deviceToPortMap, aJunctionUrn, zJunctionUrn, urnMap, validVlanMap, availableVlanMap, nonFixPortUrnMap, nonFixPortVlansMap);

        //Initialize chosen VLAN map
        Set<String> fixtures = reqPipe.getAJunction().getFixtures().stream().map(RequestedVlanFixtureE::getPortUrn).collect(Collectors.toSet());
        fixtures.addAll(reqPipe.getZJunction().getFixtures().stream().map(RequestedVlanFixtureE::getPortUrn).collect(Collectors.toSet()));
        Map<String, Set<Integer>> chosenVlanMap = initializeChosenVlanMap(pipeUrns, fixtures);


        // If there is any overlap between these sets, use this ID for everything
        Set<Integer> availableEverywhere = availableVlansAcrossFixtures != null ? new HashSet<>(availableVlansAcrossFixtures) : new HashSet<>();
        availableEverywhere.retainAll(availableVlansAcrossPath);
        if (isSwitch(aJunctionUrn_e)) {
            availableEverywhere.retainAll(nonFixPortVlansMap.get(aJunctionUrn));
        }
        if (isSwitch(zJunctionUrn_e)) {
            availableEverywhere.retainAll(nonFixPortVlansMap.get(zJunctionUrn));
        }
        // If there is at least one VLAN available everywhere, reserve it at each URN
        if (!availableEverywhere.isEmpty()) {
            List<Integer> options = availableEverywhere.stream().sorted().collect(Collectors.toList());
            Integer chosenVlan = options.get(0);
            chosenVlanMap = pipeUrns.stream().collect(Collectors.toMap(u -> u, u -> Collections.singleton(chosenVlan)));
            for (String fixUrn : validVlanMap.keySet()) {
                chosenVlanMap.putIfAbsent(fixUrn, Collections.singleton(chosenVlan));
            }
            // Reserve VLANs at the other ports if Junction A is a switch
            if (isSwitch(aJunctionUrn_e)) {
                for (String aNonFixUrn : nonFixPortUrnMap.get(aJunctionUrn)) {
                    chosenVlanMap.putIfAbsent(aNonFixUrn, Collections.singleton(chosenVlan));
                }
            }
            // Reserve VLANs at the other ports if Junction Z is a switch
            if (isSwitch(zJunctionUrn_e)) {
                for (String zNonFixUrn : nonFixPortUrnMap.get(zJunctionUrn)) {
                    chosenVlanMap.putIfAbsent(zNonFixUrn, Collections.singleton(chosenVlan));
                }
            }
        }
        // Otherwise, iterate through the valid vlans per fixture
        // For each valid VLAN, see if it is available across the path
        // If so, use that for the path
        // Either way, choose a valid VLAN for the fixture
        else {
            boolean pipeVlansAssigned = false;
            Integer numFixturesAssigned = 0;

            Map<String, Set<Integer>> vlansAssignedJunctionMap = new HashMap<>();
            vlansAssignedJunctionMap.put(aJunctionUrn, new HashSet<>());
            vlansAssignedJunctionMap.put(zJunctionUrn, new HashSet<>());

            // For each fixture, find if there are any overlapping VLANs between the path and the fixture
            List<String> sortedFixtureUrns = validVlanMap.keySet().stream()
                    .sorted(String::compareToIgnoreCase)
                    .collect(Collectors.toList());

            //Attempt to assign VLANs to the fixtures
            for (String fixUrn : sortedFixtureUrns) {
                UrnE parentDeviceUrn_e = urnMap.get(portToDeviceMap.get(fixUrn));

                Set<Integer> overlappingVlans = new HashSet<>();
                overlappingVlans.addAll(validVlanMap.get(fixUrn));

                overlappingVlans.retainAll(availableVlansAcrossPath);
                // If the parent device is a switch, add the available VLANs for that device to the overlap
                if (isSwitch(parentDeviceUrn_e)) {
                    Set<Integer> nfpvm = nonFixPortVlansMap.getOrDefault(parentDeviceUrn_e.getUrn(), new HashSet<>());
                    overlappingVlans.retainAll(nfpvm);
                }
                // If there is at least one VLAN ID in common between this fixture and the other ports in the path
                // AND the other ports on the switch (if this fixture is on a switch)
                if (!overlappingVlans.isEmpty() && !pipeVlansAssigned) {
                    pipeVlansAssigned = true;
                    // Choose VLAN ID
                    List<Integer> options = overlappingVlans.stream().sorted().collect(Collectors.toList());
                    Integer chosenVlan = options.get(0);
                    // Assign this VLAN to every URN in the pipe
                    for (String pipeUrn : pipeUrns) {
                        chosenVlanMap.get(pipeUrn).add(chosenVlan);
                    }
                    // Assign this VLAN to other ports on the switch (if junction is a switch)
                    if (isSwitch(parentDeviceUrn_e)) {
                        vlansAssignedJunctionMap.get(parentDeviceUrn_e.getUrn()).add(chosenVlan);
                        for (String portUrn : nonFixPortUrnMap.get(parentDeviceUrn_e.getUrn())) {
                            chosenVlanMap.get(portUrn).add(chosenVlan);
                        }
                    }

                    //Increment the number of fixtures assigned
                    numFixturesAssigned++;
                }
                // If there's at least one VLAN that can work for this fixture, reserve it
                // (ONLY if it is also available on the other ports on the device, if the device is a switch)
                if (!validVlanMap.get(fixUrn).isEmpty()) {
                    List<Integer> options = validVlanMap.get(fixUrn).stream().sorted().collect(Collectors.toList());
                    // Retain only the VLANs that are also available at other ports on the device (if it is a switch)
                    if (isSwitch(parentDeviceUrn_e)) {
                        Set<Integer> nfpvm = nonFixPortVlansMap.getOrDefault(parentDeviceUrn_e.getUrn(), new HashSet<>());
                        options.retainAll(nfpvm);
                    }
                    // If available options, choose a VLAN and assign it to the fixture and the port URNs (if device is a switch)
                    if (!options.isEmpty()) {
                        Integer chosenVlan = options.get(0);
                        chosenVlanMap.get(fixUrn).add(chosenVlan);
                        if (isSwitch(parentDeviceUrn_e)) {
                            vlansAssignedJunctionMap.get(parentDeviceUrn_e.getUrn()).add(chosenVlan);
                            for (String portUrn : nonFixPortUrnMap.get(parentDeviceUrn_e.getUrn())) {
                                chosenVlanMap.get(portUrn).add(chosenVlan);
                            }
                        }
                        //Increment the number of fixtures assigned
                        numFixturesAssigned++;
                    }
                }
            }

            // If no VLAN tags available at fixtures could be used, assign any VLAN available across the path
            // To every port in the path (excluding fixtures)
            if (!pipeVlansAssigned && numFixturesAssigned == sortedFixtureUrns.size()) {
                // If there's at least one VLAN ID available across the path
                if (!availableVlansAcrossPath.isEmpty()) {
                    // Choose VLAN ID
                    List<Integer> options = availableVlansAcrossPath.stream().sorted().collect(Collectors.toList());
                    Integer chosenVlan = options.get(0);
                    // Assign this VLAN to every URN in the pipe
                    for (String pipeUrn : pipeUrns) {
                        // Assign this VLAN to other ports on the switch (if this URN is on switch junction A)
                        if (nonFixPortUrnMap.get(aJunctionUrn).contains(pipeUrn) && isSwitch(aJunctionUrn_e)) {
                            vlansAssignedJunctionMap.get(aJunctionUrn).add(chosenVlan);
                            for (String portUrn : nonFixPortUrnMap.get(aJunctionUrn)) {
                                chosenVlanMap.get(portUrn).add(chosenVlan);
                            }
                        }
                        // Assign this VLAN to other ports on the switch (if this URN is on switch junction Z)
                        if (nonFixPortUrnMap.get(zJunctionUrn).contains(pipeUrn) && isSwitch(zJunctionUrn_e)) {
                            vlansAssignedJunctionMap.get(zJunctionUrn).add(chosenVlan);
                            for (String portUrn : nonFixPortUrnMap.get(zJunctionUrn)) {
                                chosenVlanMap.get(portUrn).add(chosenVlan);
                            }
                        }
                        chosenVlanMap.get(pipeUrn).add(chosenVlan);
                    }
                }
            }


            for (String fixUrn : sortedFixtureUrns) {
                UrnE parentDeviceUrn = urnMap.get(portToDeviceMap.get(fixUrn));
                if (!chosenVlanMap.get(fixUrn).isEmpty() && isSwitch(parentDeviceUrn)) {
                    chosenVlanMap.put(fixUrn, vlansAssignedJunctionMap.get(parentDeviceUrn.getUrn()));
                }
            }
        }


        return chosenVlanMap;
    }

    private Map<String,Set<Integer>> initializeChosenVlanMap(Set<String> pipeUrns, Set<String> fixtures) {

        //log.info("Pipe URNs: " + pipeUrns.toString());
        Map<String, Set<Integer>> chosenVlanMap = new HashMap<>();
        for(String pipeUrn : pipeUrns){
            chosenVlanMap.put(pipeUrn, new HashSet<>());
        }
        for(String fixtureUrn : fixtures){
            chosenVlanMap.put(fixtureUrn, new HashSet<>());
        }

        return chosenVlanMap;
    }

    private boolean isSwitch(UrnE urn) {
        return urn.getUrnType().equals(UrnType.DEVICE) && urn.getDeviceType().equals(DeviceType.SWITCH);
    }

    private void populateDeviceVlanMaps(Map<String, Set<String>> deviceToPortMap,
                                        String aJunctionUrn,
                                        String zJunctionUrn,
                                        Map<String, UrnE> urnMap,
                                        Map<String, Set<Integer>> validVlanMap,
                                        Map<String, Set<Integer>> availableVlanMap,
                                        Map<String, Set<String>> nonFixPortUrnMap,
                                        Map<String, Set<Integer>> nonFixPortVlansMap) {

        Set<String> aNonFixJunctionPortUrns = deviceToPortMap.get(aJunctionUrn)
                .stream()
                .filter(urnMap::containsKey)
                .filter(urn -> !validVlanMap.containsKey(urn))
                .collect(Collectors.toSet());

        Set<String> zNonFixJunctionPortUrns = deviceToPortMap.get(zJunctionUrn)
                .stream()
                .filter(urnMap::containsKey)
                .filter(urn -> !validVlanMap.containsKey(urn))
                .collect(Collectors.toSet());

        Set<Integer> aAvailableVlansNonFix = findAvailableVlansBidirectional(aNonFixJunctionPortUrns, availableVlanMap);
        Set<Integer> zAvailableVlansNonFix = findAvailableVlansBidirectional(zNonFixJunctionPortUrns, availableVlanMap);

        nonFixPortUrnMap.put(aJunctionUrn, aNonFixJunctionPortUrns);
        nonFixPortUrnMap.put(zJunctionUrn, zNonFixJunctionPortUrns);

        nonFixPortVlansMap.put(aJunctionUrn, aAvailableVlansNonFix);
        nonFixPortVlansMap.put(zJunctionUrn, zAvailableVlansNonFix);
    }


    /**
     * Check both AZ and ZA paths, determine which VLAN IDs are available across both.
     *
     * @param availableVlanMap - Map of available VLAN IDs
     * @return Set of VLAN IDs available across both path edges
     */
    private Set<Integer> findAvailableVlansBidirectional(Set<String> urns, Map<String, Set<Integer>> availableVlanMap) {
        // Ignore the first and last edges of each ERO
        // These edges connect to fixtures
        Set<Integer> availableVlans = null;

        // Loop through all edges in the AZ / ZA combined path
        // Ignore MPLS edges
        for (String urn : urns) {
            Set<Integer> vlans = availableVlanMap.getOrDefault(urn, new HashSet<>());
            if (availableVlans == null) {
                availableVlans = new HashSet<>(vlans);
            } else {
                availableVlans.retainAll(vlans);
            }
        }
        return availableVlans != null ? availableVlans : new HashSet<>();
    }


    /**
     * Return a pruned set of edges where the nodes on either end of the edge support at least one of the specified VLANs.
     * A map of URNs are used to retrive the reservable VLAN sets from nodes (where applicable). Builds a mapping from
     * each available VLAN id to the set of edges that support that ID. Using this mapping, the largest set of edges
     * that supports a requested VLAN id (or any VLAN id if none are specified) is returned.
     *
     * @param availableEdges - The set of currently available edges, which will be pruned further using VLAN tags.
     * @param urnMap         - Map of URN name to UrnE object.
     * @param vlans          - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param availVlanMap   - Map of UrnE objects to a List<> of available VLAN tags.
     * @return The input edges, pruned using the input set of VLAN tags.
     */
    public Set<TopoEdge> findMaxValidEdgeSet(Set<TopoEdge> availableEdges,
                                             Map<String, UrnE> urnMap,
                                             List<IntRange> vlans,
                                             Map<String, Set<Integer>> availVlanMap) {


        // Find a set of matching edges for each available VLAN id
        Map<Integer, Set<TopoEdge>> edgesPerId = findEdgeSetPerVlanID(availableEdges, urnMap, availVlanMap);
        // Get a set of the requested VLAN ids
        Set<Integer> idsInRanges = getIntegersFromRanges(vlans);
        // Find the largest set of TopoEdges that meet the request
        Set<TopoEdge> bestSet = new HashSet<>();
        for (Integer id : edgesPerId.keySet()) {
            // Ignore the set of edges where both terminating nodes do not have reservable VLAN fields
            // Add them to the best set of edges after this loop
            if (id == -1) {
                continue;
            }
            // If the currently considered ID matches the request (or there are no VLANs requested)
            // and the set of edges supporting this ID are larger than the current best
            // choose this set of edges
            if ((idsInRanges.contains(id) || idsInRanges.isEmpty()) && edgesPerId.get(id).size() > bestSet.size()) {
                bestSet = edgesPerId.get(id);
            }
        }
        // Add all edges where neither terminating node has reservable VLAN attributes
        bestSet.addAll(edgesPerId.get(-1));
        return bestSet;
    }


    /**
     * Traverse the set of edges, and create a map of VLAN ID to the edges where that ID is available
     *
     * @param edges  - The set of edges.
     * @param urnMap - Map of URN name to UrnE object.
     * @return A (possibly empty) set of VLAN tags that are available across every edge.
     */
    public Map<Integer, Set<TopoEdge>> findEdgeSetPerVlanID(Set<TopoEdge> edges, Map<String, UrnE> urnMap,
                                                            Map<String, Set<Integer>> availVlanMap) {
        // Overlap is used to track all VLAN tags that are available across every edge.
        Map<Integer, Set<TopoEdge>> edgesPerId = new HashMap<>();
        edgesPerId.put(-1, new HashSet<>());
        for (TopoEdge edge : edges) {
            // Overlap is used to track all VLAN tags that are available across both endpoints of an edge
            Set<Integer> overlap = new HashSet<>();
            // Get all possible VLAN ranges reservable at the a and z ends of the edge
            Set<Integer> aAvailVlans = availVlanMap.getOrDefault(edge.getA().getUrn(), null);
            Set<Integer> zAvailVlans = availVlanMap.getOrDefault(edge.getZ().getUrn(), null);

            // If neither edge has reservable VLAN fields, add the edge to the "-1" VLAN tag list.
            // These edges do not need to be pruned, and will be added at the end to the best set of edges
            Boolean safeA = edge.getA().getVertexType().equals(VertexType.VIRTUAL) || aAvailVlans == null;
            Boolean safeZ = edge.getZ().getVertexType().equals(VertexType.VIRTUAL) || zAvailVlans == null;
            if (safeA || safeZ) {
                edgesPerId.get(-1).add(edge);
            }
            // Otherwise, find the intersection between the VLAN ranges (if any), and add the edge to the list
            // matching each overlapping VLAN ID.
            else {
                // Find what VLAN ids are actually available at A and Z
                if (aAvailVlans.size() > 0) {
                    addToSetOverlap(overlap, aAvailVlans);
                }
                if (zAvailVlans.size() > 0) {
                    addToSetOverlap(overlap, zAvailVlans);
                }

                // For overlapping IDs, put that edge into the map
                for (Integer id : overlap) {
                    if (!edgesPerId.containsKey(id)) {
                        edgesPerId.put(id, new HashSet<>());
                    }
                    edgesPerId.get(id).add(edge);
                }
            }

        }
        return edgesPerId;
    }

    /**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * HELPER METHODS
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */


    /**
     * Given a list of strings, convert all valid strings into IntRanges.
     *
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return A list of IntRanges, each representing a range of VLAN ID values parsed from a string.
     */
    public List<IntRange> getIntRangesFromString(String vlans) {
        if (IntRangeParsing.isValidIntRangeInput(vlans)) {
            try {
                return IntRangeParsing.retrieveIntRanges(vlans);
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    /**
     * Return all of the VLAN ids contained within the list of VLAN ranges.
     *
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The set of VLAN ids making up the input ranges.
     */
    public Set<Integer> getIntegersFromRanges(List<IntRange> vlans) {
        return vlans
                .stream()
                .map(this::getSetOfNumbersInRange)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieve a set of all Integers that fall within the specified IntRange.
     *
     * @param aRange - The specified IntRange
     * @return The set of Integers contained within the range.
     */
    public Set<Integer> getSetOfNumbersInRange(IntRange aRange) {
        Set<Integer> numbers = new HashSet<>();
        for (Integer num = aRange.getFloor(); num <= aRange.getCeiling(); num++) {
            numbers.add(num);
        }
        return numbers;
    }

    /**
     * Iterate through the set of overlapping VLAN tags, keep the elements in that set which are also
     * contained in every IntRange passed in.
     *
     * @param overlap - The set of overlapping VLAN tags.
     * @param other   - Another set of VLAN tags, to be overlapped with the current overlapping set.
     * @return The (possibly reduced) set of overlapping VLAN tags.
     */
    public Set<Integer> addToSetOverlap(Set<Integer> overlap, Set<Integer> other) {
        // If there are no ranges available, just return the current overlap set
        if (other == null || other.isEmpty()) {
            return overlap;
        }
        // If the overlap does not already have elements, add all of the tags retrieved from this range
        if (overlap.isEmpty()) {
            overlap.addAll(other);
        } else {
            // Otherwise, find the intersection between the current overlap and the tags retrieved from this range
            overlap.retainAll(other);
        }
        return overlap;
    }

    /**
     * Given a map of Urn Entity -> Sets of Integers (VLANs), find the overlap across the keyset
     *
     * @param vlanMap - Map of URNs to VLANs
     * @return The intersection across the keyset of the map
     */
    private Set<Integer> getVlanOverlapAcrossMap(Map<String, Set<Integer>> vlanMap) {
        Set<Integer> overlappingVlans = null;
        for (String urn : vlanMap.keySet()) {
            Set<Integer> vlans = vlanMap.get(urn);
            if (overlappingVlans == null) {
                overlappingVlans = new HashSet<>(vlans);
            } else {
                overlappingVlans.retainAll(vlans);
            }
        }
        return overlappingVlans;
    }


    /**
     * Given a list of edges and a URN map, get a list of all URNs in that list.
     *
     * @param edges           - The list of edges
     * @param urnMap          - A map of URN strings to URN entities
     * @param deviceToPortMap
     * @return Set of all URNs across the list of edges
     */
    private Set<String> getUrnsFromListOfEdges(List<TopoEdge> edges, Map<String, UrnE> urnMap,
                                             Map<String, Set<String>> deviceToPortMap) {

        Set<String> urns = new HashSet<>();

        for (TopoEdge edge : edges) {
            List<String> urnStrings = new ArrayList<>();
            urnStrings.add(edge.getA().getUrn());
            urnStrings.add(edge.getZ().getUrn());
            for (String urn_s : urnStrings) {
                UrnE urn_e = urnMap.get(urn_s);
                if (urn_e != null) {
                    if (urn_e.getUrnType().equals(UrnType.DEVICE) && urn_e.getDeviceType().equals(DeviceType.SWITCH)) {
                        Set<String> ports = deviceToPortMap.get(urn_e.getUrn());
                        urns.addAll(ports);
                    } else if (urn_e.getUrnType().equals(UrnType.IFCE) && urn_e.getReservableVlans() != null) {
                        urns.add(urn_e.getUrn());
                    }
                }
            }

        }
        return urns;
    }

}