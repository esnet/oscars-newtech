package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.CoreUnitTestConfiguration;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.resv.ent.ReservedVlanE;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.ReservableVlanE;
import net.es.oscars.topo.enums.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.beans.TopoVertex;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.svc.TopoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.*;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(CoreUnitTestConfiguration.class)
public class PruningServiceTest {

    @Autowired
    private PruningService pruningService;

    @Autowired
    private TopoService topoService;


    @Test
    public void testBwPrune(){
        log.info("Pruning using only Bandwidth");
        Topology topo = buildTopology();
        log.info("Built Topology");
        log.info(topo.toString());
        List<UrnE> urns = buildUrnList();
        log.info("Built list of URNs");
        log.info(urns.toString());
        List<ReservedBandwidthE> rsvBwList = buildReservedBandwidthList(urns, 10);
        log.info("Built list of Reserved Bandwidth");
        log.info(rsvBwList.toString());
        List<ReservedVlanE> rsvVlanList = buildReservedVlanList(urns, Arrays.asList(1,2,3));
        log.info("Built list of Reserved VLANs");
        log.info(rsvVlanList.toString());

        log.info("Pruning - Remove no edges");
        Topology pruned = pruningService.pruneWithBw(topo, 100, urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().size() == topo.getEdges().size());

        log.info("Pruning - Remove every edge");
        pruned = pruningService.pruneWithBw(topo, 175, urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().isEmpty());

        log.info("Pruning - Remove only some edges");
        pruned = pruningService.pruneWithBw(topo, 140, urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().size() < topo.getEdges().size());
        assert(!pruned.getEdges().isEmpty());
        assert(!getEdgeByEndpoints(pruned, "swA", "swA:1").isPresent());
        assert(getEdgeByEndpoints(pruned, "swA", "swA:2").isPresent());
        assert(getEdgeByEndpoints(pruned, "swA:2", "swB:1").isPresent());
        assert(getEdgeByEndpoints(pruned, "swB:1", "swB").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swB", "swB:2").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swB:2", "swC:1").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swC:1", "swC").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swC", "swC:2").isPresent());
    }


    @Test
    public void testAZBwPrune(){
        log.info("Pruning using AZ & ZA Bandwidth");
        Topology topo = buildTopology();
        log.info("Built Topology");
        log.info(topo.toString());
        List<UrnE> urns = buildUrnList();
        log.info("Built list of URNs");
        log.info(urns.toString());
        List<ReservedBandwidthE> rsvBwList = buildReservedBandwidthList(urns, 10);
        log.info("Built list of Reserved Bandwidth");
        log.info(rsvBwList.toString());
        List<ReservedVlanE> rsvVlanList = buildReservedVlanList(urns, Arrays.asList(1,2,3));
        log.info("Built list of Reserved VLANs");
        log.info(rsvVlanList.toString());

        log.info("Pruning - Remove no edges");
        Topology pruned = pruningService.pruneWithAZBw(topo, 100, 115, urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().size() == topo.getEdges().size());

        log.info("Pruning - Remove every edge");
        pruned = pruningService.pruneWithAZBw(topo, 175, 100, urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().isEmpty());

        log.info("Pruning - Remove only some edges");
        pruned = pruningService.pruneWithAZBw(topo, 140, 115, urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().size() < topo.getEdges().size());
        assert(!pruned.getEdges().isEmpty());
        assert(getEdgeByEndpoints(pruned, "swA", "swA:1").isPresent());
        assert(getEdgeByEndpoints(pruned, "swA", "swA:2").isPresent());
        assert(getEdgeByEndpoints(pruned, "swA:2", "swB:1").isPresent());
        assert(getEdgeByEndpoints(pruned, "swB:1", "swB").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swB", "swB:2").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swB:2", "swC:1").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swC:1", "swC").isPresent());
        assert(getEdgeByEndpoints(pruned, "swC", "swC:2").isPresent());
    }

    @Test
    public void testBwVlanPrune(){
        log.info("Pruning using Bandwidth and VLANs");
        Topology topo = buildTopology();
        log.info("Built Topology");
        log.info(topo.toString());
        List<UrnE> urns = buildUrnList();
        log.info("Built list of URNs");
        log.info(urns.toString());
        List<ReservedBandwidthE> rsvBwList = buildReservedBandwidthList(urns, 10);
        log.info("Built list of Reserved Bandwidth");
        log.info(rsvBwList.toString());
        List<ReservedVlanE> rsvVlanList = buildReservedVlanList(urns, Arrays.asList(1,2,3));
        log.info("Built list of Reserved VLANs");
        log.info(rsvVlanList.toString());

        log.info("Pruning - Remove no edges");
        Topology pruned = pruningService.pruneWithBwVlans(topo, 100, "2:5", urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().size() == topo.getEdges().size());

        log.info("Pruning - Remove every edge");
        pruned = pruningService.pruneWithBwVlans(topo, 150, "90:120", urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().isEmpty());

        log.info("Pruning - Remove only some edges");
        pruned = pruningService.pruneWithBwVlans(topo, 115, "11:20,21:27,29", urns, rsvBwList, rsvVlanList);
        log.info(pruned.getEdges().toString());
        assert(pruned.getEdges().size() < topo.getEdges().size());
        assert(!pruned.getEdges().isEmpty());
        assert(!getEdgeByEndpoints(pruned, "swA", "swA:1").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swA", "swA:2").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swA:2", "swB:1").isPresent());
        assert(getEdgeByEndpoints(pruned, "swB:1", "swB").isPresent());
        assert(getEdgeByEndpoints(pruned, "swB", "swB:2").isPresent());
        assert(getEdgeByEndpoints(pruned, "swB:2", "swC:1").isPresent());
        assert(getEdgeByEndpoints(pruned, "swC:1", "swC").isPresent());
        assert(getEdgeByEndpoints(pruned, "swC", "swC:2").isPresent());
    }

    @Test
    public void testAZBwVlanPrune(){
        log.info("Pruning using AZ & ZA Bandwidth and VLANs");
        Topology topo = buildTopology();
        log.info("Built Topology");
        log.info(topo.toString());
        List<UrnE> urns = buildUrnList();
        log.info("Built list of URNs");
        log.info(urns.toString());
        List<ReservedBandwidthE> rsvBwList = buildReservedBandwidthList(urns, 10);
        log.info("Built list of Reserved Bandwidth");
        log.info(rsvBwList.toString());
        List<ReservedVlanE> rsvVlanList = buildReservedVlanList(urns, Arrays.asList(1,2,3));
        log.info("Built list of Reserved VLANs");
        log.info(rsvVlanList.toString());

        log.info("Pruning - Remove no edges");
        Topology pruned = pruningService.pruneWithAZBwVlans(topo, 100, 115, "3,4,10", urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().size() == topo.getEdges().size());

        log.info("Pruning - Remove every edge");
        pruned = pruningService.pruneWithAZBwVlans(topo, 175, 100, "80:90", urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().isEmpty());

        log.info("Pruning - Remove only some edges");
        pruned = pruningService.pruneWithAZBwVlans(topo, 100, 115, "40:45,50", urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().size() < topo.getEdges().size());
        assert(!pruned.getEdges().isEmpty());
        assert(!getEdgeByEndpoints(pruned, "swA", "swA:1").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swA", "swA:2").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swA:2", "swB:1").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swB:1", "swB").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swB", "swB:2").isPresent());
        assert(!getEdgeByEndpoints(pruned, "swB:2", "swC:1").isPresent());
        assert(getEdgeByEndpoints(pruned, "swC:1", "swC").isPresent());
        assert(getEdgeByEndpoints(pruned, "swC", "swC:2").isPresent());
    }

    @Test
    public void testBadInput(){
        log.info("Pruning using poorly formatted VLAN expressions");
        Topology topo = buildTopology();
        List<UrnE> urns = buildUrnList();
        List<ReservedBandwidthE> rsvBwList = buildReservedBandwidthList(urns, 10);
        List<ReservedVlanE> rsvVlanList = buildReservedVlanList(urns, Arrays.asList(1,2,3));

        log.info("VLAN expression: 9-10");
        Topology pruned = pruningService.pruneWithBwVlans(topo, 20, "9-10", urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().size() == topo.getEdges().size());

        log.info("VLAN expression: 20:3");
        pruned = pruningService.pruneWithBwVlans(topo, 20, "20:3", urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().size() == topo.getEdges().size());

        log.info("VLAN expression: jiofashjfiasf");
        pruned = pruningService.pruneWithBwVlans(topo, 20, "jiofashjfiasf", urns, rsvBwList, rsvVlanList);
        assert(pruned.getEdges().size() == topo.getEdges().size());

        log.info("Pruning using empty Reserved Bandwidth List");
        pruned = pruningService.pruneWithBwVlans(topo, 20, "1:10", urns, new ArrayList<ReservedBandwidthE>(), rsvVlanList);
        assert(pruned.getEdges().size() == topo.getEdges().size());

        log.info("Pruning using empty Reserved VLAN List");
        pruned = pruningService.pruneWithBwVlans(topo, 20, "1", urns, rsvBwList, new ArrayList<ReservedVlanE>());
        assert(pruned.getEdges().size() == topo.getEdges().size());

        log.info("Pruning using empty Reserved VLAN and Bandwidth List");
        pruned = pruningService.pruneWithBwVlans(topo, 20, "1", urns, new ArrayList<ReservedBandwidthE>(), new ArrayList<ReservedVlanE>());
        assert(pruned.getEdges().size() == topo.getEdges().size());

        log.info("Pruning using empty Reserved VLAN and Bandwidth List and incorrect expression");
        pruned = pruningService.pruneWithBwVlans(topo, 20, "~~~", urns, new ArrayList<ReservedBandwidthE>(), new ArrayList<ReservedVlanE>());
        assert(pruned.getEdges().size() == topo.getEdges().size());
    }


    private Topology buildTopology() {
        Topology topo = new Topology();
        topo.setLayer(Layer.ETHERNET);
        Set<TopoVertex> vertices = new HashSet<>();
        Set<TopoEdge> edges = new HashSet<>();
        TopoVertex switchA = new TopoVertex("swA", VertexType.SWITCH);
        TopoVertex switchB = new TopoVertex("swB", VertexType.SWITCH);
        TopoVertex switchC = new TopoVertex("swC", VertexType.SWITCH);

        TopoVertex switchAIn = new TopoVertex("swA:1", VertexType.PORT);
        TopoVertex switchAOut = new TopoVertex("swA:2", VertexType.PORT);
        TopoVertex switchBIn = new TopoVertex("swB:1", VertexType.PORT);
        TopoVertex switchBOut = new TopoVertex("swB:2", VertexType.PORT);
        TopoVertex switchCIn = new TopoVertex("swC:1", VertexType.PORT);
        TopoVertex switchCOut = new TopoVertex("swC:2", VertexType.PORT);

        // Directed edges
        TopoEdge edgeInt_Ain_A = new TopoEdge(switchAIn, switchA, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_A_Aout = new TopoEdge(switchA, switchAOut, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Aout_Bin = new TopoEdge(switchAOut, switchBIn, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_Bin_B = new TopoEdge(switchBIn, switchB, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_B_Bout = new TopoEdge(switchB, switchBOut, 0L, Layer.INTERNAL);

        TopoEdge edgeEth_Bout_Cin = new TopoEdge(switchBOut, switchCIn, 100L, Layer.ETHERNET);

        TopoEdge edgeInt_Cin_C = new TopoEdge(switchCIn, switchC, 0L, Layer.INTERNAL);
        TopoEdge edgeInt_C_Cout = new TopoEdge(switchC, switchCOut, 0L, Layer.INTERNAL);

        vertices.add(switchA);
        vertices.add(switchB);
        vertices.add(switchC);
        vertices.add(switchAIn);
        vertices.add(switchBIn);
        vertices.add(switchCIn);
        vertices.add(switchAOut);
        vertices.add(switchBOut);
        vertices.add(switchCOut);

        edges.add(edgeInt_Ain_A);
        edges.add(edgeInt_Bin_B);
        edges.add(edgeInt_Cin_C);
        edges.add(edgeInt_A_Aout);
        edges.add(edgeInt_B_Bout);
        edges.add(edgeInt_C_Cout);
        edges.add(edgeEth_Aout_Bin);
        edges.add(edgeEth_Bout_Cin);

        topo.setVertices(vertices);
        topo.setEdges(edges);
        return topo;
    }

    private List<UrnE> buildUrnList() {
        List<UrnE> urns = new ArrayList<>();

        UrnE swA = UrnE.builder()
                .deviceModel(DeviceModel.JUNIPER_EX)
                .capabilities(new HashSet<>())
                .deviceType(DeviceType.SWITCH)
                .urnType(UrnType.DEVICE)
                .urn("swA")
                .valid(true)
                .build();
        swA.getCapabilities().add(Layer.ETHERNET);

        UrnE swB = UrnE.builder()
                .deviceModel(DeviceModel.JUNIPER_EX)
                .capabilities(new HashSet<>())
                .deviceType(DeviceType.SWITCH)
                .urnType(UrnType.DEVICE)
                .urn("swB")
                .valid(true)
                .build();
        swB.getCapabilities().add(Layer.ETHERNET);

        UrnE swC = UrnE.builder()
                .deviceModel(DeviceModel.JUNIPER_EX)
                .capabilities(new HashSet<>())
                .deviceType(DeviceType.SWITCH)
                .urnType(UrnType.DEVICE)
                .urn("swC")
                .valid(true)
                .build();
        swC.getCapabilities().add(Layer.ETHERNET);


        UrnE swAIn = UrnE.builder()
                .capabilities(new HashSet<>())
                .urnType(UrnType.IFCE)
                .urn("swA:1")
                .valid(true)
                .build();
        swAIn.getCapabilities().add(Layer.ETHERNET);
        swAIn.setReservableBandwidth(createReservableBandwidth(swAIn, 125, 150));
        swAIn.setReservableVlans(createReservableVlans(swAIn, 1, 10));

        UrnE swAOut = UrnE.builder()
                .capabilities(new HashSet<>())
                .urnType(UrnType.IFCE)
                .urn("swA:2")
                .valid(true)
                .build();
        swAOut.getCapabilities().add(Layer.ETHERNET);
        swAOut.setReservableBandwidth(createReservableBandwidth(swAOut, 150, 150));
        swAOut.setReservableVlans(createReservableVlans(swAOut, 1, 10));

        UrnE swBIn = UrnE.builder()
                .capabilities(new HashSet<>())
                .urnType(UrnType.IFCE)
                .urn("swB:1")
                .valid(true)
                .build();
        swBIn.getCapabilities().add(Layer.ETHERNET);
        swBIn.setReservableBandwidth(createReservableBandwidth(swBIn, 150, 150));
        swBIn.setReservableVlans(createReservableVlans(swBIn, 1, 20));

        UrnE swBOut = UrnE.builder()
                .capabilities(new HashSet<>())
                .urnType(UrnType.IFCE)
                .urn("swB:2")
                .valid(true)
                .build();
        swBOut.getCapabilities().add(Layer.ETHERNET);
        swBOut.setReservableBandwidth(createReservableBandwidth(swBOut, 125, 150));
        swBOut.setReservableVlans(createReservableVlans(swBOut, 1, 20));

        UrnE swCIn = UrnE.builder()
                .capabilities(new HashSet<>())
                .urnType(UrnType.IFCE)
                .urn("swC:1")
                .valid(true)
                .build();
        swCIn.getCapabilities().add(Layer.ETHERNET);
        swCIn.setReservableBandwidth(createReservableBandwidth(swCIn, 125, 125));
        swCIn.setReservableVlans(createReservableVlans(swCIn, 1, 50));

        UrnE swCOut = UrnE.builder()
                .capabilities(new HashSet<>())
                .urnType(UrnType.IFCE)
                .urn("swC:2")
                .valid(true)
                .build();
        swCOut.getCapabilities().add(Layer.ETHERNET);
        swCOut.setReservableBandwidth(createReservableBandwidth(swCOut, 150, 125));
        swCOut.setReservableVlans(createReservableVlans(swCOut, 1, 50));

        urns.add(swA);
        urns.add(swAIn);
        urns.add(swAOut);
        urns.add(swB);
        urns.add(swBIn);
        urns.add(swBOut);
        urns.add(swC);
        urns.add(swCIn);
        urns.add(swCOut);
        return urns;
    }

    private Optional<TopoEdge> getEdgeByEndpoints(Topology topo, String urnA, String urnZ){
        return topo.getEdges().stream()
                .filter(e -> e.getA().getUrn().equals(urnA) && e.getZ().getUrn().equals(urnZ)
                        || e.getA().getUrn().equals(urnZ) && e.getZ().getUrn().equals(urnA))
                .findAny();
    }

    private ReservableVlanE createReservableVlans(UrnE urn, Integer floor, Integer ceiling){
        Set<IntRangeE> intRanges= new HashSet<>();
        intRanges.add(IntRangeE.builder().ceiling(ceiling).floor(floor).build());
        return ReservableVlanE.builder()
                .vlanRanges(intRanges)
                .urn(urn)
                .build();
    }

    private ReservableBandwidthE createReservableBandwidth(UrnE urn, Integer ingress, Integer egress){
        Integer max = Math.max(ingress, egress);
        return ReservableBandwidthE.builder()
                .bandwidth(max)
                .ingressBw(ingress)
                .egressBw(egress)
                .urn(urn)
                .build();
    }

    private List<ReservedBandwidthE> buildReservedBandwidthList(List<UrnE> urns, Integer bandwidth) {
        List<ReservedBandwidthE> reservedBandwidth = new ArrayList<>();

        for(UrnE urn : urns){
            if(urn.getUrnType() == UrnType.IFCE){
                ReservedBandwidthE rsvBw = ReservedBandwidthE.builder()
                        .urn(urn)
                        .bandwidth(bandwidth)
                        .inBandwidth(bandwidth)
                        .egBandwidth(bandwidth)
                        .beginning(Instant.MIN)
                        .ending(Instant.MAX)
                        .build();
                reservedBandwidth.add(rsvBw);
            }
        }
        return reservedBandwidth;
    }

    private List<ReservedVlanE> buildReservedVlanList(List<UrnE> urns, List<Integer> vlanIds) {
        List<ReservedVlanE> reservedVlans = new ArrayList<>();

        for(UrnE urn : urns){
            for(Integer vlanId : vlanIds){
                if(urn.getUrnType() == UrnType.IFCE){
                    ReservedVlanE rsvVlan = ReservedVlanE.builder()
                            .urn(urn)
                            .vlan(vlanId)
                            .beginning(Instant.MIN)
                            .ending(Instant.MAX)
                            .build();
                    reservedVlans.add(rsvVlan);
                }
            }
        }
        return reservedVlans;
    }
}
