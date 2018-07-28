package net.es.oscars.topo.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.topo.beans.*;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.db.PortRepository;
import net.es.oscars.topo.db.VersionRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.enums.CommandParamType;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.pop.ConsistencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@Slf4j
@Data
public class TopoService {
    private Map<String, TopoUrn> topoUrnMap;
    private List<TopoAdjcy> topoAdjcies;

    @Autowired
    private DeviceRepository deviceRepo;
    @Autowired
    private PortRepository portRepo;
    @Autowired
    private PortAdjcyRepository adjcyRepo;
    @Autowired
    private VersionRepository versionRepo;

    @Autowired
    private PssProperties pssProperties;


    @Transactional
    public void updateTopo() throws ConsistencyException, TopoException {
        Optional<Version> maybeCurrent = currentVersion();

        if (maybeCurrent.isPresent()) {
            Version current = maybeCurrent.get();
            log.info("updating pathfinding topo to version: " + current.getId());
            List<Device> devices = deviceRepo.findByVersion(current);
            List<PortAdjcy> adjcies = adjcyRepo.findByVersion(current);

            // first add all devices (and ports) to the urn map
            this.topoUrnMap = this.urnsFromDevices(devices);

            // now process all adjacencies
            this.topoAdjcies = topoAdjciesFromDevices(devices);
            this.topoAdjcies.addAll(topoAdjciesFromPortAdjcies(adjcies));


            log.info("updated with " + this.topoAdjcies.size() + " topo adjacencies");
        } else {
            throw new TopoException("no valid topology version!");
        }

    }

    public Optional<Version> currentVersion() throws ConsistencyException {
        List<Version> valid = versionRepo.findByValid(true);
        if (valid.size() == 0) {
            return Optional.empty();
        } else if (valid.size() == 1) {
            return Optional.of(valid.get(0));
        } else {
            throw new ConsistencyException("more than 1 valid versions found");
        }
    }

    @Transactional
    public Version nextVersion() throws ConsistencyException {
        Version newVersion = Version.builder().updated(Instant.now()).valid(true).build();
        Optional<Version> maybeCurrent = currentVersion();

        if (maybeCurrent.isPresent()) {
            Version noLongerValid = maybeCurrent.get();
            noLongerValid.setValid(false);
            log.info("setting version to invalid: " + noLongerValid.getId());
            versionRepo.save(noLongerValid);
        }

        versionRepo.save(newVersion);
        versionRepo.flush();
        return newVersion;
    }

    @Transactional
    public void mergeVersionDelta(VersionDelta vd, Version currentVersion, Version newVersion) throws ConsistencyException {
        log.info("merging version delta, new version : " + newVersion.getId());
        String pretty = null;
        try {
            pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(vd);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
//        log.info(pretty);

        Delta<Device> dd = vd.getDeviceDelta();
        Delta<PortAdjcy> ad = vd.getAdjcyDelta();
        Delta<Port> pd = vd.getPortDelta();

        for (String urn : dd.getRemoved().keySet()) {
            if (dd.getUnchanged().keySet().contains(urn)) {
                throw new ConsistencyException("removed and unchanged delta for "+urn);
            }
            if (dd.getModified().keySet().contains(urn)) {
                throw new ConsistencyException("removed and modified delta for "+urn);
            }
            if (dd.getAdded().keySet().contains(urn)) {
                throw new ConsistencyException("removed and added delta for "+urn);
            }
        }

        for (String urn : dd.getModified().keySet()) {
            if (dd.getUnchanged().keySet().contains(urn)) {
                throw new ConsistencyException("modified and unchanged delta for "+urn);
            }
            if (dd.getAdded().keySet().contains(urn)) {
                throw new ConsistencyException("modified and added delta for "+urn);
            }
        }
        for (String urn : dd.getUnchanged().keySet()) {
            if (dd.getAdded().keySet().contains(urn)) {
                throw new ConsistencyException("unchanged and added delta for "+urn);
            }
        }

            Map<String, Device> devicesToMakeInvalid = new HashMap<>();
        Map<String, Device> devicesToUpdateVersion = new HashMap<>();
        Map<String, Device> devicesToAdd = new HashMap<>();
        Map<String, Device> devicesToUpdate = new HashMap<>();
        Map<String, Device> devicesUpdateTarget = new HashMap<>();

        for (Device d : dd.getAdded().values()) {
            // need to add the entry
            Optional<Device> maybeExists = deviceRepo.findByUrn(d.getUrn());
            if (!maybeExists.isPresent()) {
                devicesToAdd.put(d.getUrn(), d);
            } else {
                Device prev = maybeExists.get();
                if (prev.getVersion().getValid()) {
                    throw new ConsistencyException("trying to re-add already valid device "+d.getUrn());
                } else {
                    // adding a previously invalid device: set to valid, update
                    devicesToUpdateVersion.put(d.getUrn(), prev);
                    devicesToUpdate.put(d.getUrn(), prev);
                    devicesUpdateTarget.put(d.getUrn(), d);
                }
            }
        }

        for (Device d : dd.getRemoved().values()) {
            Optional<Device> maybeExists = deviceRepo.findByUrn(d.getUrn());
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("trying to remove missing device "+d.getUrn());
            } else {
                Device prev = maybeExists.get();
                devicesToMakeInvalid.put(d.getUrn(), prev);
            }
        }
        for (Device d : dd.getModified().values()) {
            Optional<Device> maybeExists = deviceRepo.findByUrn(d.getUrn());
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("trying to modify missing device "+d.getUrn());
            } else {
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
                throw new ConsistencyException("trying to keep unchanged missing device "+d.getUrn());
            } else {
                Device prev = maybeExists.get();
                devicesToUpdateVersion.put(d.getUrn(), prev);
                unchangedDevices++;
            }
        }




