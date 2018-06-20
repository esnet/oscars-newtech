package net.es.oscars.topo.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.app.props.PssProperties;
import net.es.oscars.app.props.TopoProperties;
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

import javax.transaction.Transactional;
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


    public void updateTopo() throws ConsistencyException, TopoException {
        Optional<Version> maybeCurrent = currentVersion();

        if (maybeCurrent.isPresent()) {
            Version current = maybeCurrent.get();
            log.info("updating topo to version: "+current.getId());
            List<Device> devices = deviceRepo.findByVersion(current);
            List<PortAdjcy> adjcies = adjcyRepo.findByVersion(current);

            // first add all devices (and ports) to the urn map
            this.topoUrnMap = this.urnsFromDevices(devices);

            // now process all adjacencies
            this.topoAdjcies = topoAdjciesFromDevices(devices);
            this.topoAdjcies.addAll(topoAdjciesFromPortAdjcies(adjcies));



            log.info("updated with "+this.topoAdjcies.size()+" topo adjacencies");
        } else  {
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
            log.info("setting version to invalid: "+noLongerValid.getId());
            versionRepo.save(noLongerValid);
        }

        versionRepo.save(newVersion);
        return newVersion;
    }

    @Transactional
    public void mergeVersionDelta(VersionDelta vd, Version currentVersion, Version newVersion) throws ConsistencyException {
        log.info("merging version delta, new version : "+newVersion.getId());
        /*
        String pretty = null;
        try {
            pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(vd);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.info(pretty);
        */
        Delta<Device> dd = vd.getDeviceDelta();
        Delta<PortAdjcy> ad = vd.getAdjcyDelta();
        Delta<Port> pd = vd.getPortDelta();


        // to delete gone devices, we don't do anything; we just won't update their version

        int addedDevs = 0;
        // now add new devices
        for (Device d: dd.getAdded().values()) {
            // fist check if it exists in the repo at all
            //      (the delta is against the latest version only)
            if (deviceRepo.findByUrn(d.getUrn()).isPresent()) {
                // this means it's present in the db but its has an invalid version
                Version v = deviceRepo.findByUrn(d.getUrn()).get().getVersion();
                if (v.getValid()) {
                    throw new ConsistencyException("merge error: device in valid version but also present in delta");
                }
                dd.getModified().put(d.getUrn(), d);

            } else {
                // it was not present in the database; just add and save it
                // this will also
                d.setVersion(newVersion);
                deviceRepo.save(d);
                for (Port p : d.getPorts()) {
                    pd.getAdded().remove(p);
                }
            }
            addedDevs++;
        }
        log.debug("done adding new devices, added: "+addedDevs);

        int unchangedDevices = 0;
        // now handle unchanged devices; these too need to be set to latest version
        for (Device d: dd.getUnchanged().values()) {
            Optional<Device> maybeDev = deviceRepo.findByUrn(d.getUrn());
            if (maybeDev.isPresent()) {
                Device savedDevice = maybeDev.get();
                savedDevice.setVersion(newVersion);

            } else {
                throw new ConsistencyException("merge error: unchanged device in delta not found in repo");
            }
            unchangedDevices++;
        }
        log.debug("done updating version for unchanged devices: "+unchangedDevices+" entries");

        deviceRepo.flush();
        int modifiedDevs = 0;
        int modifiedPorts = 0;
        int addedPorts = 0;

        // then merge modified ones including the ports
        log.debug("now modifying devices");
        for (Device md: dd.getModified().values()) {
            log.debug(" modifying "+md.getUrn());
            Device prev = deviceRepo.findByUrn(md.getUrn()).orElseThrow(NoSuchElementException::new);
            prev.setCapabilities(md.getCapabilities());
            prev.setIpv4Address(md.getIpv4Address());
            prev.setIpv6Address(md.getIpv6Address());
            prev.setModel(md.getModel());
            prev.setReservableVlans(md.getReservableVlans());
            prev.setType(md.getType());
            prev.setVersion(newVersion);

            Set<String> handledPorts = new HashSet<>();

            for (Port ap: pd.getAdded().values()) {
                Optional<Port> maybeExists = portRepo.findByUrn(ap.getUrn());
                if (maybeExists.isPresent()) {
                    // it exists in the repo so instead of adding we need to modify it instead
                    pd.getModified().put(ap.getUrn(), ap);
                    ap.setVersion(newVersion);
                    handledPorts.add(ap.getUrn());
                } else {
                    if (ap.getDevice().getUrn().equals(prev.getUrn())) {
                        ap.setDevice(prev);
                        ap.setVersion(newVersion);
                        prev.getPorts().add(ap);
                        handledPorts.add(ap.getUrn());
                    }
                }
                addedPorts++;
            }
            for (String p : handledPorts) {
                pd.getAdded().remove(p);
            }
            handledPorts.clear();

            for (Port mp: pd.getModified().values()) {
                if (mp.getDevice().getUrn().equals(prev.getUrn())) {
                    boolean found = false;
                    Port prevPort = null;

                    for (Port pp : prev.getPorts()) {
                        if (pp.getUrn().equals(mp.getUrn())) {
                            found = true;
                            prevPort = pp;
                            break;
                        }
                    }

                    if (!found) {
                        throw new ConsistencyException("error locating modified port" + mp.getUrn());
                    }
                    prevPort.setVersion(newVersion);
                    prevPort.setCapabilities(mp.getCapabilities());
                    prevPort.setTags(mp.getTags());
                    prevPort.setDevice(prev);
                    prevPort.setIpv4Address(mp.getIpv4Address());
                    prevPort.setIpv6Address(mp.getIpv6Address());
                    prevPort.setReservableVlans(mp.getReservableVlans());
                    prevPort.setReservableIngressBw(mp.getReservableIngressBw());
                    prevPort.setReservableEgressBw(mp.getReservableEgressBw());
                    handledPorts.add(mp.getUrn());
                }
                modifiedPorts++;
            }
            for (String p : handledPorts) {
                pd.getModified().remove(p);
            }
            handledPorts.clear();

            modifiedDevs++;
            deviceRepo.save(prev);
        }
        deviceRepo.flush();
        portRepo.flush();
        log.debug("done modifying devices, modified: "+modifiedDevs);
        log.debug("                     invalidated: "+dd.getRemoved().values().size());
        log.debug("     + modified ports: "+modifiedPorts);
        log.debug("     + added ports: "+addedPorts);


        int unchangedPorts = 0;
        // now for unchanged ports; these too need to be set to latest version
        for (Port p: pd.getUnchanged().values()) {
            Optional<Port> maybePort = portRepo.findByUrn(p.getUrn());
            if (maybePort.isPresent()) {
                Port savedPort = maybePort.get();
                savedPort.setVersion(newVersion);

            } else {
                throw new ConsistencyException("merge error: unchanged port in delta not found in repo");
            }
            unchangedPorts++;
        }

        for (Port p : pd.getRemoved().values()) {
            log.info(" invalidating port "+p.getUrn()+" , version id stays: "+p.getVersion().getId());
        }
        portRepo.flush();
        log.debug("   unchanged ports: "+unchangedPorts+" entries");
        log.debug(" invalidated ports: "+pd.getRemoved().values().size());


        // to invalidate adjacencies that no longer exist, do nothing, just don't set the new version
        // now modify existing ones
        int modifiedAdjs = 0;
        for (PortAdjcy pa: ad.getModified().values()) {
            log.warn("modifying an adjcy: "+pa.getA().getUrn()+ " -- "+pa.getZ().getUrn());
            boolean found = false;
            PortAdjcy prev = null;
            for (PortAdjcy candidate : adjcyRepo.findByVersion(currentVersion)) {
                if (candidate.getA().getUrn().equals(pa.getA().getUrn()) &&
                        candidate.getZ().getUrn().equals(pa.getZ().getUrn())) {
                    prev = candidate;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ConsistencyException("adjcy in modified delta does not exist in repo");
            }
            for (Layer l : prev.getMetrics().keySet()) {
                if (!pa.getMetrics().keySet().contains(l)) {
                    log.debug("  removing a metric for "+l.toString());
                    prev.getMetrics().remove(l);
                }
            }
            for (Layer l : pa.getMetrics().keySet()) {
                Long newMetric = pa.getMetrics().get(l);
                if (!prev.getMetrics().containsKey(l)) {
                    log.debug("  adding a metric for "+l.toString() + " to "+newMetric);
                    prev.getMetrics().put(l, newMetric);

                } else {
                    Long prevMetric = prev.getMetrics().get(l);
                    if (!prevMetric.equals(newMetric)) {
                        log.debug("  replacing metric for "+l.toString() + " to "+newMetric);
                        prev.getMetrics().put(l, newMetric);
                    }
                }
            }

            prev.setVersion(newVersion);
            adjcyRepo.save(prev);

            modifiedAdjs ++;
        }

        adjcyRepo.flush();
        log.debug("done modifying adjacencies: modified: "+modifiedAdjs);
        log.debug("                        invalidated : "+ad.getRemoved().values().size());


        int addedAdjs = 0;

        // finally add new adjacencies; this is done last to make sure the ports we refer to have been added already
        for (PortAdjcy pa: ad.getAdded().values()) {
            // log.info("adding an adjcy: "+pa.getA().getUrn()+ " -- "+pa.getZ().getUrn());
            // fix port object refs
            pa.setA(portRepo.findByUrn(pa.getA().getUrn()).orElseThrow(NoSuchElementException::new));
            pa.setZ(portRepo.findByUrn(pa.getZ().getUrn()).orElseThrow(NoSuchElementException::new));
            pa.setVersion(newVersion);
            pa.getA().getAdjciesWhereA().add(pa);
            pa.getZ().getAdjciesWhereZ().add(pa);
            adjcyRepo.save(pa);
            addedAdjs++;
        }
        log.debug("done adding adjacencies: added: "+addedAdjs);

        int unchangedAdjs = 0;
        // now handle unchanged adjacencies; these too need to be set to latest version
        for (PortAdjcy pa: ad.getUnchanged().values()) {
            PortAdjcy existing = null;
            boolean found = false;
            for (PortAdjcy candidate : adjcyRepo.findByVersion(currentVersion)) {
                if (candidate.getA().getUrn().equals(pa.getA().getUrn()) &&
                        candidate.getZ().getUrn().equals(pa.getZ().getUrn())) {
                    existing = candidate;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ConsistencyException("adjcy in modified delta does not exist in repo");
            }
            existing.setVersion(newVersion);
            adjcyRepo.save(existing);
            unchangedAdjs++;

        }
        log.debug("done updating version for unchanged adjacencies: "+unchangedAdjs+" entries");
        adjcyRepo.flush();

    }

    public Topology currentTopology() throws ConsistencyException {
        List<Device> devices = new ArrayList<>();
        List<PortAdjcy> adjcies = new ArrayList<>();
        List<Port> ports = new ArrayList<>();
        Topology t = Topology.builder()
                .adjcies(adjcies)
                .devices(devices)
                .ports(ports)
                .build();

        if (versionRepo.findAll().size() != 0) {
            List<Version> versions = versionRepo.findByValid(true);
            if (versions.size() != 1) {
                throw new ConsistencyException("exactly one valid version can exist");
            }
            devices = deviceRepo.findByVersion(versions.get(0));
            adjcies = adjcyRepo.findByVersion(versions.get(0));
            log.info("found "+devices.size()+" devices in version "+versions.get(0).getId());
            log.info("found "+adjcies.size()+" adjcies in version "+versions.get(0).getId());
            t.setDevices(devices);
            t.setAdjcies(adjcies);
            devices.forEach(d -> {
                for (Port p : d.getPorts()) {
                    if (p.getVersion() != null && p.getVersion().getValid()) {
                        ports.add(p);
                    }
                }
            });
            t.setPorts(ports);

        }


        return t;
    }



    private Map<String, TopoUrn> urnsFromDevices(List<Device> devices) {
        Map<String, TopoUrn> urns = new HashMap<>();

        devices.forEach(d -> {
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

            });
        });

        return urns;

    }
    private List<TopoAdjcy> topoAdjciesFromPortAdjcies(List<PortAdjcy> portAdjcies) throws TopoException {
        List<TopoAdjcy> adjcies = new ArrayList<>();

        for (PortAdjcy pa : portAdjcies) {
            // all our adjcies should point to ports in the topoUrnMap

            if (!this.topoUrnMap.containsKey(pa.getA().getUrn())) {
                log.error(pa.getA().getUrn()+" -- "+pa.getZ().getUrn());

                throw new TopoException("missing A "+pa.getA().getUrn());

            } else if (!this.topoUrnMap.containsKey(pa.getZ().getUrn())) {

                log.error(pa.getA().getUrn()+" -- "+pa.getZ().getUrn());
                throw new TopoException("missing Z "+pa.getZ().getUrn());

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
        for (Device d: devices) {
            if (this.topoUrnMap.containsKey(d.getUrn())) {
                TopoUrn deviceUrn = this.topoUrnMap.get(d.getUrn());
                for (Port p : d.getPorts()) {
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
                        throw new TopoException("missing a port urn!");
                    }
                }
            } else {
                throw new TopoException("missing a device urn!");
            }
        }

        return adjcies;
    }

}
