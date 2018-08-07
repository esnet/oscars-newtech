package net.es.oscars.topo.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.Delta;
import net.es.oscars.topo.beans.VersionDelta;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.db.PortRepository;
import net.es.oscars.topo.db.VersionRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.pop.ConsistencyException;
import org.opensaml.xml.signature.P;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;


@Service
@Slf4j
public class UpdateSvc {
    @Autowired
    private TopoService topoSvc;

    @Autowired
    private DeviceRepository deviceRepo;
    @Autowired
    private PortRepository portRepo;
    @Autowired
    private PortAdjcyRepository adjcyRepo;
    @Autowired
    private VersionRepository versionRepo;


    @Transactional
    public Version nextVersion() throws ConsistencyException {
        Version newVersion = Version.builder().updated(Instant.now()).valid(true).build();
        Optional<Version> maybeCurrent = topoSvc.currentVersion();

        if (maybeCurrent.isPresent()) {
            Version noLongerValid = maybeCurrent.get();
            noLongerValid.setValid(false);
            log.debug("Setting previous version to invalid: " + noLongerValid.getId());
            versionRepo.save(noLongerValid);
        }

        versionRepo.save(newVersion);
        versionRepo.flush();
        log.debug("New version id is: " + newVersion.getId());
        return newVersion;
    }

    public void mergeVersionDelta(VersionDelta vd, Version newVersion) throws ConsistencyException {
        log.info("Merging version delta");
        String pretty = null;
        try {
            pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(vd);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
//        log.info(pretty);

        this.mergeDevices(vd, newVersion);
        this.mergePorts(vd, newVersion);
        this.mergeAdjacencies(vd, newVersion);


        log.info("finished merging topology delta.");
    }

    @Transactional
    public void mergePorts(VersionDelta vd, Version newVersion) throws ConsistencyException {
        Delta<Port> pd = vd.getPortDelta();
        this.verifyPortDelta(pd);

        Map<String, Port> portsToMakeInvalid = new HashMap<>();
        Map<String, Port> portsToUpdateVersion = new HashMap<>();
        Map<String, Port> portsToInsert = new HashMap<>();
        Map<String, Port> portsToUpdate = new HashMap<>();
        Map<String, Port> portsUpdateTarget = new HashMap<>();

        for (Port p : pd.getAdded().values()) {
            // need to add the entry
            Optional<Port> maybeExists = portRepo.findByUrn(p.getUrn());
            if (!maybeExists.isPresent()) {
                portsToInsert.put(p.getUrn(), p);
            } else {
                Port prev = maybeExists.get();
                if (prev.getVersion().getValid()) {
                    throw new ConsistencyException("re-inserting an already valid port " + p.getUrn());
                } else {
                    // adding a previously invalid port: set to valid, update
                    portsToUpdateVersion.put(p.getUrn(), prev);
                    portsToUpdate.put(p.getUrn(), prev);
                    portsUpdateTarget.put(p.getUrn(), p);
                }
            }
        }


        for (Port p : pd.getRemoved().values()) {
            Optional<Port> maybeExists = portRepo.findByUrn(p.getUrn());
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("invalidating a missing port " + p.getUrn());
            } else {
                Port prev = maybeExists.get();
                portsToMakeInvalid.put(p.getUrn(), prev);
            }
        }

        for (Port next : pd.getModified().values()) {
            String urn = next.getUrn();
            Optional<Port> maybeExists = portRepo.findByUrn(urn);
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("modifying missing port " + urn);
            } else {
                Port prev = maybeExists.get();
                portsToUpdateVersion.put(urn, prev);
                portsToUpdate.put(urn, prev);
                portsUpdateTarget.put(urn, next);
            }
        }

