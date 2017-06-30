package net.es.oscars.topo.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.DevicePortMap;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import net.es.oscars.dto.topo.enums.*;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.ent.ReservedBandwidthE;
import net.es.oscars.topo.dao.ReservableBandwidthRepository;
import net.es.oscars.topo.dao.ReservableVlanRepository;
import net.es.oscars.topo.dao.UrnAdjcyRepository;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.ReservableBandwidthE;
import net.es.oscars.topo.ent.ReservableVlanE;
import net.es.oscars.topo.ent.UrnAdjcyE;
import net.es.oscars.topo.ent.UrnE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Component
public class TopoService {
    private UrnAdjcyRepository adjcyRepo;

    private UrnRepository urnRepo;

    private ReservableVlanRepository vlanRepo;

    private ReservableBandwidthRepository bwRepo;

    private ReservedBandwidthRepository bwResRepo;

    public UrnE getUrn(String urn) throws NoSuchElementException {
        return urnRepo.findByUrn(urn).orElseThrow(NoSuchElementException::new);
    }


    @Autowired
    public TopoService(UrnAdjcyRepository adjcyRepo, UrnRepository urnRepo,
                       ReservableVlanRepository vlanRepo, ReservableBandwidthRepository bwRepo, ReservedBandwidthRepository bwResRepo) {
        this.adjcyRepo = adjcyRepo;
        this.urnRepo = urnRepo;
        this.vlanRepo = vlanRepo;
        this.bwRepo = bwRepo;
        this.bwResRepo = bwResRepo;
    }

