package net.es.oscars.topo.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.StartupComponent;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.app.props.TopoProperties;
import net.es.oscars.topo.beans.Delta;
import net.es.oscars.topo.beans.TopoException;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.beans.VersionDelta;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.db.PortRepository;
import net.es.oscars.topo.db.VersionRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.svc.TopoLibrary;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;


@Slf4j
@Service
public class TopoPopulator implements StartupComponent {
    private TopoProperties topoProperties;
    private TopoService topoService;


    @Autowired
    public TopoPopulator(TopoService topoService,
                         TopoProperties topoProperties) {
        this.topoProperties = topoProperties;
        this.topoService = topoService;
    }

    @Transactional
    public void startup() throws StartupException {
        log.info("starting topo populator");
        if (topoProperties == null) {
            throw new StartupException("No topo stanza in application properties");
        }
        String devicesFilename = "./config/topo/" + topoProperties.getPrefix() + "-devices.json";
        String adjciesFilename = "./config/topo/" + topoProperties.getPrefix() + "-adjcies.json";

        try {
            if (!topoService.currentVersion().isPresent()) {
                log.info("first topology import");
                // no version, so make an empty first one
                topoService.nextVersion();
            }

            Topology current = topoService.currentTopology();
            log.info("previous topo: dev: "+current.getDevices().size()+" adj: "+current.getAdjcies().size());
            Topology incoming = this.loadTopology(devicesFilename, adjciesFilename);
            VersionDelta vd = TopoLibrary.compare(current, incoming);
            if (vd.isChanged()) {

                Version currentVersion = topoService.currentVersion().get();
                Version newVersion = topoService.nextVersion();
                log.info("found topology changes; new valid version will be: "+newVersion.getId());

                // TODO: check how the delta affects existing connections
                topoService.mergeVersionDelta(vd, currentVersion, newVersion);

            }
        } catch (IOException | ConsistencyException ex) {
            throw new StartupException("Import failed! " + ex.getMessage());
        }
        log.info("topo populator finished");
    }

    public Topology loadTopology(String devicesFilename, String adjciesFilename) throws IOException {

        List<Device> devices = loadDevicesFromFile(devicesFilename);
        Map<String, Port> portMap = new HashMap<>();
        devices.forEach(d -> {
            d.getPorts().forEach(p -> {
                portMap.put(p.getUrn(), p);
            });
        });

        List<PortAdjcy> adjcies = loadPortAdjciesFromFile(adjciesFilename, portMap);
        List<Port> ports = new ArrayList<>();
        devices.forEach(d -> {
            ports.addAll(d.getPorts());
        });

        return Topology.builder()
                .adjcies(adjcies)
                .devices(devices)
                .ports(ports)
                .build();

    }


    private List<Device> loadDevicesFromFile(String filename) throws IOException {
        File jsonFile = new File(filename);
        ObjectMapper mapper = new ObjectMapper();
        List<Device> devices = Arrays.asList(mapper.readValue(jsonFile, Device[].class));
        for (Device d : devices) {
            for (Port p : d.getPorts()) {
                p.setDevice(d);
            }
        }
        return devices;
    }

    private List<PortAdjcy> loadPortAdjciesFromFile(String filename, Map<String, Port> portMap) throws IOException {
        File jsonFile = new File(filename);
        ObjectMapper mapper = new ObjectMapper();
        List<PortAdjcyForImport> fromFile = Arrays.asList(mapper.readValue(jsonFile, PortAdjcyForImport[].class));
        List<PortAdjcy> result = new ArrayList<>();
        fromFile.forEach(t -> {
            if (portMap.containsKey(t.getA()) && portMap.containsKey(t.getZ())) {
                Port a = portMap.get(t.getA());
                Port z = portMap.get(t.getZ());
                Map<Layer, Long> metrics = t.getMetrics();
                PortAdjcy adjcy = PortAdjcy.builder().a(a).z(z).metrics(metrics).build();
                result.add(adjcy);
            } else {
                log.error("could not import adjcy: " + t.getA() + " -- " + t.getZ());
            }
        });
        return result;

    }


    @Data
    private static class PortAdjcyForImport {
        private String a;
        private String z;
        private Map<Layer, Long> metrics = new HashMap<>();
    }
}
