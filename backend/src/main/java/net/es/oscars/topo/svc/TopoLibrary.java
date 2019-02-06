package net.es.oscars.topo.svc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.Delta;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.beans.VersionDelta;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.ent.Version;
import net.es.oscars.topo.enums.Layer;

import java.util.*;

@Slf4j
public class TopoLibrary {

    public static List<PortAdjcy> adjciesOriginatingFrom(String urn, List<PortAdjcy> allAdjcies) {
        List<PortAdjcy> result = new ArrayList<>();
        allAdjcies.forEach(adj -> {
            if (adj.getA().getUrn().equals(urn)) {
                result.add(adj);
            }
        });

        return result;
    }

    public static VersionDelta compare(Topology alpha, Topology beta) {
        // log.info("comparing topologies");
        Delta<PortAdjcy> adjcyDelta = comparePortAdjcies(alpha.getAdjcies(), beta.getAdjcies());

        Delta<Device> deviceDelta = compareDevices(alpha, beta);

        Delta<Port> portDelta = comparePorts(alpha, beta, deviceDelta);

        boolean changed = false;
        if (adjcyDelta.getModified().size() > 0 ||
                adjcyDelta.getAdded().size() > 0 ||
                adjcyDelta.getRemoved().size() > 0) {
            changed = true;
        }
        if (deviceDelta.getModified().size() > 0 ||
                deviceDelta.getAdded().size() > 0 ||
                deviceDelta.getRemoved().size() > 0) {
            changed = true;
        }
        if (portDelta.getModified().size() > 0 ||
                portDelta.getAdded().size() > 0 ||
                portDelta.getRemoved().size() > 0) {
            changed = true;
        }

        VersionDelta vd = VersionDelta.builder()
                .adjcyDelta(adjcyDelta)
                .deviceDelta(deviceDelta)
                .portDelta(portDelta)
                .changed(changed)
                .build();
        /*
        try {
            String pretty = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(vd);
            log.debug(pretty);
        } catch (JsonProcessingException ex) {
            log.error("json error", ex);
        }
        */

        return vd;
    }

    public static Delta<Device> compareDevices(Topology alpha, Topology beta) {

        //log.info("comparing devices");
        Map<String, Device> added = new HashMap<>();
        Map<String, Device> modified = new HashMap<>();
        Map<String, Device> removed = new HashMap<>();
        Map<String, Device> unchanged = new HashMap<>();

        List<String> checkForChanges = new ArrayList<>();

        for (String urn : alpha.getDevices().keySet()) {
            if (beta.getDevices().keySet().contains(urn)) {
                checkForChanges.add(urn);
            } else {
                removed.put(urn, alpha.getDevices().get(urn));
            }
        }
        for (String urn : beta.getDevices().keySet()) {
            if (!alpha.getDevices().keySet().contains(urn)) {
                // set ports to empty; they will get added by the port delta
                beta.getDevices().get(urn).setPorts(new HashSet<>());
                added.put(urn, beta.getDevices().get(urn));
            }
        }


        for (String urn : checkForChanges) {
            Device aDevice = alpha.getDevices().get(urn);
            Device bDevice = beta.getDevices().get(urn);
            boolean changed = false;
            if (!aDevice.getModel().equals(bDevice.getModel())) {
                changed = true;
            }
            if (!aDevice.getIpv4Address().equals(bDevice.getIpv4Address())) {
                changed = true;
            }
            if (!aDevice.getType().equals(bDevice.getType())) {
                changed = true;
            }
            if (!aDevice.getLocation().equals(bDevice.getLocation())) {
                changed = true;
            }
            if (!aDevice.getLocationId().equals(bDevice.getLocationId())) {
                changed = true;
            }
            if (!aDevice.getLatitude().equals(bDevice.getLatitude())) {
                changed = true;
            }
            if (!aDevice.getLongitude().equals(bDevice.getLongitude())) {
                changed = true;
            }
            if (!aDevice.getCapabilities().equals(bDevice.getCapabilities())) {
                changed = true;
            }
            if (!aDevice.getReservableVlans().equals(bDevice.getReservableVlans())) {
                changed = true;
            }
            if (!changed) {
                unchanged.put(aDevice.getUrn(), aDevice);
            } else {
                log.info("will modify " + urn);
                modified.put(urn, bDevice);
            }
        }

        return Delta.<Device>builder()
                .added(added)
                .modified(modified)
                .removed(removed)
                .unchanged(unchanged)
                .build();
    }


