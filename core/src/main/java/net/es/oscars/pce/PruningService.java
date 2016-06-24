package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.IntRange;
import net.es.oscars.dto.rsrc.ReservableBandwidth;
import net.es.oscars.helpers.IntRangeParsing;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.dao.ReservedVlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.Layer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/***
 * Pruning Service. This class can take in a variety of inputs (Bidrectional bandwidth, a pair of unidirectional
 * bandwidths (AZ/ZA), specified set of desired VLAN tags, or a logical pipe (containing those other elements)).
 * With any/all of those inputs, edges are removed from the passed in topology that do not meet the specified
 * bandwidth/VLAN requirements.
 */
@Slf4j
@Service
@Component
public class PruningService {

    @Autowired
    private UrnRepository urnRepo;

    @Autowired
    private ReservedBandwidthRepository resvBwRepo;

    @Autowired
    private ReservedVlanRepository resvVlanRepo;


    /**
     * Prune the topology using a specified bidirectional bandwidth, set of VLANs, and a list of URNs to
     * match to the topology.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBwVlans(Topology topo, Integer Bw, String vlans, List<UrnE> urns,
                                     List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        return pruneTopology(topo, Bw, Bw, getIntRangesFromString(vlans), urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology using a specified bidirectional bandwidth, and set of VLANs. The URNs are pulled
     * from the URN repository.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBwVlans(Topology topo, Integer Bw, String vlans, Date start, Date end){
        return pruneTopology(topo, Bw, Bw, getIntRangesFromString(vlans), urnRepo.findAll(),
                getReservedBandwidth(start, end), getReservedVlans(start, end));
    }

    /**
     * Prune the topology using a pair of specified unidirectional bandwidths, set of VLANs, and a list of URNs to
     * match to the topology.
     * @param topo - The topology to be pruned.
     * @param azBw - The minimum required undirectional bandwidth in one direction.
     * @param zaBw - The minimum required undirectional bandwidth in the other direction.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithAZBwVlans(Topology topo, Integer azBw, Integer zaBw, String vlans, List<UrnE> urns,
                                       List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        return pruneTopology(topo, azBw, zaBw, getIntRangesFromString(vlans), urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology using a pair of specified unidirectional bandwidths, and set of VLANs. The URNs are pulled
     * from the URN repository.
     * @param topo - The topology to be pruned.
     * @param azBw - The minimum required undirectional bandwidth in one direction.
     * @param zaBw - The minimum required undirectional bandwidth in the other direction.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithAZBwVlans(Topology topo, Integer azBw, Integer zaBw, String vlans, Date start, Date end){
        return pruneTopology(topo, azBw, zaBw, getIntRangesFromString(vlans), urnRepo.findAll(),
                getReservedBandwidth(start, end), getReservedVlans(start, end));
    }

    /**
     * Prune the topology using a specified bidirectional bandwidth, set of VLANs, and a list of URNs to
     * match to the topology.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBw(Topology topo, Integer Bw, List<UrnE> urns,
                                List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        return pruneTopology(topo, Bw, Bw, new ArrayList<>(), urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology using a specified bidirectional bandwidth. With no VLAN specified, the
     * pruning service will search for at least one VLAN (if not several) that is available across the topology,
     * and prune using that set of VLANs. The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param Bw - The minimum required bidirectional Bandwidth.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithBw(Topology topo, Integer Bw, Date start, Date end){
        return pruneTopology(topo, Bw, Bw, new ArrayList<>(), urnRepo.findAll(),
                getReservedBandwidth(start, end), getReservedVlans(start, end));
    }

    /**
     * Prune the topology using a pair of specified unidirectional bandwidths, and a list of URNs.
     * With no VLAN specified, the pruning service will search for at least one VLAN (if not several) that is
     * available across the topology, and prune using that set of VLANs.
     * @param topo - The topology to be pruned.
     * @param azBw - The minimum required undirectional bandwidth in one direction.
     * @param zaBw - The minimum required undirectional bandwidth in the other direction.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithAZBw(Topology topo, Integer azBw, Integer zaBw, List<UrnE> urns,
                                  List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        return pruneTopology(topo, azBw, zaBw, new ArrayList<>(), urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology using a pair of specified unidirectional bandwidths. With no VLAN specified, the
     * pruning service will search for at least one VLAN (if not several) that is available across the topology,
     * and prune using that set of VLANs. The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param azBw - The minimum required undirectional bandwidth in one direction.
     * @param zaBw - The minimum required undirectional bandwidth in the other direction.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithAZBw(Topology topo, Integer azBw, Integer zaBw, Date start, Date end){
        return pruneTopology(topo, azBw, zaBw, new ArrayList<>(), urnRepo.findAll(),
                getReservedBandwidth(start, end), getReservedVlans(start, end));
    }


    /**
     * Prune the topology using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). The URNs are pulled from the URN repository.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe, Date start, Date end){
        return pruneWithPipe(topo, pipe, urnRepo.findAll(),
                getReservedBandwidth(start, end), getReservedVlans(start, end));
    }

    /**
     * Prune the topology using a logical pipe. The pipe contains the requested bandwidth and VLANs (through
     * querying the attached junctions/fixtures). A list of URNs is passed into match devices/interfaces to
     * topology elements.
     * @param topo - The topology to be pruned.
     * @param pipe - The logical pipe, from which the requested bandwidth and VLANs are retrieved.
     * @param urns - The URNs that will be used to match available resources with elements of the topology.
     * @return The topology with ineligible edges removed.
     */
    public Topology pruneWithPipe(Topology topo, RequestedVlanPipeE pipe, List<UrnE> urns,
                                  List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        Integer azBw = pipe.getAzMbps();
        Integer zaBw = pipe.getZaMbps();
        List<IntRange> vlans = new ArrayList<>();
        vlans.addAll(getVlansFromJunction(pipe.getAJunction()));
        vlans.addAll(getVlansFromJunction(pipe.getZJunction()));
        return pruneTopology(topo, azBw, zaBw, vlans, urns, rsvBwList, rsvVlanList);
    }