        Integer addedDevices = 0;
        for (String urn : devicesToAdd.keySet()) {
            Device d = devicesToAdd.get(urn);
            d.setVersion(newVersion);
            log.info("adding d: "+d.getUrn());
            deviceRepo.save(d);
            addedDevices++;
        }


        Integer versionUpdatedDevices = 0;
        for (String urn : devicesToUpdateVersion.keySet()) {
            Device d = devicesToUpdateVersion.get(urn);
            log.info("updating version d: "+d.getUrn());
            d.setVersion(newVersion);
            deviceRepo.save(d);
            versionUpdatedDevices++;
        }

        Integer dataUpdatedDevices = 0;
        for (String urn : devicesToUpdate.keySet()) {
            Device prev = devicesToUpdate.get(urn);
            Device next = devicesUpdateTarget.get(urn);
            log.info("updating data d: "+urn);

            prev.setCapabilities(next.getCapabilities());
            prev.setIpv4Address(next.getIpv4Address());
            prev.setIpv6Address(next.getIpv6Address());
            prev.setModel(next.getModel());
            prev.setReservableVlans(next.getReservableVlans());
            prev.setType(next.getType());
            deviceRepo.save(prev);
            dataUpdatedDevices++;
        }

        Integer invalidatedDevices = 0;
        for (String urn : devicesToMakeInvalid.keySet()) {
            invalidatedDevices++;
            Device prev = devicesToMakeInvalid.get(urn);
        }

        log.info("finished merging devices");
        log.info("   added          :   "+addedDevices);
        log.info("   ver updated    :   "+versionUpdatedDevices);
        log.info("    + unchanged     :   "+unchangedDevices);
        log.info("    + data updated  :   "+dataUpdatedDevices);
        log.info("   invalidated    :   "+invalidatedDevices);

        Map<String, Port> portsToMakeInvalid = new HashMap<>();
        Map<String, Port> portsToUpdateVersion = new HashMap<>();
        Map<String, Port> portsToAdd = new HashMap<>();
        Map<String, Port> portsToUpdate = new HashMap<>();
        Map<String, Port> portsUpdateTarget = new HashMap<>();


        for (Port p : pd.getAdded().values()) {
            // need to add the entry
            Optional<Port> maybeExists = portRepo.findByUrn(p.getUrn());
            if (!maybeExists.isPresent()) {
                portsToAdd.put(p.getUrn(), p);
            } else {
                Port prev = maybeExists.get();
                if (prev.getVersion().getValid()) {
                    throw new ConsistencyException("trying to re-add already valid port "+p.getUrn());
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
                throw new ConsistencyException("trying to remove missing port "+p.getUrn());
            } else {
                Port prev = maybeExists.get();
                portsToMakeInvalid.put(p.getUrn(), prev);
            }
        }

        for (Port p : pd.getModified().values()) {
            Optional<Port> maybeExists = portRepo.findByUrn(p.getUrn());
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("trying to modify missing port "+p.getUrn());
            } else {
                Port prev = maybeExists.get();
                portsToUpdateVersion.put(p.getUrn(), prev);
                portsToUpdate.put(p.getUrn(), prev);
                portsUpdateTarget.put(p.getUrn(), p);
            }
        }