    public Topology layer(Layer layer) throws NoSuchElementException {
        Topology topo = new Topology();
        topo.setLayer(layer);
        List<UrnE> urns = urnRepo.findAll();
        List<UrnAdjcyE> adjcies = adjcyRepo.findAll();

        urns.stream()
                .forEach(u ->
                {
                    Set<Layer> urnCapabilities = u.getCapabilities();
                    DeviceType urnDeviceType = u.getDeviceType();
                    IfceType urnInterfaceType = u.getIfceType();

                    VertexType vertType = null;
                    PortLayer portLayer = PortLayer.NONE;

                    if (urnDeviceType == null && urnInterfaceType != null) {
                        vertType = VertexType.PORT;

                        if (urnCapabilities.contains(Layer.MPLS)) {
                            portLayer = PortLayer.MPLS;
                        } else
                            portLayer = PortLayer.ETHERNET;
                    } else {
                        switch (urnDeviceType) {
                            case ROUTER:
                                vertType = VertexType.ROUTER;
                                break;
                            case SWITCH:
                                vertType = VertexType.SWITCH;
                                break;
                        }
                    }

                    if (urnCapabilities.contains(layer) || layer.equals(Layer.INTERNAL)) {
                        TopoVertex dev = TopoVertex.builder()
                                .portLayer(portLayer)
                                .vertexType(vertType)
                                .urn(u.getUrn())
                                .build();
                        topo.getVertices().add(dev);
                    }
                });

        adjcies.stream()
                .filter(adj -> adj.getMetrics().containsKey(layer))
                .forEach(adj -> {
                    if (adj.getA() == null || adj.getZ() == null) {
                        log.error("error in adjacency!");
                        log.error(adj.toString());
                    } else {
                        Long metric = adj.getMetrics().get(layer);
                        Optional<TopoVertex> a = topo.getVertexByUrn(adj.getA().getUrn());
                        Optional<TopoVertex> z = topo.getVertexByUrn(adj.getZ().getUrn());

                        if (a.isPresent() && z.isPresent()) {
                            TopoEdge edge = TopoEdge.builder()
                                    .a(a.get())
                                    .z(z.get())
                                    .metric(metric)
                                    .layer(layer)
                                    .build();
                            topo.getEdges().add(edge);
                        }
                    }
                });

        String pretty = null;
        try {
            pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(topo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        //log.info(pretty);     //commented for output readability

        return topo;
    }

    public Topology getMultilayerTopology() {
        Topology multiLayerTopo = new Topology();

        Topology ethTopo = layer(Layer.ETHERNET);
        Topology intTopo = layer(Layer.INTERNAL);
        Topology mplsTopo = layer(Layer.MPLS);

        multiLayerTopo.getVertices().addAll(ethTopo.getVertices());
        multiLayerTopo.getVertices().addAll(intTopo.getVertices());
        multiLayerTopo.getVertices().addAll(mplsTopo.getVertices());
        multiLayerTopo.getEdges().addAll(ethTopo.getEdges());
        multiLayerTopo.getEdges().addAll(intTopo.getEdges());
        multiLayerTopo.getEdges().addAll(mplsTopo.getEdges());

        return multiLayerTopo;
    }

    public Map<String, DeviceModel> deviceModels() {
        Map<String, DeviceModel> modelMap = new HashMap<>();
        urnRepo.findAll().stream().filter(u -> u.getUrnType().equals(UrnType.DEVICE)).forEach(u -> {
            modelMap.put(u.getUrn(), u.getDeviceModel());
        });

        return modelMap;

    }

    public UrnE device(String urn) throws NoSuchElementException {
        UrnE device = urnRepo.findByUrn(urn).orElseThrow(NoSuchElementException::new);

        if (!device.getUrnType().equals(UrnType.DEVICE)) {
            throw new NoSuchElementException();
        }
        return device;

    }

    public List<ReservableVlanE> reservableVlans() {
        return vlanRepo.findAll();

    }


    public List<ReservableBandwidthE> reservableBandwidths() {
        return bwRepo.findAll();

    }

    public List<String> edges(Layer layer) {
        log.info("finding edges for " + layer);

        return urnRepo.findAll().stream()
                .filter(u -> u.getCapabilities().contains(layer) && u.getUrnType().equals(UrnType.IFCE))
                .map(UrnE::getUrn)
                .collect(Collectors.toList());

    }

    public List<String> edgesWithCapability(String device, Layer layer) {
        log.info("finding edges with capability " + layer + " for device " + device);
        List<UrnAdjcyE> adjcies = adjcyRepo.findAll();

        return adjcies.stream()
                .filter(adj ->
                        adj.getMetrics().containsKey(Layer.INTERNAL)
                                && adj.getA().getUrn().equals(device)
                                && adj.getZ().getCapabilities().contains(layer))
                .map(adj -> adj.getZ().getUrn())
                .collect(Collectors.toList());

    }

    public List<String> devices() {
        log.info("retrieving all devices");

        return urnRepo.findAll().stream()
                .filter(u -> u.getUrnType().equals(UrnType.DEVICE))
                .map(UrnE::getUrn)
                .collect(Collectors.toList());
    }


    public VertexType getVertexTypeFromDeviceType(DeviceType deviceType) {
        if (deviceType.equals(DeviceType.SWITCH))
            return VertexType.SWITCH;
        else
            return VertexType.ROUTER;
    }

    public PortLayer lookupPortLayer(String portURN) {
        Optional<UrnE> thePortOpt = urnRepo.findAll().stream()
                .filter(p -> p.getUrn().equals(portURN))
                .findFirst();

        if (thePortOpt.isPresent()) {
            UrnE thePort = thePortOpt.get();

            if (thePort.getUrnType().equals(UrnType.DEVICE))
                return PortLayer.NONE;
            else if (thePort.getCapabilities().contains(Layer.MPLS))
                return PortLayer.MPLS;
            else
                return PortLayer.ETHERNET;
        }

        return null;
    }

    public DevicePortMap buildDeviceToPortMap() {
        Topology topo = getMultilayerTopology();
        Map<String, Set<String>> deviceToPortMap = new HashMap<>();
        Map<TopoVertex, Boolean> checkedMap = topo.getVertices().stream().collect(Collectors.toMap(v -> v, v -> false));
        for (TopoEdge edge : topo.getEdges()) {
            TopoVertex a = edge.getA();
            TopoVertex z = edge.getZ();
            // If either a or z is a device
            if (!a.getVertexType().equals(VertexType.PORT) || !z.getVertexType().equals(VertexType.PORT)) {
                // and if either a or z has not been checked
                if (!checkedMap.get(a) || !checkedMap.get(z)) {
                    // Add A (if it is a device) to the map if it has not been already
                    if (!a.getVertexType().equals(VertexType.PORT)) {
                        deviceToPortMap.putIfAbsent(a.getUrn(), new HashSet<>());
                        // If the other node (port) has not been checked, add it to the device's set of ports
                        if (!checkedMap.get(z) && z.getVertexType().equals(VertexType.PORT)) {
                            deviceToPortMap.get(a.getUrn()).add(z.getUrn());
                        }
                    }
                    // Add Z (if it is a device) to the map if it has not been already
                    if (!z.getVertexType().equals(VertexType.PORT)) {
                        deviceToPortMap.putIfAbsent(z.getUrn(), new HashSet<>());
                        // If the other node (port) has not been checked, add it to the device's set of ports
                        if (!checkedMap.get(a) && a.getVertexType().equals(VertexType.PORT)) {
                            deviceToPortMap.get(z.getUrn()).add(a.getUrn());
                        }
                    }
                    // Mark both as checked
                    checkedMap.put(a, true);
                    checkedMap.put(z, true);
                }
            }
        }
        return DevicePortMap.builder().map(deviceToPortMap).build();

    }

    public Map<String, String> buildPortToDeviceMap(Map<String, Set<String>> deviceToPortMap) {
        Map<String, String> portToDeviceMap = new HashMap<>();
        for (String device : deviceToPortMap.keySet()) {
            deviceToPortMap.get(device).forEach(port -> portToDeviceMap.put(port, device));
        }
        return portToDeviceMap;
    }

    public List<ReservedBandwidthE> reservedBandwidths() {
        return bwResRepo.findAll();
    }

    public boolean determineIfRouterHasEthernetPorts(String deviceURN) {
        List<UrnE> deviceList = urnRepo.findAll().stream().filter(u -> u.getUrn().equals(deviceURN)).collect(Collectors.toList());

        if (deviceList.isEmpty())
            return false;

        assert (deviceList.size() == 1);
        assert (deviceList.get(0).getDeviceType().equals(DeviceType.ROUTER));

        Map<String, Set<String>> dToPMap = this.buildDeviceToPortMap().getMap();
        Set<String> portsOnDevice = dToPMap.get(deviceURN);

        for (String onePort : portsOnDevice) {
            Optional<UrnE> portUrnOpt = urnRepo.findByUrn(onePort);
            assert (portUrnOpt.isPresent());

            UrnE portURN = portUrnOpt.get();

            if (!portURN.getCapabilities().contains(Layer.MPLS))
                return true;
        }

        return false;
    }

    public DevicePortMap deviceEdgePorts() {
        DevicePortMap dpm = this.buildDeviceToPortMap();
        DevicePortMap result = DevicePortMap.builder()
                .map(new HashMap<>())
                .build();
        dpm.getMap().entrySet().forEach((e) -> {
            String device = e.getKey();
            result.getMap().put(device, new HashSet<>());
            Set<String> ports = e.getValue();
            for (String port : ports) {
                PortLayer layer = lookupPortLayer(port);
                if (layer.equals(PortLayer.ETHERNET)) {
                    result.getMap().get(device).add(port);
                }
            }
        });
        return result;
    }

    public Set<String> identifyEdgePortURNs() {
        Topology fullTopo = getMultilayerTopology();
        Set<String> edgePortURNs = new HashSet<>();

        Set<TopoVertex> allPorts = fullTopo.getVertices().stream()
                .filter(v -> v.getVertexType().equals(VertexType.PORT))
                .collect(Collectors.toSet());

        Set<TopoEdge> allExternalLinks = fullTopo.getEdges().stream()
                .filter(l -> !l.getLayer().equals(Layer.INTERNAL))
                .collect(Collectors.toSet());

        for (TopoVertex onePort : allPorts) {
            String portURN = onePort.getUrn();
            boolean portNotOnAnyLink = true;

            for (TopoEdge oneExtAdjcy : allExternalLinks) {
                if (oneExtAdjcy.getA().equals(onePort) || oneExtAdjcy.getZ().equals(onePort))    // The port doesn't exist on an external (ETHERNET/MPLS) edge
                {
                    portNotOnAnyLink = false;
                    break;
                }
            }

            if (portNotOnAnyLink)
                edgePortURNs.add(portURN);
        }

        return edgePortURNs;
    }
}