    /**
     * Prune the topology. Called by all outward facing methods to actually perform the pruning. Given the parameters,
     * filter out the edges where the terminating nodes (a/z) do not meet the bandwidth/vlan requirements.
     * @param topo - The topology to be pruned.
     * @param azBw - The required bandwidth that must be supported in one direction on each edge.
     * @param zaBw - The required bandwidth that must be supported in the other direction on each edge.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param urns - The URNs that will be matched to elements in the topology.
     * @return The topology with ineligible edges removed.
     */
    private Topology pruneTopology(Topology topo, Integer azBw, Integer zaBw, List<IntRange> vlans, List<UrnE> urns,
                                   List<ReservedBandwidthE> rsvBwList, List<ReservedVlanE> rsvVlanList){
        //Build map of URN name to UrnE
        Map<String, UrnE> urnMap = buildUrnMap(urns);

        // Build map of URN to resvBw
        Map<UrnE, List<ReservedBandwidthE>> resvBwMap = buildReservedBandwidthMap(rsvBwList);

        // Build map of URN to resvVlan
        Map<UrnE, List<ReservedVlanE>> resvVlanMap = buildReservedVlanMap(rsvVlanList);

        // Copy the original topology's layer and set of vertices.
        Topology pruned = new Topology();
        pruned.setLayer(topo.getLayer());
        pruned.setVertices(topo.getVertices());
        // Filter out edges from the topology that do not have sufficient bandwidth available on both terminating nodes.
        // Also filters out all edges where either terminating node is not present in the URN map.
        Set<TopoEdge> availableEdges = topo.getEdges().stream()
                .filter(e -> urnMap.get(e.getA().getUrn()) != null)
                .filter(e -> urnMap.get(e.getZ().getUrn()) != null)
                .filter(e -> bwAvailable(e, azBw, zaBw, urnMap, resvBwMap))
                .collect(Collectors.toSet());
        // If this is an MPLS topology, or there are no edges left, just take the bandwidth-pruned set of edges.
        // Otherwise, find all the remaining edges that can support the requested VLAN(s).
        if(pruned.getLayer() == Layer.MPLS || availableEdges.isEmpty()){
            pruned.setEdges(availableEdges);
        }else {
            pruned.setEdges(findEdgesWithAvailableVlans(availableEdges, urnMap, vlans, resvVlanMap));
        }
        return pruned;
    }