        for (Port p : pd.getUnchanged().values()) {
            // need to add the entry
            Optional<Port> maybeExists = portRepo.findByUrn(p.getUrn());
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("bumping version for a missing port " + p.getUrn());
            } else {
                Port prev = maybeExists.get();
                portsToUpdateVersion.put(p.getUrn(), prev);
            }
        }


        int insertedPorts = 0;
        for (String urn : portsToInsert.keySet()) {
            Port p = portsToInsert.get(urn);
            p.setVersion(newVersion);
            String deviceUrn = p.getDevice().getUrn();
            Optional<Device> maybeDevice = deviceRepo.findByUrn(deviceUrn);
            if (!maybeDevice.isPresent()) {
                throw new ConsistencyException("insert port: points to unknown device: " + urn + " d: " + deviceUrn);
            }

            Device d = maybeDevice.get();
            if (!d.getVersion().getValid()) {
                throw new ConsistencyException("insert port: points to not-valid device: " + urn + " d: " + deviceUrn);
            }

            for (Port ep : d.getPorts()) {
                if (ep.getUrn().equals(urn)) {
                    throw new ConsistencyException("new port somehow already in device port set: " + urn);
                }
            }
            log.info("inserting p: " + p.getUrn() + " to " + deviceUrn);

            int prevPortsNum = d.getPorts().size();
            portRepo.save(p);
            d.getPorts().add(p);
            p.setDevice(d);
            deviceRepo.save(d);
            int nowPortsNum = d.getPorts().size();
            if (nowPortsNum - prevPortsNum != 1) {
                throw new ConsistencyException("device port size mismatch");
            }

            insertedPorts++;
        }

        int versionUpdatedPorts = 0;
        for (String urn : portsToUpdateVersion.keySet()) {
            Port prev = portsToUpdateVersion.get(urn);

            Pair<Port, Device> existing = this.getExistingPort(prev);
            Port port = existing.getKey();
            Device dev = existing.getValue();

            log.debug("updating version p: " + urn);
            port.setVersion(newVersion);
            port.setDevice(dev);
            portRepo.save(port);

            int prevPortsNum = dev.getPorts().size();
            deviceRepo.save(dev);
            int nowPortsNum = dev.getPorts().size();
            if (nowPortsNum != prevPortsNum) {
                throw new ConsistencyException("device port size mismatch");
            }
            versionUpdatedPorts++;
        }

        int dataUpdatedPorts = 0;
        for (String urn : portsToUpdate.keySet()) {
            log.debug("updating data p: " + urn);
            Port prev = portsToUpdate.get(urn);
            Port next = portsUpdateTarget.get(urn);

            Pair<Port, Device> existing = this.getExistingPort(prev);
            Port port = existing.getKey();
            Device dev = existing.getValue();

            port.setDevice(dev);

            if (next.getCapabilities() == null) {
                log.debug(urn + " <- caps (empty)");
                port.setCapabilities(new HashSet<>());
            } else {
                log.debug(urn + " <- caps "+next.getCapabilities().toString());
                port.setCapabilities(next.getCapabilities());
            }

            if (next.getIfce() == null) {
                log.debug(urn + " <- ifce (empty)");
                port.setIfce("");
            } else {
                log.debug(urn + " <- ifce "+next.getIfce());
                port.setIfce(next.getIfce());

            }

            if (next.getTags() == null) {
                log.debug(urn + " <- tags (empty)");
                port.setTags(new ArrayList<>());
            } else {
                log.debug(urn + " <- tags "+next.getTags().toString());
                port.setTags(next.getTags());
            }

            if (next.getIpv4Address() == null) {
                log.debug(urn + " <- ipv4 (empty)");
                port.setIpv4Address("");
            } else {
                log.debug(urn + " <- ipv4 "+next.getIpv4Address());
                port.setIpv4Address(next.getIpv4Address());

            }
            if (next.getIpv6Address() == null) {
                log.debug(urn + " <- ipv6 (empty)");
                port.setIpv6Address("");
            } else {
                log.debug(urn + " <- ipv6 "+next.getIpv4Address());
                port.setIpv6Address(next.getIpv6Address());

            }

            if (port.getReservableVlans() == null) {
                log.debug(urn + " <- vlans (empty)");
                port.setReservableVlans(new HashSet<>());
            } else {
                log.debug(urn + " <- vlans "+next.getReservableVlans().toString());
                port.setReservableVlans(next.getReservableVlans());
            }

            port.setReservableIngressBw(next.getReservableIngressBw());
            port.setReservableEgressBw(next.getReservableEgressBw());
            int prevPortsNum = dev.getPorts().size();
            portRepo.save(port);
            deviceRepo.save(dev);
            int nowPortsNum = dev.getPorts().size();
            if (nowPortsNum != prevPortsNum) {
                throw new ConsistencyException("internal error: device port size mismatch");
            }

            dataUpdatedPorts++;
        }

        int invalidatedPorts = 0;
        for (String urn : portsToMakeInvalid.keySet()) {
            Port prev = portsToMakeInvalid.get(urn);
            log.debug("invalidating port " + urn);
            Optional<Device> maybeDevice = deviceRepo.findByUrn(prev.getDevice().getUrn());
            if (!maybeDevice.isPresent()) {
                throw new ConsistencyException("invalidating a port pointing to an unknown device: " + urn);
            }
            invalidatedPorts++;
        }

        log.info("finished merging ports");
        log.info("   inserted     :   " + insertedPorts);
        log.info("   ver updated  :   " + versionUpdatedPorts);
        log.info("   data updated :   " + dataUpdatedPorts);
        log.info("   invalidated  :   " + invalidatedPorts);
        portRepo.flush();
        deviceRepo.flush();
    }

    public Pair<Port, Device> getExistingPort(Port port) throws ConsistencyException {
        String deviceUrn = port.getDevice().getUrn();
        String urn = port.getUrn();

        Optional<Device> maybeDevice = deviceRepo.findByUrn(deviceUrn);
        if (!maybeDevice.isPresent()) {
            throw new ConsistencyException("existing port: unknown device: " + urn + " " + deviceUrn);
        }
        Device dev = port.getDevice();
        if (!dev.getVersion().getValid()) {
            throw new ConsistencyException("existing port ver: belongs to a not-valid device: " + urn + " d: " + dev.getUrn());
        }

        Port resultPort = null;
        boolean foundInDevice = false;
        for (Port p : dev.getPorts()) {
            if (!p.getDevice().equals(dev)) {
                log.error("device id - found:     " + dev.toString());
                log.error("          - from port: " + p.getDevice().toString());
                throw new ConsistencyException("port device entity mismatch " + p.getUrn());
            }
            if (p.getUrn().equals(port.getUrn())) {
                if (!p.getId().equals(port.getId())) {
                    throw new ConsistencyException("port id mismatch in device port list " + urn);
                }
                foundInDevice = true;
                resultPort = p;
            }
        }
        if (!foundInDevice) {
            throw new ConsistencyException("data updated port not found in device " + urn);
        } else {
            return new Pair<>(resultPort, dev);
        }

    }


    @Transactional
    public void mergeDevices(VersionDelta vd, Version newVersion) throws ConsistencyException {

        Delta<Device> dd = vd.getDeviceDelta();
        this.verifyDeviceDelta(dd);

        Map<String, Device> devicesToMakeInvalid = new HashMap<>();
        Map<String, Device> devicesToUpdateVersion = new HashMap<>();
        Map<String, Device> devicesToInsert = new HashMap<>();
        Map<String, Device> devicesToUpdate = new HashMap<>();
        Map<String, Device> devicesUpdateTarget = new HashMap<>();

        for (Device d : dd.getAdded().values()) {
            // need to add the entry
            Optional<Device> maybeExists = deviceRepo.findByUrn(d.getUrn());
            if (!maybeExists.isPresent()) {
                devicesToInsert.put(d.getUrn(), d);
            } else {
                Device prev = maybeExists.get();
                if (prev.getVersion().getValid()) {
                    throw new ConsistencyException("re-inserting an already valid device " + d.getUrn());
                } else {
                    // adding a previously invalid device: set to valid, update
                    log.debug("will update device & set to valid: " + d.getUrn());
                    devicesToUpdateVersion.put(d.getUrn(), prev);
                    devicesToUpdate.put(d.getUrn(), prev);
                    devicesUpdateTarget.put(d.getUrn(), d);
                }
            }
        }

        for (Device d : dd.getRemoved().values()) {
            Optional<Device> maybeExists = deviceRepo.findByUrn(d.getUrn());
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("removing a missing device " + d.getUrn());
            } else {
                Device prev = maybeExists.get();
                log.info("will invalidate device (and all ports): " + d.getUrn());
                devicesToMakeInvalid.put(d.getUrn(), prev);
            }
        }
        for (Device d : dd.getModified().values()) {
            Optional<Device> maybeExists = deviceRepo.findByUrn(d.getUrn());
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("modifying a missing device " + d.getUrn());
            } else {
                log.debug("will modify device & update version: " + d.getUrn());
                Device prev = maybeExists.get();
                devicesToUpdateVersion.put(d.getUrn(), prev);
                devicesToUpdate.put(d.getUrn(), prev);
                devicesUpdateTarget.put(d.getUrn(), d);
            }
        }

        Integer unchangedDevices = 0;
        for (Device d : dd.getUnchanged().values()) {
            // need to add the entry
            Optional<Device> maybeExists = deviceRepo.findByUrn(d.getUrn());
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("bumping version for a missing device " + d.getUrn());
            } else {
                log.debug("will just update version: " + d.getUrn());
                Device prev = maybeExists.get();
                devicesToUpdateVersion.put(d.getUrn(), prev);
                unchangedDevices++;
            }
        }


        int insertedDevices = 0;
        for (String urn : devicesToInsert.keySet()) {
            Device d = devicesToInsert.get(urn);
            d.setVersion(newVersion);
            log.info("d: inserting: " + d.getUrn());
            deviceRepo.save(d);
            insertedDevices++;
        }

        int versionUpdatedDevices = 0;
        for (String urn : devicesToUpdateVersion.keySet()) {
            Device d = devicesToUpdateVersion.get(urn);
            log.debug("updating version d: " + d.getUrn());
            d.setVersion(newVersion);
            deviceRepo.save(d);
            versionUpdatedDevices++;
        }

        int dataUpdatedDevices = 0;
        for (String urn : devicesToUpdate.keySet()) {
            Device prev = devicesToUpdate.get(urn);
            Device next = devicesUpdateTarget.get(urn);
            log.debug("updating data d: " + urn);

            prev.setCapabilities(next.getCapabilities());
            prev.setIpv4Address(next.getIpv4Address());
            prev.setIpv6Address(next.getIpv6Address());
            prev.setModel(next.getModel());
            prev.setReservableVlans(next.getReservableVlans());
            prev.setType(next.getType());
            deviceRepo.save(prev);
            dataUpdatedDevices++;
        }

        int invalidatedDevices = 0;
        for (String urn : devicesToMakeInvalid.keySet()) {
            invalidatedDevices++;
            Device prev = devicesToMakeInvalid.get(urn);
            if (prev.getVersion().equals(newVersion)) {
                throw new ConsistencyException("Unable to set " + urn + " to invalid version!");
            }
            for (Port p : prev.getPorts()) {
                if (p.getVersion().equals(newVersion)) {
                    throw new ConsistencyException("Unable to set " + p.getUrn() + " to invalid version!");
                }
            }
        }

        log.info("finished merging devices");
        log.info("   inserted       : " + insertedDevices);
        log.info("   ver updated    : " + versionUpdatedDevices);
        log.info("    + unchanged      : " + unchangedDevices);
        log.info("    + data updated   : " + dataUpdatedDevices);
        log.info("   invalidated    :   " + invalidatedDevices);
        deviceRepo.flush();

    }

    @Transactional
    public void mergeAdjacencies(VersionDelta vd, Version newVersion) throws ConsistencyException {
        Delta<PortAdjcy> ad = vd.getAdjcyDelta();
        this.verifyAdjcyDelta(ad);


        Map<String, PortAdjcy> adjciesToMakeInvalid = new HashMap<>();
        Map<String, PortAdjcy> adjciesToUpdateVersion = new HashMap<>();
        Map<String, PortAdjcy> adjciesToInsert = new HashMap<>();
        Map<String, PortAdjcy> adjciesToUpdate = new HashMap<>();
        Map<String, PortAdjcy> adjciesUpdateTarget = new HashMap<>();


        for (PortAdjcy pa : ad.getAdded().values()) {
            String aUrn = pa.getA().getUrn();
            String zUrn = pa.getZ().getUrn();
            Optional<PortAdjcy> maybeExists = adjcyRepo.findByA_UrnAndZ_Urn(aUrn, zUrn);
            if (!maybeExists.isPresent()) {
                adjciesToInsert.put(pa.getUrn(), pa);
            } else {
                PortAdjcy prev = maybeExists.get();
                if (prev.getVersion().getValid()) {
                    throw new ConsistencyException("re-inserting already valid adjcy " + pa.getUrn());
                } else {
                    // adding a previously invalid adjcy: set to valid, update
                    adjciesToUpdateVersion.put(pa.getUrn(), prev);
                    adjciesToUpdate.put(pa.getUrn(), prev);
                    adjciesUpdateTarget.put(pa.getUrn(), pa);
                }
            }
        }


        for (PortAdjcy pa : ad.getRemoved().values()) {
            String aUrn = pa.getA().getUrn();
            String zUrn = pa.getZ().getUrn();
            Optional<PortAdjcy> maybeExists = adjcyRepo.findByA_UrnAndZ_Urn(aUrn, zUrn);
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("invalidating a missing adjcy " + pa.getUrn());
            } else {
                PortAdjcy prev = maybeExists.get();
                adjciesToMakeInvalid.put(pa.getUrn(), prev);
            }
        }

        for (PortAdjcy pa : ad.getModified().values()) {
            String aUrn = pa.getA().getUrn();
            String zUrn = pa.getZ().getUrn();
            Optional<PortAdjcy> maybeExists = adjcyRepo.findByA_UrnAndZ_Urn(aUrn, zUrn);
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("updating a missing adjcy " + pa.getUrn());
            } else {
                PortAdjcy prev = maybeExists.get();
                adjciesToUpdateVersion.put(pa.getUrn(), prev);
                adjciesToUpdate.put(pa.getUrn(), prev);
                adjciesUpdateTarget.put(pa.getUrn(), pa);
            }
        }

        for (PortAdjcy pa : ad.getUnchanged().values()) {
            String aUrn = pa.getA().getUrn();
            String zUrn = pa.getZ().getUrn();
            Optional<PortAdjcy> maybeExists = adjcyRepo.findByA_UrnAndZ_Urn(aUrn, zUrn);

            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("bumping version for missing adjcy " + pa.getUrn());
            } else {
                PortAdjcy prev = maybeExists.get();
                adjciesToUpdateVersion.put(pa.getUrn(), prev);
            }
        }

        int insertedAdjcies = 0;
        for (String urn : adjciesToInsert.keySet()) {
            log.info("inserting pa: " + urn);
            PortAdjcy pa = adjciesToInsert.get(urn);
            pa.setVersion(newVersion);
            String aUrn = pa.getA().getUrn();
            String zUrn = pa.getZ().getUrn();
            Optional<Port> maybeA = portRepo.findByUrn(aUrn);
            Optional<Port> maybeZ = portRepo.findByUrn(zUrn);
            if (!maybeA.isPresent()) {
                throw new ConsistencyException("new adjcy pointing to unknown a: " + urn);
            }
            if (!maybeZ.isPresent()) {
                throw new ConsistencyException("new adjcy pointing to unknown z: " + urn);
            }
            if (!maybeA.get().getVersion().getValid()) {
                throw new ConsistencyException("new adjcy pointing to invalid a: " + urn);
            }
            if (!maybeZ.get().getVersion().getValid()) {
                throw new ConsistencyException("new adjcy pointing to invalid z: " + urn);
            }
            pa.setA(maybeA.get());
            pa.setZ(maybeZ.get());
            adjcyRepo.save(pa);

            insertedAdjcies++;
        }

        int versionUpdatedAdjcies = 0;
        for (String urn : adjciesToUpdateVersion.keySet()) {
            PortAdjcy pa = adjciesToUpdateVersion.get(urn);
            if (!pa.getA().getVersion().getValid()) {
                throw new ConsistencyException("adjcy version update: invalid a: " + urn);
            }
            if (!pa.getZ().getVersion().getValid()) {
                throw new ConsistencyException("adjcy version update: invalid z: " + urn);
            }
            log.debug("updating version pa: " + urn);
            pa.setVersion(newVersion);
            adjcyRepo.save(pa);
            versionUpdatedAdjcies++;
        }


        int dataUpdatedAdjcies = 0;
        for (String urn : adjciesToUpdate.keySet()) {
            log.debug("updating data pa: " + urn);
            PortAdjcy prev = adjciesToUpdate.get(urn);
            PortAdjcy next = adjciesUpdateTarget.get(urn);

            if (!prev.getA().getUrn().equals(next.getA().getUrn())) {
                throw new ConsistencyException("port adjcy update : mismatch A: " + urn);
            }
            if (!prev.getZ().getUrn().equals(next.getZ().getUrn())) {
                throw new ConsistencyException("port adjcy update : mismatch Z: " + urn);
            }
            if (!prev.getA().getVersion().getValid()) {
                throw new ConsistencyException("port adjcy update : invalid A: " + urn);
            }
            if (!prev.getZ().getVersion().getValid()) {
                throw new ConsistencyException("port adjcy update : invalid Z: " + urn);
            }
            prev.getMetrics().clear();
            prev.getMetrics().putAll(next.getMetrics());
            adjcyRepo.save(prev);

            dataUpdatedAdjcies++;
        }


        int invalidatedAdjcies = 0;
        for (String urn : adjciesToMakeInvalid.keySet()) {
            invalidatedAdjcies++;
            PortAdjcy prev = adjciesToMakeInvalid.get(urn);
            if (prev.getVersion().getValid()) {
                throw new ConsistencyException("could not make invalid: " + urn);
            }
        }


        log.info("finished merging adjacencies");
        log.info("   inserted     :   " + insertedAdjcies);
        log.info("   ver updated  :   " + versionUpdatedAdjcies);
        log.info("   data updated :   " + dataUpdatedAdjcies);
        log.info("   invalidated  :   " + invalidatedAdjcies);
    }


    public void verifyDeviceDelta(Delta<Device> dd) throws ConsistencyException {

        // first check if any keys are overlapping
        Set<String> overlapping;
        overlapping = Sets.intersection(dd.addedUrns(), dd.modifiedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping device added / modified sets");
        }
        overlapping = Sets.intersection(dd.addedUrns(), dd.removedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping device added / removed sets");
        }
        overlapping = Sets.intersection(dd.addedUrns(), dd.unchangedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping device added / unchanged sets");
        }
        overlapping = Sets.intersection(dd.removedUrns(), dd.modifiedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping device removed / modified sets");
        }
        overlapping = Sets.intersection(dd.removedUrns(), dd.unchangedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping device removed / unchanged sets");
        }
        overlapping = Sets.intersection(dd.modifiedUrns(), dd.unchangedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping device modified / unchanged sets");
        }
        for (String urn : dd.addedUrns()) {
            Device d = dd.getAdded().get(urn);
            if (!d.getUrn().equals(urn)) {
                throw new ConsistencyException("key / urn mismatch");
            }
        }
        for (String urn : dd.modifiedUrns()) {
            Device d = dd.getModified().get(urn);
            if (!d.getUrn().equals(urn)) {
                throw new ConsistencyException("key / urn mismatch");
            }
        }
        for (String urn : dd.removedUrns()) {
            Device d = dd.getRemoved().get(urn);
            if (!d.getUrn().equals(urn)) {
                throw new ConsistencyException("key / urn mismatch");
            }
        }
        for (String urn : dd.unchangedUrns()) {
            Device d = dd.getUnchanged().get(urn);
            if (!d.getUrn().equals(urn)) {
                throw new ConsistencyException("key / urn mismatch");
            }
        }

    }

    public void verifyAdjcyDelta(Delta<PortAdjcy> pad) throws ConsistencyException {

        Set<String> overlapping;
        overlapping = Sets.intersection(pad.addedUrns(), pad.modifiedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping adjcy added / modified sets");
        }
        overlapping = Sets.intersection(pad.addedUrns(), pad.removedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping adjcy added / removed sets");
        }
        overlapping = Sets.intersection(pad.addedUrns(), pad.unchangedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping adjcy added / unchanged sets");
        }
        overlapping = Sets.intersection(pad.removedUrns(), pad.modifiedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping adjcy removed / modified sets");
        }
        overlapping = Sets.intersection(pad.removedUrns(), pad.unchangedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping adjcy removed / unchanged sets");
        }
        overlapping = Sets.intersection(pad.modifiedUrns(), pad.unchangedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping adjcy modified / unchanged sets");
        }

    }

    public void verifyPortDelta(Delta<Port> pd) throws ConsistencyException {

        Set<String> overlapping;
        overlapping = Sets.intersection(pd.addedUrns(), pd.modifiedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping port added / modified sets");
        }
        overlapping = Sets.intersection(pd.addedUrns(), pd.removedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping port added / removed sets");
        }
        overlapping = Sets.intersection(pd.addedUrns(), pd.unchangedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping port added / unchanged sets");
        }
        overlapping = Sets.intersection(pd.removedUrns(), pd.modifiedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping port removed / modified sets");
        }
        overlapping = Sets.intersection(pd.removedUrns(), pd.unchangedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping port removed / unchanged sets");
        }
        overlapping = Sets.intersection(pd.modifiedUrns(), pd.unchangedUrns());
        if (!overlapping.isEmpty()) {
            log.error(overlapping.toString());
            throw new ConsistencyException("Overlapping port modified / unchanged sets");
        }
        for (String urn : pd.addedUrns()) {
            Port p = pd.getAdded().get(urn);
            if (!p.getUrn().equals(urn)) {
                throw new ConsistencyException("key / urn mismatch");
            }
        }
        for (String urn : pd.modifiedUrns()) {
            Port p = pd.getModified().get(urn);
            if (!p.getUrn().equals(urn)) {
                throw new ConsistencyException("key / urn mismatch");
            }
        }
        for (String urn : pd.removedUrns()) {
            Port p = pd.getRemoved().get(urn);
            if (!p.getUrn().equals(urn)) {
                throw new ConsistencyException("key / urn mismatch");
            }
        }
        for (String urn : pd.unchangedUrns()) {
            Port p = pd.getUnchanged().get(urn);
            if (!p.getUrn().equals(urn)) {
                throw new ConsistencyException("key / urn mismatch");
            }
        }
    }
}