    public static Delta<Port> comparePorts(Topology alpha, Topology beta, Delta<Device> deviceDelta) {
        // log.info("comparing ports");
        Map<String, Port> added = new HashMap<>();
        Map<String, Port> modified = new HashMap<>();
        Map<String, Port> removed = new HashMap<>();
        Map<String, Port> unchanged = new HashMap<>();

        List<String> checkForChanges = new ArrayList<>();


        for (String urn : alpha.getPorts().keySet()) {
            if (beta.getPorts().keySet().contains(urn)) {
                // log.info(" check changes "+urn);
                checkForChanges.add(urn);
            } else {
                log.info(" will remove port " + urn);
                removed.put(urn, alpha.getPorts().get(urn));
            }
        }

        for (String urn : beta.getPorts().keySet()) {
            if (!alpha.getPorts().keySet().contains(urn)) {
                log.info(" will add port " + urn);
                added.put(urn, beta.getPorts().get(urn));
            }
        }

        for (String urn : checkForChanges) {
            Port aPort = alpha.getPorts().get(urn);
            Port bPort = beta.getPorts().get(urn);
            boolean changed = false;
            if (aPort.getIpv4Address() == null) {
                if (bPort.getIpv4Address() != null) {
                    log.debug(urn + " changed ipv4 (from null)");
                    changed = true;
                }
            } else if (!aPort.getIpv4Address().equals(bPort.getIpv4Address())) {
                log.debug(urn + " changed ipv4");
                changed = true;
            }

            if (aPort.getIfce() == null) {
                if (bPort.getIfce() != null) {
                    log.debug(urn + " changed ifce (from null)");
                    changed = true;
                }
            } else if (!aPort.getIfce().equals(bPort.getIfce())) {
                log.debug(urn + " changed ifce");
                changed = true;
            }

            if (aPort.getTags() == null) {
                if (bPort.getTags() != null) {
                    log.debug(urn + " changed tags (from null)");
                    changed = true;
                }
            } else if (!aPort.getTags().equals(bPort.getTags())) {
                log.debug(urn + " changed tags");
                changed = true;
            }

            if (!aPort.getCapabilities().equals(bPort.getCapabilities())) {
                log.debug(urn + " changed caps");
                changed = true;
            }
            if (!aPort.getReservableIngressBw().equals(bPort.getReservableIngressBw())) {
                log.debug(urn + " changed ibw");
                changed = true;
            }
            if (!aPort.getReservableEgressBw().equals(bPort.getReservableEgressBw())) {
                log.debug(urn + " changed ebw");
                changed = true;
            }
            if (!aPort.getReservableVlans().equals(bPort.getReservableVlans())) {
                log.debug(urn + " changed vlans");
                changed = true;
            }

            if (changed) {
                // log.info("will modify "+aPort.getUrn());
                modified.put(urn, bPort);
            } else {
                unchanged.put(urn, aPort);
            }

        }
        return Delta.<Port>builder()
                .added(added)
                .modified(modified)
                .removed(removed)
                .unchanged(unchanged)
                .build();
    }

    public static Delta<PortAdjcy> comparePortAdjcies(List<PortAdjcy> alpha, List<PortAdjcy> beta) {
        // log.info("comparing port adjcies");
        Map<String, PortAdjcy> added = new HashMap<>();
        Map<String, PortAdjcy> modified = new HashMap<>();
        Map<String, PortAdjcy> removed = new HashMap<>();
        Map<String, PortAdjcy> unchanged = new HashMap<>();

        for (PortAdjcy aAdjcy : alpha) {
            String adjcyStr = aAdjcy.getA().getUrn() + " -- " + aAdjcy.getZ().getUrn();
            PortAdjcy newAdjcy = null;

            String a_a_urn = aAdjcy.getA().getUrn();
            String a_z_urn = aAdjcy.getZ().getUrn();
            boolean found = false;
            boolean changed = false;
            for (PortAdjcy bAdjcy : beta) {
                String b_a_urn = bAdjcy.getA().getUrn();
                String b_z_urn = bAdjcy.getZ().getUrn();
                if (a_a_urn.equals(b_a_urn) && a_z_urn.equals(b_z_urn)) {
                    found = true;
                    for (Layer l : aAdjcy.getMetrics().keySet()) {
                        if (!bAdjcy.getMetrics().containsKey(l)) {
                            log.info("  removed a metric " + l + " on " + adjcyStr);
                            changed = true;
                        }
                    }
                    for (Layer l : bAdjcy.getMetrics().keySet()) {
                        if (!aAdjcy.getMetrics().containsKey(l)) {
                            log.info("  added a metric " + l + " on " + adjcyStr);
                            changed = true;
                        }
                    }

                    if (!changed) {
                        for (Layer l : aAdjcy.getMetrics().keySet()) {
                            Long aMetric = aAdjcy.getMetrics().get(l);
                            Long bMetric = bAdjcy.getMetrics().get(l);
                            if (!aMetric.equals(bMetric)) {
                                log.info("  modified metric " + l + " on " + adjcyStr);
                                changed = true;
                            }
                        }
                    }
                    if (changed) {
                        newAdjcy = bAdjcy;
                    }
                }
            }
            if (found) {
                if (changed) {
                    // log.info("will modify "+aAdjcy.getA().getUrn()+ " -- "+aAdjcy.getZ().getUrn());
                    modified.put(aAdjcy.getUrn(), newAdjcy);
                } else {
                    unchanged.put(aAdjcy.getUrn(), aAdjcy);
                }
            } else {
                // log.info("will remove "+aAdjcy.getA().getUrn()+ " -- "+aAdjcy.getZ().getUrn());
                removed.put(aAdjcy.getUrn(), aAdjcy);
            }
        }
        for (PortAdjcy bAdjcy : beta) {
            // log.info("checking if exists: "+ bAdjcy.getA().getUrn()+ " -- "+bAdjcy.getZ().getUrn());
            String b_a_urn = bAdjcy.getA().getUrn();
            String b_z_urn = bAdjcy.getZ().getUrn();
            boolean found = false;
            for (PortAdjcy aAdjcy : alpha) {
                String a_a_urn = aAdjcy.getA().getUrn();
                String a_z_urn = aAdjcy.getZ().getUrn();
                if (b_a_urn.equals(a_a_urn) && b_z_urn.equals(a_z_urn)) {
                    found = true;
                }
            }
            if (!found) {
                // log.info("will add "+bAdjcy.getA().getUrn()+ " -- "+bAdjcy.getZ().getUrn());
                added.put(bAdjcy.getUrn(), bAdjcy);
            }
        }


        return Delta.<PortAdjcy>builder()
                .added(added)
                .modified(modified)
                .removed(removed)
                .unchanged(unchanged)
                .build();
    }
}
