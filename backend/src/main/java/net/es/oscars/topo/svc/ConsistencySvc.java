package net.es.oscars.topo.svc;

import lombok.extern.slf4j.Slf4j;

import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.topo.beans.ConsistencyReport;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.beans.VersionDelta;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortAdjcyRepository;
import net.es.oscars.topo.db.PortRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.pop.ConsistencyException;
import net.es.oscars.topo.pop.UIPopulator;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Slf4j
@Service
@Component
@Transactional
public class ConsistencySvc {
    private ConnectionRepository connRepo;
    private PortAdjcyRepository adjcyRepo;

    private DeviceRepository deviceRepo;
    private PortRepository portRepo;

    private UIPopulator ui;

    private TopoService ts;

    @Autowired
    public ConsistencySvc(PortAdjcyRepository adjcyRepo,
                          PortRepository portRepo,
                          DeviceRepository deviceRepo,
                          ConnectionRepository connRepo,
                          UIPopulator ui,
                          TopoService ts) {
        this.adjcyRepo = adjcyRepo;
        this.portRepo = portRepo;
        this.deviceRepo = deviceRepo;
        this.connRepo = connRepo;
        this.ui = ui;
        this.ts = ts;
    }

    public List<ConsistencyReport> checkConsistency(VersionDelta vd) {
        log.info("checking topology consistency..");

        // TODO: implement this

        log.info("checking connection references");
        List<ConsistencyReport> crs = new ArrayList<>();

        List<Connection> reserved = connRepo.findByPhase(Phase.RESERVED);
        for (Connection c : reserved) {
            crs.add(this.checkConnection(c));
        }
        List<Connection> held = connRepo.findByPhase(Phase.HELD);
        for (Connection c : held) {
            crs.add(this.checkConnection(c));
        }
        return crs;
    }

    public ConsistencyReport checkConnection(Connection c) {
        ConsistencyReport cr = ConsistencyReport.builder().build();
        Map<String, String> errors = new HashMap<>();

        Components cmp = c.getReserved().getCmp();
        for (VlanJunction vj : cmp.getJunctions()) {
            String devUrn = vj.getDeviceUrn();
            try {
                Device d = this.checkDeviceUrn(devUrn);
                if (!d.getVersion().getValid()) {
                    errors.put(devUrn, "junction device not valid");
                }
            } catch (ConsistencyException ex) {
                errors.put(devUrn, ex.getMessage());
            }
        }
        for (VlanFixture f : cmp.getFixtures()) {
            String portUrn = f.getPortUrn();
            try {
                Port p = this.checkPortUrn(portUrn);
                if (!p.getVersion().getValid()) {
                    errors.put(portUrn, "junction device not valid");
                } else {
                    // TODO: check capacity for ports

                    Integer vlanId = f.getVlan().getVlanId();
                    boolean contained = false;
                    for (IntRange vlanRange : p.getReservableVlans()) {
                        if (vlanRange.contains(vlanId)) {
                            contained = true;
                        }
                    }
                    if (!contained) {
                        errors.put(portUrn, "vlan " + vlanId + " not in ranges");
                    }
                }
            } catch (ConsistencyException ex) {
                errors.put(portUrn, ex.getMessage());
            }
        }
        for (VlanPipe pipe : cmp.getPipes()) {
            for (EroHop hop : pipe.getAzERO()) {
                String urn = hop.getUrn();
                if (!ts.getTopoUrnMap().containsKey(urn)) {
                    Optional<Device> maybeDev = deviceRepo.findByUrn(urn);
                    Optional<Port> maybePort = portRepo.findByUrn(urn);

                    if (maybeDev.isPresent()) {
                        Device d = maybeDev.get();
                        if (!d.getVersion().getValid()) {
                            errors.put(urn, "ERO hop invalid device");

                        } else {
                            log.error("Internal error: valid device missing from current topo! "+urn);
                        }

                    } else if (maybePort.isPresent()) {
                        Port p = maybePort.get();
                        if (!p.getVersion().getValid()) {
                            errors.put(urn, "ERO hop invalid port");

                        } else {
                            log.error("Internal error: valid device missing from current topo! "+urn);
                        }

                    } else {
                        errors.put(urn, "ERO hop urn missing");
                    }

                } else {
                    TopoUrn topoUrn = ts.getTopoUrnMap().get(urn);
                    if (topoUrn.getUrnType().equals(UrnType.PORT)) {
                        Port p = topoUrn.getPort();
                        // TODO: check capacity for ports
                        if (p == null) {
                            errors.put(urn, "ERO hop missing port");
                        } else if (!p.getVersion().getValid()) {
                            errors.put(urn, "ERO hop invalid port");
                        }
                    } else if (topoUrn.getUrnType().equals(UrnType.DEVICE)) {
                        Device d = topoUrn.getDevice();
                        if (d == null) {
                            errors.put(urn, "ERO hop missing device");
                        } else if (!d.getVersion().getValid()) {
                            errors.put(urn, "ERO hop invalid device");
                        }

                    } else {
                        errors.put(urn, "invalid topology URN type");

                    }

                }
            }
        }
        return cr;

    }

    public Port checkPortUrn(String urn) throws ConsistencyException {
        Map<String, TopoUrn> urnMap = ts.getTopoUrnMap();
        if (!urnMap.containsKey(urn)) {
            Optional<Port> maybePort = portRepo.findByUrn(urn);
            if (maybePort.isPresent()) {
                return maybePort.get();
            } else {
                throw new ConsistencyException("missing port: " + urn);
            }

        } else {
            TopoUrn topoUrn = urnMap.get(urn);
            if (!topoUrn.getUrnType().equals(UrnType.PORT)) {
                throw new ConsistencyException("invalid urn type for port: " + urn);
            } else {
                Port p = topoUrn.getPort();
                if (p == null) {
                    throw new ConsistencyException("invalid urn type for port: " + urn);

                } else {
                    return topoUrn.getPort();
                }
            }
        }
    }

    public Device checkDeviceUrn(String urn) throws ConsistencyException {
        Map<String, TopoUrn> urnMap = ts.getTopoUrnMap();
        if (!urnMap.containsKey(urn)) {
            Optional<Device> maybeDevice = deviceRepo.findByUrn(urn);
            if (maybeDevice.isPresent()) {
                return maybeDevice.get();
            } else {
                throw new ConsistencyException("missing device: " + urn);
            }

        } else {
            TopoUrn topoUrn = urnMap.get(urn);
            if (!topoUrn.getUrnType().equals(UrnType.DEVICE)) {
                throw new ConsistencyException("invalid urn type for device: " + urn);
            } else {
                Device p = topoUrn.getDevice();
                if (p == null) {
                    throw new ConsistencyException("invalid urn type for device: " + urn);

                } else {
                    return topoUrn.getDevice();
                }
            }
        }
    }

}