    /***
     * Build a mapping of URN names to UrnE objects.
     * @param urns - A list of URNs, used to create the name -> UrnE map.
     * @return A map of URN names to UrnE objects.
     */
    private Map<String,UrnE> buildUrnMap(List<UrnE> urns) {
        return urns.stream().collect(Collectors.toMap(UrnE::getUrn, urn -> urn));
    }

    /**
     * Get a list of all bandwidth reserved between a start and end date/time.
     * @param start - The start of the time range
     * @param end - The end of the time range
     * @return A list of reserved bandwidth
     */
    private List<ReservedBandwidthE> getReservedBandwidth(Date start, Date end){
        // Get all Reserved Bandwidth between start and end
        Optional<List<ReservedBandwidthE>> optResvBw = resvBwRepo.findOverlappingInterval(start.toInstant(), end.toInstant());
        return optResvBw.isPresent() ? optResvBw.get() : new ArrayList<>();
    }

    /**
     * Get a list of all VLAN IDs reserved between a start and end date/time.
     * @param start - The start of the time range
     * @param end - The end of the time range
     * @return A list of reserved VLAN IDs
     */
    private List<ReservedVlanE> getReservedVlans(Date start, Date end){
        //Get all Reserved VLan between start and end
        Optional<List<ReservedVlanE>> optResvVlan = resvVlanRepo.findOverlappingInterval(start.toInstant(), end.toInstant());
        return optResvVlan.isPresent() ? optResvVlan.get() : new ArrayList<>();
    }

    /**
     * Build a mapping of UrnE objects to ReservedBandwidthE objects.
     * @param rsvBwList A list of all bandwidth reserved.
     * @return A map of UrnE to ReservedBandwidthE objects
     */
    private Map<UrnE, List<ReservedBandwidthE>> buildReservedBandwidthMap(List<ReservedBandwidthE> rsvBwList) {
        Map<UrnE, List<ReservedBandwidthE>> map = new HashMap<>();
        for(ReservedBandwidthE resv : rsvBwList){
            UrnE resvUrn = resv.getUrn();
            if(!map.containsKey(resvUrn)){
                map.put(resvUrn, new ArrayList<>());
            }
            map.get(resvUrn).add(resv);
        }
        return map;
    }

    /**
     * Build a mapping of UrnE objects to ReservedVlanE objects.
     * @param rsvVlanList - A list of all reserved VLAN IDs
     * @return A map of UrnE to ReservedVlanE objects
     */
    private Map<UrnE, List<ReservedVlanE>> buildReservedVlanMap(List<ReservedVlanE> rsvVlanList) {

        Map<UrnE, List<ReservedVlanE>> map = new HashMap<>();
        for(ReservedVlanE resv : rsvVlanList){
            UrnE resvUrn = resv.getUrn();
            if(!map.containsKey(resvUrn)){
                map.put(resvUrn, new ArrayList<>());
            }
            map.get(resvUrn).add(resv);
        }
        return map;
    }


