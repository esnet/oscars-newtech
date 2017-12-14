package net.es.oscars.pss.svc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.StartupComponent;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.app.props.TopoProperties;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.pop.ConsistencyException;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;


@Component
@Slf4j
@Data
public class PssHealthChecker implements StartupComponent {
    private PssProperties pssProperties;
    private TopoProperties topoProperties;
    private TopoService topoService;
    private DeviceRepository deviceRepo;

    private Set<Device> devicesToCheck = new HashSet<>();
    private Map<String, CommandStatus> statuses = new HashMap<>();

    @Autowired
    public PssHealthChecker(PssProperties pssProperties,
                            DeviceRepository deviceRepo,
                            TopoService topoService, TopoProperties topoProperties) {
        this.pssProperties = pssProperties;
        this.topoProperties = topoProperties;
        this.topoService = topoService;
        this.deviceRepo = deviceRepo;
    }

    public void startup() throws StartupException {

        if (!pssProperties.getControlPlaneCheckOnStart()) {
            log.info("skipping control plane check");
            return;
        }


        Integer randomNumToCheck = pssProperties.getControlPlaneCheckRandom();

        try {
            Optional<Version> maybeCurrent = topoService.currentVersion();
            if (maybeCurrent.isPresent()) {
                Version current = maybeCurrent.get();
                List<Device> devices = deviceRepo.findByVersion(current);
                if (randomNumToCheck <= 0) {
                    // do not perform a random check, get from json
                    String checkFilename = "./config/topo/" + topoProperties.getPrefix() + "-check.json";
                    log.info("control plane check from file "+checkFilename);

                    File jsonFile = new File(checkFilename);
                    ObjectMapper mapper = new ObjectMapper();
                    List<String> fromFile = Arrays.asList(mapper.readValue(jsonFile, String[].class));
                    for (String deviceUrn : fromFile) {
                        if (!topoService.getTopoUrnMap().containsKey(deviceUrn)) {
                            throw new StartupException("invalid entry in check file: "+deviceUrn);
                        }
                        TopoUrn urn = topoService.getTopoUrnMap().get(deviceUrn);
                        if (!urn.getUrnType().equals(UrnType.DEVICE)) {
                            throw new StartupException("entry in check file: "+deviceUrn+ " is "+urn.getUrnType()+", should be device");
                        }
                        devicesToCheck.add(urn.getDevice());
                    }

                } else if (randomNumToCheck > devices.size()) {
                    log.error("asked to check "+randomNumToCheck+" devices but only "+devices.size()+" exist; checking all ");
                    devicesToCheck.addAll(devices);
                } else {
                    log.info("adding "+randomNumToCheck+" random devices");
                    Random r = new Random();

                    while (devicesToCheck.size() < randomNumToCheck) {
                        int idx = r.nextInt(devices.size());
                        Device d = devices.get(idx);
                        if (!devicesToCheck.contains(d)) {
                            devicesToCheck.add(d);
                            log.debug("will check "+d.getUrn());
                        }
                    }
                }


            } else {
                throw new StartupException("could not get topology!");
            }


        }catch (ConsistencyException | IOException ex) {
            throw new StartupException("PSS health check failed! " + ex.getMessage());
        }

    }
}