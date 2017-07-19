package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.rsrc.ReservableBandwidth;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.viz.Position;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.pop.UIPopulator;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TopologyProvider {
    private TopoService topoService;
    private UIPopulator uiPopulator;

    @Autowired
    public TopologyProvider(TopoService topoService, UIPopulator uiPopulator) {
        this.topoService = topoService;
        this.uiPopulator = uiPopulator;
    }

    public Topology getTopology() {
        return topoService.getMultilayerTopology();
    }

    public Map<String, Set<String>> devicePortMap() {
        return topoService.buildDeviceToPortMap().getMap();
    }

    // Reverse of devicePortMap: Key = port, Value = corresponding device
    public Map<String, String> portDeviceMap() {
        Map<String, String> p2d = new HashMap<>();
        Map<String, Set<String>> d2p = devicePortMap();

        for (String d : d2p.keySet()) {
            for (String p : d2p.get(d)) {
                p2d.put(p, d);
            }
        }

        return p2d;
    }

    public Map<String, Set<String>> getHubs() {
        Map<String, Set<String>> result = new HashMap<>();
        return result;
    }

    public Map<String, Position> getPositions() {
        if (uiPopulator.getStarted()) {
            return uiPopulator.getPositions().getPositions();
        }
        return new HashMap<>();
    }

    public List<ReservableBandwidth> getPortCapacities() {
        List<ReservableBandwidthE> portCapacity = topoService.reservableBandwidths();
        List<ReservableBandwidth> portCapDTO = new ArrayList<>();

        for (ReservableBandwidthE oneCap : portCapacity) {
            ReservableBandwidth oneDTO = ReservableBandwidth.builder()
                    .ingressBw(oneCap.getIngressBw())
                    .egressBw(oneCap.getEgressBw())
                    .topoVertexUrn(oneCap.getUrn().getUrn())
                    .build();

            portCapDTO.add(oneDTO);
        }

        return portCapDTO;
    }


    public Integer computeLinkCapacity(String portA, String portZ, List<ReservableBandwidth> portCapacities) {
        // Compute link capacities from port capacities //
        List<ReservableBandwidth> portCaps = portCapacities.stream()
                .filter(p -> p.getTopoVertexUrn().equals(portA) || p.getTopoVertexUrn().equals(portZ))
                .collect(Collectors.toList());

        assert (portCaps.size() == 2);

        ReservableBandwidth bw1 = portCaps.get(0);
        ReservableBandwidth bw2 = portCaps.get(1);
        Integer aCapIn;
        Integer aCapEg;
        Integer zCapIn;
        Integer zCapEg;

        Integer minCap;

        if (bw1.getTopoVertexUrn().equals(portA)) {
            aCapIn = bw1.getIngressBw();
            aCapEg = bw1.getEgressBw();
            zCapIn = bw2.getIngressBw();
            zCapEg = bw2.getEgressBw();
        } else {
            aCapIn = bw2.getIngressBw();
            aCapEg = bw2.getEgressBw();
            zCapIn = bw1.getIngressBw();
            zCapEg = bw1.getEgressBw();
        }

        Set<Integer> bwCapSet = new HashSet<>();
        bwCapSet.add(aCapIn);
        bwCapSet.add(aCapEg);
        bwCapSet.add(zCapIn);
        bwCapSet.add(zCapEg);

        minCap = bwCapSet.stream().min(Integer::compare).get();

        return minCap;
    }


}