    /**
     * Evaluate an edge to determine if the nodes on either end of the edge support the requested
     * az and za bandwidths. An edge will only fail the test if one or both URNs (corresponding to the nodes):
     * (1) have valid reservable bandwidth fields, and (2) the URN(s) do not have sufficient reservable bandwidth
     * available in both the az and za directions (egress and ingress).
     * @param edge - The edge to be evaluated.
     * @param azBw - The requested bandwidth in one direction.
     * @param zaBw - The requested bandwidth in the other direction.
     * @param urnMap - Map of URN name to UrnE object.
     * @return True if there is sufficient reservable bandwidth, False otherwise.
     */
    private boolean bwAvailable(TopoEdge edge, Integer azBw, Integer zaBw, Map<String, UrnE> urnMap, Map<UrnE, List<ReservedBandwidthE>> resvBwMap){
        // Get the reservable bandwidth for the URN matching the node on the "a" side of the edge.
        ReservableBandwidthE aBandwidth = urnMap.get(edge.getA().getUrn()).getReservableBandwidth();
        // Get the reservable bandwidth for the URN matching the node on the "z" side of the edge.
        ReservableBandwidthE zBandwidth = urnMap.get(edge.getZ().getUrn()).getReservableBandwidth();


        // At least one of the two has reservable bandwidth, so check the valid nodes to determine
        // If they have sufficient bandwidth. In the case of a (device, port) edge, only the port must be checked.
        // If it is a (port, port) edge, then both must be checked.
        boolean aPasses = true;
        boolean zPasses = true;
        if(aBandwidth != null){
            // Get a map of the available Ingress/Egress bandwidth for URN a
            Map<String, Integer> aAvailBwMap = getBwAvailabilityForUrn(aBandwidth, resvBwMap);
            aPasses = aAvailBwMap.get("Egress") >= azBw && aAvailBwMap.get("Ingress") >= zaBw;
        }
        if(zBandwidth != null){
            // Get a map of the available Ingress/Egress bandwidth for URN z
            Map<String, Integer> zAvailBwMap = getBwAvailabilityForUrn(zBandwidth, resvBwMap);
            zPasses = zAvailBwMap.get("Ingress") >= azBw && zAvailBwMap.get("Egress") >= zaBw;
        }

        return aPasses && zPasses;


    }

    /**
     * Determine how much Ingress/Egress bandwidth is still available at a URN. If the list of reserved bandwidths
     * is empty, then all of the Reservable Bandwidth at that URN is available. Otherwise,
     * subtract the sum Ingress/Egress bandwidth from the maximum reservable bandwidth at that URN.
     * @param bandwidth - ReservableBandwidthE object, which contains the maximum Ingress/Egress bandwidth for a given URN
     * @param resvBwMap - A Mapping from a URN to a list of Reserved Bandwidths at that URN.
     * @return A map containing the net available ingress/egress bandwidth at a URN
     */
    private Map<String,Integer> getBwAvailabilityForUrn(ReservableBandwidthE bandwidth, Map<UrnE, List<ReservedBandwidthE>> resvBwMap) {
        Map<String, Integer> availBw = new HashMap<>();
        availBw.put("Ingress", bandwidth.getIngressBw());
        availBw.put("Egress", bandwidth.getEgressBw());
        if(resvBwMap.containsKey(bandwidth.getUrn())){
            List<ReservedBandwidthE> resvBwList = resvBwMap.get(bandwidth.getUrn());
            Integer sumIngress = 0;
            Integer sumEgress = 0;
            for(ReservedBandwidthE resv : resvBwList){
                sumIngress += resv.getInBandwidth();
                sumEgress += resv.getEgBandwidth();
            }
            availBw.put("Ingress", Math.max(bandwidth.getIngressBw() - sumIngress, 0));
            availBw.put("Egress", Math.max(bandwidth.getEgressBw() - sumEgress, 0));
        }
        return availBw;
    }

    /**
     * Return a pruned set of edges where the nodes on either end of the edge support at least one of the specified VLANs.
     * A map of URNs are used to retrive the reservable VLAN sets from nodes (where applicable). Builds a mapping from
     * each available VLAN id to the set of edges that support that ID. Using this mapping, the largest set of edges
     * that supports a requested VLAN id (or any VLAN id if none are specified) is returned.
     * @param availableEdges - The set of currently available edges, which will be pruned further using VLAN tags.
     * @param urnMap - Map of URN name to UrnE object.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @param resvVlanMap - Map of UrnE objects to a List<> of Reserved VLAN tags.
     * @return The input edges, pruned using the input set of VLAN tags.
     */
    private Set<TopoEdge> findEdgesWithAvailableVlans(Set<TopoEdge> availableEdges, Map<String, UrnE> urnMap,
                                                      List<IntRange> vlans, Map<UrnE, List<ReservedVlanE>> resvVlanMap) {
        // Find a set of matching edges for each available VLAN id
        Map<Integer, Set<TopoEdge>> edgesPerId = findEdgesPerVlanId(availableEdges, urnMap, resvVlanMap);
        // Get a set of the requested VLAN ids
        Set<Integer> idsInRanges = getIntegersFromRanges(vlans);
        // Find the largest set of TopoEdges that meet the request
        Set<TopoEdge> bestSet = new HashSet<>();
        for(Integer id : edgesPerId.keySet()){
            // Ignore the set of edges where both terminating nodes do not have reservable VLAN fields
            // Add them to the best set of edges after this loop
            if(id == -1){
                continue;
            }
            // If the currently considered ID matches the request (or there are no VLANs requested)
            // and the set of edges supporting this ID are larger than the current best
            // choose this set of edges
            if((idsInRanges.contains(id) || idsInRanges.isEmpty()) && edgesPerId.get(id).size() > bestSet.size()){
                bestSet = edgesPerId.get(id);
            }
        }
        // Add all edges where neither terminating node has reservable VLAN attributes
        bestSet.addAll(edgesPerId.get(-1));
        return bestSet;
    }

