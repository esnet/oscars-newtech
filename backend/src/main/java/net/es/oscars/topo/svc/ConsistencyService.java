package net.es.oscars.topo.svc;

import lombok.extern.slf4j.Slf4j;

import net.es.oscars.resv.db.ConnectionRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.resv.enums.Phase;
import net.es.oscars.topo.beans.ConsistencyReport;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.db.DeviceRepository;
import net.es.oscars.topo.db.PortRepository;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.pop.ConsistencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;


@Slf4j
@Service
@Component
@Transactional
public class ConsistencyService {
    private ConnectionRepository connRepo;

    private DeviceRepository deviceRepo;
    private PortRepository portRepo;


    private ConsistencyReport latestReport;

    private TopoService ts;

    @Autowired
    public ConsistencyService(PortRepository portRepo,
                              DeviceRepository deviceRepo,
                              ConnectionRepository connRepo,
                              TopoService ts) {
        this.portRepo = portRepo;
        this.deviceRepo = deviceRepo;
        this.connRepo = connRepo;
        this.ts = ts;
        this.latestReport = ConsistencyReport.builder()
                .topologyUpdated(Instant.MIN)
                .generated(Instant.now())
                .issuesByUrn(new HashMap<>())
                .issuesByConnectionId(new HashMap<>())
                .build();
    }

    public ConsistencyReport getLatestReport() {
        return this.latestReport;
    }

    public void checkConsistency() throws ConsistencyException {
        log.info("Checking topology consistency.");

        ConsistencyReport cr = ConsistencyReport.builder()
                .issuesByConnectionId(new HashMap<>())
                .issuesByUrn(new HashMap<>())
                .generated(Instant.now())
                .topologyUpdated(ts.currentVersion().orElseThrow(ConsistencyException::new).getUpdated())
                .build();

        List<Connection> reserved = connRepo.findByPhase(Phase.RESERVED);
        for (Connection c : reserved) {
            this.checkConnection(c, cr);
        }
        List<Connection> held = connRepo.findByPhase(Phase.HELD);
        for (Connection c : held) {
            this.checkConnection(c, cr);
        }
        this.checkBlankPorts(cr);
        this.latestReport = cr;
        log.info("Generated consistency report.");
    }
    public void checkBlankPorts(ConsistencyReport cr) {
        Map<String, TopoUrn> urnMap = ts.getTopoUrnMap();
        for (String urn: urnMap.keySet()) {
            if (urn.contains("BLANK")) {
                cr.addUrnError(urn, "blank port found -  unconfigured lag?");
            }
        }

    }

