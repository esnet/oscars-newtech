package net.es.oscars.topo.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.StartupComponent;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.app.props.TopoProperties;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.db.PortRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.enums.Layer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;


@Slf4j
@Service
public class TopoPopulator implements StartupComponent {
    private DeviceRepository deviceRepo;
    private PortRepository portRepo;
    private TopoProperties topoProperties;
    private PortAdjcyRepository adjcyRepo;


    @Autowired
    public TopoPopulator(DeviceRepository deviceRepo,
                         PortRepository portRepo,
                         PortAdjcyRepository adjcyRepo,
                         TopoProperties topoProperties) {
        this.deviceRepo = deviceRepo;
        this.portRepo = portRepo;
        this.adjcyRepo = adjcyRepo;
        this.topoProperties = topoProperties;
    }

    public void startup() throws StartupException {
        if (topoProperties == null) {
            throw new StartupException("No topo stanza in application properties");
        }
        String devicesFilename = "./config/topo/" + topoProperties.getPrefix() + "-devices.json";
        String adjciesFilename = "./config/topo/" + topoProperties.getPrefix() + "-adjcies.json";

        try {
            this.importDevices(false, devicesFilename);
            this.importAdjacencies(false, adjciesFilename);
        } catch (IOException ex) {
            throw new StartupException("Import failed! " + ex.getMessage());
        }
    }

    @Transactional
    public void importDevices(boolean overwrite, String devicesFilename) throws IOException {

        if (overwrite) {
            log.info("Overwrite set; deleting device entries.");
            deviceRepo.deleteAll();
        }

        List<Device> fileDevices = importDevicesFromFile(devicesFilename);


//        ObjectMapper mapper = new ObjectMapper();
//        log.info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fileDevices));

        if (deviceRepo.count() == 0) {

            deviceRepo.save(fileDevices);
            int ports = 0;
            for (Device dev : fileDevices) {
                ports += dev.getPorts().size();
            }
        } else {
            log.info("Devices DB is not empty; skipping import");
        }
    }

    @Transactional
    public void importAdjacencies(boolean overwrite, String adjciesFilename) throws IOException {
        if (overwrite) {
            adjcyRepo.deleteAll();
        }

        List<PortAdjcy> adjcies = importPortAdjciesFromFile(adjciesFilename);

        if (adjcyRepo.count() == 0) {
            adjcyRepo.save(adjcies);
        }

    }


    private List<Device> importDevicesFromFile(String filename) throws IOException {
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

    private List<PortAdjcy> importPortAdjciesFromFile(String filename) throws IOException {
        File jsonFile = new File(filename);
        ObjectMapper mapper = new ObjectMapper();
        List<PortAdjcyForImport> fromFile = Arrays.asList(mapper.readValue(jsonFile, PortAdjcyForImport[].class));
        List<PortAdjcy> result = new ArrayList<>();
        fromFile.forEach(t -> {
            Optional<Port> maybeA = portRepo.findByUrn(t.getA());
            Optional<Port> maybeZ = portRepo.findByUrn(t.getZ());
            if (maybeA.isPresent() && maybeZ.isPresent()) {
                Port a = maybeA.get();
                Port z = maybeZ.get();
                Map<Layer, Long> metrics = t.getMetrics();
                PortAdjcy adjcy = PortAdjcy.builder().a(a).z(z).metrics(metrics).build();
                result.add(adjcy);
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