        for (Port p : pd.getUnchanged().values()) {
            // need to add the entry
            Optional<Port> maybeExists = portRepo.findByUrn(p.getUrn());
            if (!maybeExists.isPresent()) {
                throw new ConsistencyException("trying to keep unchanged missing port "+p.getUrn());
            } else {
                Port prev = maybeExists.get();
                portsToUpdateVersion.put(p.getUrn(), prev);
            }
        }


        Integer addedPorts = 0;
        for (String urn : portsToAdd.keySet()) {
            Port p = portsToAdd.get(urn);
            p.setVersion(newVersion);
            String deviceUrn = p.getDevice().getUrn();
            Optional<Device> maybeDevice = deviceRepo.findByUrn(deviceUrn);
            if (!maybeDevice.isPresent()) {
                throw new ConsistencyException("new port pointing to unknown device: "+urn);
            }

            Device d = maybeDevice.get();
            for (Port ep : d.getPorts()) {
                if (ep.getUrn().equals(urn)) {
                    throw new ConsistencyException("new port already in device port set: "+urn);
                }
            }
            log.info("adding p: "+p.getUrn());
            d.getPorts().add(p);
            p.setDevice(d);
            deviceRepo.save(d);

            addedPorts++;
        }

        Integer versionUpdatedPorts = 0;
        for (String urn : portsToUpdateVersion.keySet()) {
            Port p = portsToUpdateVersion.get(urn);
            log.info("updating version p: "+urn);
            p.setVersion(newVersion);
            portRepo.save(p);
            versionUpdatedPorts++;
        }

        Integer dataUpdatedPorts = 0;
        for (String urn : portsToUpdate.keySet()) {
            log.info("updating data p: "+urn);
            Port prev = portsToUpdate.get(urn);
            Port next = portsUpdateTarget.get(urn);

            Optional<Device> maybeDevice = deviceRepo.findByUrn(prev.getDevice().getUrn());
            if (!maybeDevice.isPresent()) {
                throw new ConsistencyException("new port pointing to unknown device: "+urn);
            }
            Device dev = maybeDevice.get();

            if (!prev.getDevice().getUrn()
                    .equals(next.getDevice().getUrn())) {
                String msg = "attempting to update port to a different device "+urn+" ("+dev.getUrn()+")";
                log.error(msg);
                throw new ConsistencyException(msg);
            }

            boolean fromDev = false;
            for (Port p : dev.getPorts()) {
                if (p.getUrn().equals(urn)) {
                    fromDev = true;
                    prev = p;
                }
            }
            if (!fromDev) {
                throw new ConsistencyException("data updated port not found in device "+urn);
            }
            prev.setDevice(dev);
            prev.setCapabilities(next.getCapabilities());
            prev.setTags(next.getTags());
            prev.setIpv4Address(next.getIpv4Address());
            prev.setIpv6Address(next.getIpv6Address());
            prev.setReservableVlans(next.getReservableVlans());
            prev.setReservableIngressBw(next.getReservableIngressBw());
            prev.setReservableEgressBw(next.getReservableEgressBw());

            portRepo.save(prev);
            dataUpdatedPorts++;
        }

        Integer invalidatedPorts = 0;
        for (String urn : portsToMakeInvalid.keySet()) {
            Port prev = portsToMakeInvalid.get(urn);
            log.debug("invalidating port "+urn);
            Optional<Device> maybeDevice = deviceRepo.findByUrn(prev.getDevice().getUrn());
            if (!maybeDevice.isPresent()) {
                throw new ConsistencyException("new port pointing to unknown device: "+urn);
            }
            Device d = maybeDevice.get();

            prev.setDevice(d);
            portRepo.save(prev);
            invalidatedPorts++;
        }

        log.info("finished merging ports");
        log.info("   added        :   "+addedPorts);
        log.info("   ver updated  :   "+versionUpdatedPorts);
        log.info("   data updated :   "+dataUpdatedPorts);
        log.info("   invalidated  :   "+invalidatedPorts);



