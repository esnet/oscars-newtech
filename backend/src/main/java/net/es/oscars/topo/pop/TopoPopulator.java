package net.es.oscars.topo.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.props.TopoProperties;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.beans.VersionDelta;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.Adjcy;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.svc.TopoLibrary;
import net.es.oscars.topo.svc.TopoService;
import net.es.oscars.topo.svc.UpdateSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;


@Slf4j
@Service
public class TopoPopulator {
    private TopoProperties topoProperties;
    private TopoService topoService;
    private UpdateSvc updateSvc;


    @Autowired
    public TopoPopulator(TopoService topoService,
                         UpdateSvc updateSvc,
                         TopoProperties topoProperties) {
        this.topoProperties = topoProperties;
        this.updateSvc = updateSvc;
        this.topoService = topoService;
    }

    public VersionDelta refreshTopology() throws ConsistencyException, IOException {
        log.info("refreshing topology");
        if (topoProperties == null) {
            throw new ConsistencyException("Could not load topology properties!");
        }
        String devicesFilename = "./config/topo/" + topoProperties.getPrefix() + "-devices.json";
        String adjciesFilename = "./config/topo/" + topoProperties.getPrefix() + "-adjcies.json";

        if (!topoService.currentVersion().isPresent()) {
            log.info("No topology versions found; must be the very first topology load.");
            // no version, so make an empty first one
            updateSvc.nextVersion();
        }

        Topology current = topoService.currentTopology();
        log.debug("Existing topology: dev: " + current.getDevices().size() + " adj: " + current.getAdjcies().size());
        Topology incoming = this.loadTopology(devicesFilename, adjciesFilename);
        VersionDelta vd = TopoLibrary.compare(current, incoming);
        if (vd.isChanged()) {
            log.info("Found some topology changes; adding a new version.");
            Version newVersion = updateSvc.nextVersion();
            updateSvc.mergeVersionDelta(vd, newVersion);
        } else {
            log.debug("No topology changes.");
        }
        return vd;
    }

    public Topology loadTopology(String devicesFilename, String adjciesFilename) throws IOException {

        List<Device> devices = loadDevicesFromFile(devicesFilename);
        Map<String, Port> portMap = new HashMap<>();
        Map<String, Device> deviceMap = new HashMap<>();
        log.debug("Loaded topology from " + devicesFilename + " , " + adjciesFilename);
        devices.forEach(d -> {
            deviceMap.put(d.getUrn(), d);
            // log.info("  d: "+d.getUrn());
            d.getPorts().forEach(p -> {
                // log.info("  +- "+p.getUrn());
                portMap.put(p.getUrn(), p);
            });
        });

        List<Adjcy> adjcies = loadDbAdjciesFromFile(adjciesFilename, portMap);

        return Topology.builder()
                .adjcies(adjcies)
                .devices(deviceMap)
                .ports(portMap)
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

    private List<Adjcy> loadDbAdjciesFromFile(String filename, Map<String, Port> portMap) throws IOException {
        File jsonFile = new File(filename);
        ObjectMapper mapper = new ObjectMapper();
        List<Adjcy> fromFile = Arrays.asList(mapper.readValue(jsonFile, Adjcy[].class));

        List<Adjcy> filtered = new ArrayList<>();

        fromFile.forEach(t -> {
            String aPortUrn = t.getA().getPortUrn();
            String zPortUrn = t.getZ().getPortUrn();
            boolean add = true;
            if (!portMap.containsKey(aPortUrn)) {
                log.error("  " + aPortUrn + " not in topology");
                add = false;
            }
            if (!portMap.containsKey(zPortUrn)) {
                log.error("  " + zPortUrn + " not in topology");
                add = false;
            }
            if (add) {
                filtered.add(t);

            } else {
                log.error("Could not load an adjacency: " + t.getUrn());
            }
        });
        return filtered;

    }

}