    public void checkConnection(Connection c, ConsistencyReport cr) {
        if (!c.getPhase().equals(Phase.RESERVED)) {
            return;
        }

        Components cmp = c.getReserved().getCmp();
        for (VlanJunction vj : cmp.getJunctions()) {
            String devUrn = vj.getDeviceUrn();
            try {
                Device d = this.checkDeviceUrn(devUrn);
                if (!d.getVersion().getValid()) {
                    cr.addConnectionError(c.getConnectionId(), "not valid device " + devUrn + " found in junction");
                    cr.addUrnError(devUrn, "is not valid device but found in junction for " + c.getConnectionId());
                }
            } catch (ConsistencyException ex) {
                cr.addConnectionError(c.getConnectionId(), ex.getMessage());
                cr.addUrnError(devUrn, ex.getMessage());
            }
        }

        for (VlanFixture f : cmp.getFixtures()) {
            String portUrn = f.getPortUrn();
            try {
                Port p = this.checkPortUrn(portUrn);
                if (!p.getVersion().getValid()) {
                    cr.addConnectionError(c.getConnectionId(), "not valid port " + portUrn + " found in fixture");
                    cr.addUrnError(portUrn, "is not valid port but found in fixture for " + c.getConnectionId());

                } else {
                    // TODO: check capacity for ports
                    if (!p.getCapabilities().contains(Layer.ETHERNET)) {
                        cr.addConnectionError(c.getConnectionId(), "port " + portUrn + " does not have ETHERNET capability; check topology");
                        cr.addUrnError(portUrn, "port " + portUrn + " does not have ETHERNET capability, but found in " + c.getConnectionId());

                    } else {

                        Integer vlanId = f.getVlan().getVlanId();
                        boolean contained = false;
                        for (IntRange vlanRange : p.getReservableVlans()) {
                            if (vlanRange.contains(vlanId)) {
                                contained = true;
                            }
                        }
                        if (!contained) {
                            cr.addConnectionError(c.getConnectionId(), "port " + portUrn + " does not contain in vlan ranges: " + vlanId);
                            cr.addUrnError(portUrn, "port " + portUrn + " does not contain in vlan ranges: " + vlanId + " found in " + c.getConnectionId());
                        }
                    }
                }
            } catch (ConsistencyException ex) {
                cr.addConnectionError(c.getConnectionId(), ex.getMessage());
                cr.addUrnError(portUrn, ex.getMessage());
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
                            cr.addConnectionError(c.getConnectionId(), "ero hop has invalid device, urn: " + urn);
                            cr.addUrnError(urn, "pipe ero hop has invalid device, urn: " + urn + " for connId: " + c.getConnectionId());

                        } else {
                            log.error("Internal error: valid device missing from current topo! " + urn);
                        }

                    } else if (maybePort.isPresent()) {
                        Port p = maybePort.get();
                        if (!p.getVersion().getValid()) {
                            cr.addConnectionError(c.getConnectionId(), "ero hop has invalid port, urn: " + urn);
                            cr.addUrnError(urn, "pipe ero hop has invalid port, urn: " + urn + " for connId: " + c.getConnectionId());

                        } else {
                            log.error("Internal error: valid device missing from current topo! " + urn);
                        }

                    } else {
                        cr.addConnectionError(c.getConnectionId(), "ero hop completely missing in topo, urn: " + urn);
                        cr.addUrnError(urn, "pipe ero hop completely missing in topo, urn: " + urn + " for connId: " + c.getConnectionId());
                    }

                } else {
                    TopoUrn topoUrn = ts.getTopoUrnMap().get(urn);
                    if (topoUrn.getUrnType().equals(UrnType.PORT)) {
                        Port p = topoUrn.getPort();
                        // TODO: check capacity for ports
                        if (p == null) {
                            cr.addConnectionError(c.getConnectionId(), "ero hop has missing port , urn: " + urn);
                            cr.addUrnError(urn, "pipe ero hop has missing port, urn: " + urn + " for connId: " + c.getConnectionId());
                        } else if (!p.getVersion().getValid()) {
                            cr.addConnectionError(c.getConnectionId(), "ero hop has invalid port , urn: " + urn);
                            cr.addUrnError(urn, "pipe ero hop has invalid port, urn: " + urn + " for connId: " + c.getConnectionId());
                        }
                    } else if (topoUrn.getUrnType().equals(UrnType.DEVICE)) {
                        Device d = topoUrn.getDevice();
                        if (d == null) {
                            cr.addConnectionError(c.getConnectionId(), "ero hop has missing device , urn: " + urn);
                            cr.addUrnError(urn, "pipe ero hop has missing device, urn: " + urn + " for connId: " + c.getConnectionId());
                        } else if (!d.getVersion().getValid()) {
                            cr.addConnectionError(c.getConnectionId(), "ero hop has invalid device , urn: " + urn);
                            cr.addUrnError(urn, "pipe ero hop has invalid device, urn: " + urn + " for connId: " + c.getConnectionId());
                        }

                    } else {
                        cr.addConnectionError(c.getConnectionId(), "ero hop has invalid type, urn: " + urn);
                        cr.addUrnError(urn, "pipe ero hop has invalid type, urn: " + urn + " for connId: " + c.getConnectionId());

                    }

                }
            }
        }

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