        Map<String, PortAdjcy> adjciesToMakeInvalid = new HashMap<>();
        Map<String, PortAdjcy> adjciesToUpdateVersion = new HashMap<>();
        Map<String, PortAdjcy> adjciesToAdd = new HashMap<>();
        Map<String, PortAdjcy> adjciesToUpdate = new HashMap<>();
        Map<String, PortAdjcy> adjciesUpdateTarget = new HashMap<>();



        for (PortAdjcy pa : ad.getAdded().values()) {
            String aUrn = pa.getA().getUrn();
            String zUrn = pa.getZ().getUrn();
            Optional<PortAdjcy> maybeExists = adjcyRepo.findByA_UrnAndZ_Urn(aUrn, zUrn);
            if (!maybeExists.isPresent()) {
                adjciesToAdd.put(pa.getUrn(), pa);
            } else {
                PortAdjcy prev = maybeExists.get();
                if (prev.getVersion().getValid()) {
                    throw new ConsistencyException("trying to re-add already valid adjcy "+pa.getUrn());
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
                throw new ConsistencyException("trying to remove missing adjcy "+pa.getUrn());
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
                throw new ConsistencyException("trying to modify missing adjcy "+pa.getUrn());
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
                throw new ConsistencyException("trying to keep unchanged missing adjcy "+pa.getUrn());
            } else {
                PortAdjcy prev = maybeExists.get();
                adjciesToUpdateVersion.put(pa.getUrn(), prev);
            }
        }

        Integer addedAdjcies = 0;
        for (String urn : adjciesToAdd.keySet()) {
            log.info("adding pa: "+urn);
            PortAdjcy pa = adjciesToAdd.get(urn);
            pa.setVersion(newVersion);
            String aUrn = pa.getA().getUrn();
            String zUrn = pa.getZ().getUrn();
            Optional<Port> maybeA = portRepo.findByUrn(aUrn);
            Optional<Port> maybeZ = portRepo.findByUrn(zUrn);
            if (!maybeA.isPresent()) {
                throw new ConsistencyException("new adjcy pointing to unknown a: "+urn);
            }
            if (!maybeZ.isPresent()) {
                throw new ConsistencyException("new adjcy pointing to unknown z: "+urn);
            }
            pa.setA(maybeA.get());
            pa.setZ(maybeZ.get());
            adjcyRepo.save(pa);

            addedAdjcies++;
        }

        Integer versionUpdatedAdjcies = 0;
        for (String urn : adjciesToUpdateVersion.keySet()) {
            PortAdjcy pa = adjciesToUpdateVersion.get(urn);
            log.info("updating version pa: "+urn);
            pa.setVersion(newVersion);
            adjcyRepo.save(pa);
            versionUpdatedAdjcies++;
        }


        Integer dataUpdatedAdjcies = 0;
        for (String urn : adjciesToUpdate.keySet()) {
            log.info("updating data pa: "+urn);
            PortAdjcy prev = adjciesToUpdate.get(urn);
            PortAdjcy next = adjciesUpdateTarget.get(urn);

            if (!prev.getA().getUrn().equals(next.getA().getUrn())) {
                throw new ConsistencyException("attempting to update port adjcy with different A: "+urn);
            }
            if (!prev.getZ().getUrn().equals(next.getZ().getUrn())) {
                throw new ConsistencyException("attempting to update port adjcy with different Z: "+urn);
            }
            prev.getMetrics().clear();
            prev.getMetrics().putAll(next.getMetrics());
            adjcyRepo.save(prev);

            dataUpdatedAdjcies++;
        }


        Integer invalidatedAdjcies = 0;
        for (String urn : adjciesToMakeInvalid.keySet()) {
            invalidatedAdjcies++;
            PortAdjcy prev = adjciesToMakeInvalid.get(urn);
        }


        log.info("finished merging adjacencies");
        log.info("   added        :   "+addedAdjcies);
        log.info("   ver updated  :   "+versionUpdatedAdjcies);
        log.info("   data updated :   "+dataUpdatedAdjcies);
        log.info("   invalidated  :   "+invalidatedAdjcies);

        log.info("finished merging topology delta.");

    }

