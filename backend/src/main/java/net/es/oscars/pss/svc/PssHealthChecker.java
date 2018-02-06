package net.es.oscars.pss.svc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.StartupComponent;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.app.props.TopoProperties;
import net.es.oscars.dto.pss.cmd.CommandStatus;
import net.es.oscars.dto.pss.cmd.VerifyRequest;
import net.es.oscars.dto.pss.st.LifecycleStatus;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.pop.ConsistencyException;
import net.es.oscars.topo.svc.TopoService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
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
    private Set<Device> devicesToVerify = new HashSet<>();

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


    public void checkControlPlane(String deviceUrn) throws PSSException {
        if (!topoService.getTopoUrnMap().containsKey(deviceUrn)) {
            throw new PSSException("could not find device! " + deviceUrn);
        }
        TopoUrn urn = topoService.getTopoUrnMap().get(deviceUrn);
        if (!urn.getUrnType().equals(UrnType.DEVICE)) {
            throw new PSSException("urn : " + deviceUrn + " is " + urn.getUrnType() + ", should be device");
        }

        boolean shouldCheck = false;

        if (statuses.containsKey(deviceUrn)) {
            CommandStatus cs = statuses.get(deviceUrn);
            if (cs.getLifecycleStatus().equals(LifecycleStatus.DONE)) {
                // if we have a complete record, if it's younger than 30 sec use that one
                Instant now = Instant.now();
                Instant lastUpdated = Instant.ofEpochMilli(cs.getLastUpdated().getTime());
                if (!lastUpdated.plusSeconds(30).isAfter(now)) {
                    log.debug("stale CP check for " + deviceUrn);
                    shouldCheck = true;
                }
            }
        } else {
            log.debug("no checks yet for " + deviceUrn);
            shouldCheck = true;
        }
        if (shouldCheck) {
            log.debug("will need to check " + deviceUrn);
            this.devicesToCheck.add(urn.getDevice());
        }
    }

    public VerifyRequest verifyDeviceFacts(Device d) throws PSSException {
        List<String> mbp = new ArrayList<>();
        List<String> mba = new ArrayList<>();
        Map<String, String> mhv = new HashMap<>();

        switch (d.getModel()) {
            case JUNIPER_MX:
                for (Port p : d.getPorts()) {
                    String portName = p.getUrn().split(":")[1];
                    // internal ports: must exist in interfaces, must have the IP address we expect configured on some unit
                    // TODO: what if it's not a /30?
                    if (p.getCapabilities().contains(Layer.MPLS)) {
                        mhv.put("$.interfaces.interface[?(@.name=='" + portName + "')]..inet.address.name", p.getIpv4Address()+"/30");
                    }
                }


                break;
            case ALCATEL_SR7750:
                break;
            default:
                throw new PSSException("Unsupported model");
        }
        return VerifyRequest.builder()
                .device(d.getUrn())
                .model(d.getModel())
                .mustBeAbsent(mba)
                .mustBePresent(mbp)
                .mustContainValue(mhv)
                .build();
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
                    log.info("control plane check from file " + checkFilename);

                    File jsonFile = new File(checkFilename);
                    ObjectMapper mapper = new ObjectMapper();
                    List<String> fromFile = Arrays.asList(mapper.readValue(jsonFile, String[].class));
                    for (String deviceUrn : fromFile) {
                        if (!topoService.getTopoUrnMap().containsKey(deviceUrn)) {
                            throw new StartupException("invalid entry in check file: " + deviceUrn);
                        }
                        TopoUrn urn = topoService.getTopoUrnMap().get(deviceUrn);
                        if (!urn.getUrnType().equals(UrnType.DEVICE)) {
                            throw new StartupException("entry in check file: " + deviceUrn + " is " + urn.getUrnType() + ", should be device");
                        }
                        devicesToCheck.add(urn.getDevice());
                        devicesToVerify.add(urn.getDevice());
                    }

                } else if (randomNumToCheck > devices.size()) {
                    log.error("asked to check " + randomNumToCheck + " devices but only " + devices.size() + " exist; checking all ");
                    devicesToCheck.addAll(devices);
                    devicesToVerify.addAll(devices);
                } else {
                    log.info("adding " + randomNumToCheck + " random devices");
                    Random r = new Random();

                    while (devicesToCheck.size() < randomNumToCheck) {
                        int idx = r.nextInt(devices.size());
                        Device d = devices.get(idx);
                        if (!devicesToCheck.contains(d)) {
                            devicesToCheck.add(d);
                            devicesToVerify.add(d);
                            log.debug("will check " + d.getUrn());
                        }
                    }
                }


            } else {
                throw new StartupException("could not get topology!");
            }


        } catch (ConsistencyException | IOException ex) {
            throw new StartupException("PSS health check failed! " + ex.getMessage());
        }

    }
}