package net.es.oscars.topo.svc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.StartupException;
import net.es.oscars.app.props.TopoProperties;
import net.es.oscars.topo.beans.*;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.db.PortRepository;
import net.es.oscars.topo.db.VersionRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.ent.Version;
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


    public void updateTopo() throws ConsistencyException, TopoException {
        Optional<Version> maybeCurrent = currentVersion();

        if (maybeCurrent.isPresent()) {
            Version current = maybeCurrent.get();
            List<Device> devices = deviceRepo.findByVersion(current);
            List<PortAdjcy> adjcies = adjcyRepo.findByVersion(current);

            this.topoUrnMap = this.urnsFromDevices(devices);

            this.topoAdjcies = new ArrayList<>();
            this.topoAdjcies.addAll(adjciesFromDevices(devices));
            this.topoAdjcies.addAll(transformToTopoAdjcies(adjcies));
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
            versionRepo.save(noLongerValid);
        }

        versionRepo.save(newVersion);
        return newVersion;
    }

    @Transactional
    public void mergeVersionDelta(VersionDelta vd, Version newVersion) throws ConsistencyException {
        // log.info("merging version delta w/ timestamp: "+newVersion.getUpdated());
        Delta<Device> dd = vd.getDeviceDelta();
        Delta<PortAdjcy> ad = vd.getAdjcyDelta();
        Delta<Port> pd = vd.getPortDelta();

        // to remove adjacencies that no longer exist, do nothing, just don't set the new version

        // now modify existing ones
        for (PortAdjcy pa: ad.getModified()) {
            // log.warn("modifying an adjcy: "+pa.getA().getUrn()+ " -- "+pa.getZ().getUrn());
            boolean found = false;
            PortAdjcy prev = null;
            for (PortAdjcy candidate : adjcyRepo.findAll()) {
                if (candidate.getA().getUrn().equals(pa.getA().getUrn()) &&
                        candidate.getZ().getUrn().equals(pa.getZ().getUrn())) {
                    prev = candidate;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ConsistencyException("adjcy found in delta does not exist in repo");
            }
            prev.setMetrics(pa.getMetrics());
            prev.setVersion(newVersion);
            adjcyRepo.save(prev);
        }


        // to delete gone devices, we don't do anything; we just won't update their version

        // now add new devices
        for (Device d: dd.getAdded()) {
            if (deviceRepo.findByUrn(d.getUrn()).isPresent()) {
                // this means it's present in the db but it has an invalid version
                Version v = deviceRepo.findByUrn(d.getUrn()).get().getVersion();
                if (v.getValid()) {
                    throw new ConsistencyException("merge error");
                }
                dd.getModified().add(d);

            } else {
                // log.info("adding a device "+d.getUrn());
                d.setVersion(newVersion);
                deviceRepo.save(d);
                for (Port p : d.getPorts()) {
                    pd.getAdded().remove(p);
                }
            }
        }

        // then merge modified ones including the ports
        for (Device md: dd.getModified()) {
            Device prev = deviceRepo.findByUrn(md.getUrn()).orElseThrow(NoSuchElementException::new);
            prev.setCapabilities(md.getCapabilities());
            prev.setIpv4Address(md.getIpv4Address());
            prev.setIpv6Address(md.getIpv6Address());
            prev.setModel(md.getModel());
            prev.setReservableVlans(md.getReservableVlans());
            prev.setType(md.getType());
            prev.setVersion(newVersion);

            for (Port ap: pd.getAdded()) {
                Optional<Port> maybeExists = portRepo.findByUrn(ap.getUrn());
                if (maybeExists.isPresent()) {
                    // it exists in the repo so instead of adding we need to modify it instead
                    pd.getModified().add(ap);
                } else {
                    if (ap.getDevice().getUrn().equals(prev.getUrn())) {
                        ap.setDevice(prev);
                        ap.setVersion(newVersion);
                        prev.getPorts().add(ap);
                    }

                }
            }
            for (Port mp: pd.getModified()) {
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
                }
            }



            deviceRepo.save(prev);
        }



        // finally add new adjacencies; this is done last to make sure the ports we refer to are
        for (PortAdjcy pa: ad.getAdded()) {
            // log.info("adding an adjcy: "+pa.getA().getUrn()+ " -- "+pa.getZ().getUrn());
            // fix port object refs
            pa.setA(portRepo.findByUrn(pa.getA().getUrn()).orElseThrow(NoSuchElementException::new));
            pa.setZ(portRepo.findByUrn(pa.getZ().getUrn()).orElseThrow(NoSuchElementException::new));
            pa.setVersion(newVersion);
            pa.getA().getAdjciesWhereA().add(pa);
            pa.getZ().getAdjciesWhereZ().add(pa);
            adjcyRepo.save(pa);
        }

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
            // log.info("found "+devices.size()+" devices in version "+versions.get(0).getUpdated());
            // log.info("found "+adjcies.size()+" adjcies in version "+versions.get(0).getUpdated());
            t.setDevices(devices);
            t.setAdjcies(adjcies);
            devices.forEach(d -> {
                ports.addAll(d.getPorts());
            });
            t.setPorts(ports);

        }


        return t;
    }



    private Map<String, TopoUrn> urnsFromDevices(List<Device> devices) throws TopoException {
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
                    .build();
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
                        .build();

                urns.put(p.getUrn(), portUrn);

            });
        });

        return urns;

    }
    private List<TopoAdjcy> transformToTopoAdjcies(List<PortAdjcy> portAdjcies) throws TopoException {
        List<TopoAdjcy> adjcies = new ArrayList<>();

        for (PortAdjcy pa : portAdjcies) {
            if (this.topoUrnMap.containsKey(pa.getA().getUrn()) &&
                    this.topoUrnMap.containsKey(pa.getZ().getUrn())) {

                TopoUrn aUrn = this.topoUrnMap.get(pa.getA().getUrn());
                TopoUrn zUrn = this.topoUrnMap.get(pa.getZ().getUrn());
                Map<Layer, Long> metrics = new HashMap<>();

                pa.getMetrics().entrySet().forEach(e -> {
                    metrics.put(e.getKey(), e.getValue());
                });

                TopoAdjcy adjcy = TopoAdjcy.builder().a(aUrn).z(zUrn).metrics(metrics).build();
                adjcies.add(adjcy);

            } else {
                throw new TopoException("missing a port urn!");
            }

        }
        return adjcies;

    }

    private List<TopoAdjcy> adjciesFromDevices(List<Device> devices) throws TopoException {
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