    public Topology currentTopology() throws ConsistencyException {
        Map<String, Device> deviceMap = new HashMap<>();
        Map<String, Port> portMap = new HashMap<>();
        List<PortAdjcy> adjcies = new ArrayList<>();
        Topology t = Topology.builder()
                .adjcies(adjcies)
                .devices(deviceMap)
                .ports(portMap)
                .build();

        if (versionRepo.findAll().size() != 0) {
            List<Version> versions = versionRepo.findByValid(true);
            if (versions.size() != 1) {
                throw new ConsistencyException("exactly one valid version can exist");
            }
            List<Device> devices = deviceRepo.findByVersion(versions.get(0));
            devices.forEach(d -> {
                deviceMap.put(d.getUrn(), d);
            });

            adjcies = adjcyRepo.findByVersion(versions.get(0));
            log.info("found " + devices.size() + " devices in version " + versions.get(0).getId());
            log.info("found " + adjcies.size() + " adjcies in version " + versions.get(0).getId());
            t.setDevices(deviceMap);
            t.setAdjcies(adjcies);

//            log.info(" current topo:");
            devices.forEach(d -> {
//                log.info(" d: "+d.getUrn());
                for (Port p : d.getPorts()) {
                    if (p.getVersion() != null && p.getVersion().getValid()) {
//                        log.info(" +- "+p.getUrn());
                        portMap.put(p.getUrn(), p);
                    }
                }
            });
        }


        return t;
    }


    private Map<String, TopoUrn> urnsFromDevices(List<Device> devices) {
        Map<String, TopoUrn> urns = new HashMap<>();

        devices.forEach(d -> {
            if (d.getVersion().getValid()) {

                // make a copy of the IntRanges otherwise it'd be set by reference
                Set<IntRange> drv = new HashSet<>();
                drv.addAll(IntRange.mergeIntRanges(d.getReservableVlans()));
                Set<Layer> dCaps = new HashSet<>();
                dCaps.addAll(d.getCapabilities());

                TopoUrn deviceUrn = TopoUrn.builder()
                        .urn(d.getUrn())
                        .urnType(UrnType.DEVICE)
                        .device(d)
                        .reservableVlans(drv)
                        .capabilities(dCaps)
                        .reservableCommandParams(new HashSet<>())
                        .build();

                // for all devices, if this is MPLS-capable, reserve a VC id
                if (d.getCapabilities().contains(Layer.MPLS)) {
                    Set<IntRange> vcIdRanges = IntRange.fromExpression(pssProperties.getVcidRange());
                    ReservableCommandParam vcCp = ReservableCommandParam.builder()
                            .type(CommandParamType.VC_ID)
                            .reservableRanges(vcIdRanges)
                            .build();
                    deviceUrn.getReservableCommandParams().add(vcCp);
                }

                // for ALUs, add SVC, SDP and QOS ids as reservable
                if (d.getModel().equals(DeviceModel.ALCATEL_SR7750)) {

                    Set<IntRange> svcIdRanges = IntRange.fromExpression(pssProperties.getAluSvcidRange());
                    ReservableCommandParam aluSvcCp = ReservableCommandParam.builder()
                            .type(CommandParamType.ALU_SVC_ID)
                            .reservableRanges(svcIdRanges)
                            .build();
                    deviceUrn.getReservableCommandParams().add(aluSvcCp);


                    Set<IntRange> sdpIdRanges = IntRange.fromExpression(pssProperties.getAluSdpidRange());
                    ReservableCommandParam aluSdpCp = ReservableCommandParam.builder()
                            .type(CommandParamType.ALU_SDP_ID)
                            .reservableRanges(sdpIdRanges)
                            .build();
                    deviceUrn.getReservableCommandParams().add(aluSdpCp);

                    Set<IntRange> qosIdRanges = IntRange.fromExpression(pssProperties.getAluQosidRange());
                    ReservableCommandParam aluQosCp = ReservableCommandParam.builder()
                            .type(CommandParamType.ALU_QOS_POLICY_ID)
                            .reservableRanges(qosIdRanges)
                            .build();
                    deviceUrn.getReservableCommandParams().add(aluQosCp);
                }
                urns.put(d.getUrn(), deviceUrn);

                d.getPorts().forEach(p -> {
                    if (p.getVersion().getValid()) {
                        // make a copy of the IntRanges otherwise it'd be set by reference
                        Set<IntRange> prv = new HashSet<>();
                        prv.addAll(IntRange.mergeIntRanges(p.getReservableVlans()));
                        Set<Layer> pCaps = new HashSet<>();
                        pCaps.addAll(p.getCapabilities());

                        TopoUrn portUrn = TopoUrn.builder()
                                .urn(p.getUrn())
                                .urnType(UrnType.PORT)
                                .capabilities(pCaps)
                                .device(d)
                                .port(p)
                                .reservableIngressBw(p.getReservableIngressBw())
                                .reservableEgressBw(p.getReservableEgressBw())
                                .reservableVlans(prv)
                                .reservableCommandParams(new HashSet<>())
                                .build();


                        urns.put(p.getUrn(), portUrn);
                    } else {
                        log.info("not adding invalid port " + p.getUrn());
                    }
                });
            } else {
                log.info("not adding invalid device " + d.getUrn());

            }

        });

        return urns;

    }