    /**
     * Return all of the VLAN ids contained within the list of VLAN ranges.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return The set of VLAN ids making up the input ranges.
     */
    private Set<Integer> getIntegersFromRanges(List<IntRange> vlans){
        return vlans
                .stream()
                .map(this::getSetOfNumbersInRange)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Traverse the set of edges, and create a map of VLAN ID to the edges where that ID is available
     * @param edges - The set of edges.
     * @param urnMap - Map of URN name to UrnE object.
     * @return A (possibly empty) set of VLAN tags that are available across every edge.
     */
    private Map<Integer, Set<TopoEdge>> findEdgesPerVlanId(Set<TopoEdge> edges, Map<String, UrnE> urnMap,
                                                           Map<UrnE, List<ReservedVlanE>> resvVlanMap){
        // Overlap is used to track all VLAN tags that are available across every edge.
        Map<Integer, Set<TopoEdge>> edgesPerId = new HashMap<>();
        edgesPerId.put(-1, new HashSet<>());
        for(TopoEdge edge : edges){
            // Overlap is used to track all VLAN tags that are available across both endpoints of an edge
            Set<Integer> overlap = new HashSet<>();
            // Get all possible VLAN ranges reservable at the a and z ends of the edge
            List<IntRange> aRanges = getVlanRangesFromUrn(urnMap, edge.getA().getUrn());
            List<IntRange> zRanges = getVlanRangesFromUrn(urnMap, edge.getZ().getUrn());



            // If neither edge has reservable VLAN fields, add the edge to the "-1" VLAN tag list.
            // These edges do not need to be pruned, and will be added at the end to the best set of edges
            if(aRanges.isEmpty() && zRanges.isEmpty()){
                edgesPerId.get(-1).add(edge);
            }
            // Otherwise, find the intersection between the VLAN ranges (if any), and add the edge to the list
            // matching each overlapping VLAN ID.
            else{
                // Find what VLAN ids are actually available at A and Z
                Set<Integer> aAvailableVlanIds = getAvailableVlanIds(urnMap, edge.getA().getUrn(), aRanges, resvVlanMap);
                Set<Integer> zAvailableVlanIds = getAvailableVlanIds(urnMap, edge.getZ().getUrn(), zRanges, resvVlanMap);
                // Find the intersection of those two set of VLAN ranges
                overlap = addToOverlap(overlap, aAvailableVlanIds);
                overlap = addToOverlap(overlap, zAvailableVlanIds);


                // For overlapping IDs, put that edge into the map
                for(Integer id: overlap){
                    if(!edgesPerId.containsKey(id)){
                        edgesPerId.put(id, new HashSet<>());
                    }
                    edgesPerId.get(id).add(edge);
                }
            }

        }
        return edgesPerId;
    }

    /**
     * Get all VLAN IDs available at a URN based on the possible reservable ranges and the currently reserved IDs.
     * @param urnMap - Mapping of URN string to UrnE objects
     * @param urn - The currently considered URN string
     * @param ranges - List of IntRanges, representing all ranges of VLAN IDs supported at this URN
     * @param resvVlanMap - A mapping representing the Reserved VLANs at a URN
     * @return Set of VLAN IDs that are both supported and not reserved at a URN
     */
    private Set<Integer> getAvailableVlanIds(Map<String, UrnE> urnMap, String urn, List<IntRange> ranges,
                                             Map<UrnE, List<ReservedVlanE>> resvVlanMap) {
        // Get the supported reservable VLAN IDs
        Set<Integer> reservableVlanIds = getIntegersFromRanges(ranges);

        UrnE aUrn = urnMap.get(urn);
        // If this URN is in the reserved VLAN map
        if(resvVlanMap.containsKey(aUrn)) {
            // Get the reserved VLAN IDs
            List<Integer> resvVlanIds = resvVlanMap
                    .get(aUrn)
                    .stream()
                    .map(ReservedVlanE::getVlan)
                    .collect(Collectors.toList());

            // Return the supported IDs that are not reserved
            return reservableVlanIds
                    .stream()
                    .filter(id -> !resvVlanIds.contains(id))
                    .collect(Collectors.toSet());
        }
        else{
            return reservableVlanIds;
        }
    }

    /**
     * Using the list of URNs and the URN string, find the matching UrnE object and retrieve all of its
     * reservable IntRanges.
     * @param urnMap - Map of URN name to UrnE object.
     * @param matchingUrn - String representation of the desired URN.
     * @return - All IntRanges supported at the URN.
     */
    private List<IntRange> getVlanRangesFromUrn(Map<String, UrnE> urnMap, String matchingUrn){
        if(urnMap.get(matchingUrn) == null || urnMap.get(matchingUrn).getReservableVlans() == null)
            return new ArrayList<>();
        return urnMap.get(matchingUrn)
                .getReservableVlans()
                .getVlanRanges()
                .stream()
                .map(IntRangeE::toDtoIntRange)
                .collect(Collectors.toList());
    }


    /**
     * Iterate through the set of overlapping VLAN tags, keep the elements in that set which are also
     * contained in every IntRange passed in.
     * @param overlap - The set of overlapping VLAN tags.
     * @param other - Another set of VLAN tags, to be overlapped with the current overlapping set.
     * @return The (possibly reduced) set of overlapping VLAN tags.
     */
    private Set<Integer> addToOverlap(Set<Integer> overlap, Set<Integer> other){
        // If there are no ranges available, just return the current overlap set
        if(other.isEmpty()){
            return overlap;
        }
        // If the overlap does not already have elements, add all of the tags retrieved from this range
        if(overlap.isEmpty()){
            overlap.addAll(other);
        }else{
            // Otherwise, find the intersection between the current overlap and the tags retrieved from this range
            overlap.retainAll(other);
        }
        return overlap;
    }

    /**
     * Retrieve a set of all Integers that fall within the specified IntRange.
     * @param aRange - The specified IntRange
     * @return The set of Integers contained within the range.
     */
    private Set<Integer> getSetOfNumbersInRange(IntRange aRange) {
        Set<Integer> numbers = new HashSet<>();
        for(Integer num = aRange.getFloor(); num <= aRange.getCeiling(); num++){
            numbers.add(num);
        }
        return numbers;
    }

    /**
     * Get the requested set of VLAN tags from a junction by streaming through the fixtures in the junction.
     * @param junction - The requested VLAN junction.
     * @return The set of VLAN tags (Integers) requested for fixtures at that junction.
     */
    private List<IntRange> getVlansFromJunction(RequestedVlanJunctionE junction){
        // Stream through the junction's fixtures, map the requested VLAN expression to a set of Integers
        return junction.getFixtures().stream()
                .map(RequestedVlanFixtureE::getVlanExpression)
                .map(this::getIntRangesFromString)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Given a list of strings, convert all valid strings into IntRanges.
     * @param vlans - Requested VLAN ranges. Any VLAN ID within those ranges can be accepted.
     * @return A list of IntRanges, each representing a range of VLAN ID values parsed from a string.
     */
    private List<IntRange> getIntRangesFromString(String vlans){
        if(IntRangeParsing.isValidIntRangeInput(vlans)){
            try {
                return IntRangeParsing.retrieveIntRanges(vlans);
            }catch(Exception e){
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

}