    private List<TopoAdjcy> topoAdjciesFromPortAdjcies(List<PortAdjcy> portAdjcies) throws TopoException {
        List<TopoAdjcy> adjcies = new ArrayList<>();

        for (PortAdjcy pa : portAdjcies) {
            if (pa.getVersion() == null) {
                log.info("null port adjcy: " + pa.getUrn());
                continue;
            } else if (!pa.getVersion().getValid()) {
                log.info("invalid port adjcy: " + pa.getUrn());
                continue;
            }
            // all our adjcies should point to ports in the topoUrnMap

            if (!this.topoUrnMap.containsKey(pa.getA().getUrn())) {
                log.error(pa.getA().getUrn() + " -- " + pa.getZ().getUrn());

                throw new TopoException("missing A " + pa.getA().getUrn());

            } else if (!this.topoUrnMap.containsKey(pa.getZ().getUrn())) {

                log.error(pa.getA().getUrn() + " -- " + pa.getZ().getUrn());
                throw new TopoException("missing Z " + pa.getZ().getUrn());
            } else if (!pa.getA().getVersion().getValid()) {
                throw new TopoException("invalid A in adjcy" + pa.getUrn());
            } else if (!pa.getZ().getVersion().getValid()) {
                throw new TopoException("invalid Z in adjcy" + pa.getUrn());

            } else {

                TopoUrn aUrn = this.topoUrnMap.get(pa.getA().getUrn());
                TopoUrn zUrn = this.topoUrnMap.get(pa.getZ().getUrn());
                Map<Layer, Long> metrics = new HashMap<>();

                pa.getMetrics().entrySet().forEach(e -> {
                    metrics.put(e.getKey(), e.getValue());
                });

                TopoAdjcy adjcy = TopoAdjcy.builder().a(aUrn).z(zUrn).metrics(metrics).build();
                adjcies.add(adjcy);
            }

        }
        return adjcies;

    }

    private List<TopoAdjcy> topoAdjciesFromDevices(List<Device> devices) throws TopoException {
        List<TopoAdjcy> adjcies = new ArrayList<>();
        for (Device d : devices) {
            if (this.topoUrnMap.containsKey(d.getUrn())) {
                TopoUrn deviceUrn = this.topoUrnMap.get(d.getUrn());
                for (Port p : d.getPorts()) {
                    if (p.getVersion() == null || !p.getVersion().getValid()) {
                        continue;
                    }
                    if (this.topoUrnMap.containsKey(p.getUrn())) {
                        TopoUrn portUrn = this.topoUrnMap.get(p.getUrn());
                        TopoAdjcy az = TopoAdjcy.builder()
                                .a(deviceUrn)
                                .z(portUrn)
                                .metrics(new HashMap<>())
                                .build();
                        az.getMetrics().put(Layer.INTERNAL, 1L);
                        TopoAdjcy za = TopoAdjcy.builder()
                                .a(portUrn)
                                .z(deviceUrn)
                                .metrics(new HashMap<>())
                                .build();
                        za.getMetrics().put(Layer.INTERNAL, 1L);
                        adjcies.add(az);
                        adjcies.add(za);
                    } else {
                        throw new TopoException("missing a port urn "+p.getUrn());
                    }
                }
            } else {
                throw new TopoException("missing a device urn "+d.getUrn());
            }
        }

        return adjcies;
    }

}
