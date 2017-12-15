package net.es.oscars.topo.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.Delta;
import net.es.oscars.topo.beans.Topology;
import net.es.oscars.topo.beans.VersionDelta;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;

import java.util.ArrayList;
import java.util.List;

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
        Delta<Device> deviceDelta = compareDevices(alpha.getDevices(), beta.getDevices());
        Delta<Port> portDelta = comparePorts(alpha.getPorts(), beta.getPorts());
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

        return VersionDelta.builder()
                .adjcyDelta(adjcyDelta)
                .deviceDelta(deviceDelta)
                .portDelta(portDelta)
                .changed(changed)
                .build();
    }

    public static Delta<Device> compareDevices(List<Device> alpha, List<Device> beta) {
        //log.info("comparing devices");
        List<Device> added = new ArrayList<>();
        List<Device> modified = new ArrayList<>();
        List<Device> removed = new ArrayList<>();
        List<Device> unchanged = new ArrayList<>();

        for (Device aDevice : alpha) {
            boolean found = false;
            boolean changed = false;
            for (Device bDevice : beta) {
                if (aDevice.getUrn().equals(bDevice.getUrn())) {
                    found = true;
                    if (!aDevice.getModel().equals(bDevice.getModel())) {
                        changed = true;
                    }
                    if (!aDevice.getIpv4Address().equals(bDevice.getIpv4Address())) {
                        changed = true;
                    }
                    if (!aDevice.getType().equals(bDevice.getType())) {
                        changed = true;
                    }
                    if (!aDevice.getCapabilities().equals(bDevice.getCapabilities())) {
                        changed = true;
                    }
                    if (!aDevice.getReservableVlans().equals(bDevice.getReservableVlans())) {
                        changed = true;
                    }
                }
            }
            if (found) {
                if (!changed) {
                    unchanged.add(aDevice);
                } else {
                    log.info("will modify "+aDevice.getUrn());
                    modified.add(aDevice);
                }
            } else {
                log.info("will remove "+aDevice.getUrn());
                removed.add(aDevice);
            }
        }

        for (Device bDevice : beta) {
            boolean found = false;
            for (Device aDevice : alpha) {
                if (aDevice.getUrn().equals(bDevice.getUrn())) {
                    found = true;
                }
            }
            if (!found) {
                log.info("will add "+bDevice.getUrn());
                added.add(bDevice);
            }
        }

        return Delta.<Device>builder()
                .added(added)
                .modified(modified)
                .removed(removed)
                .unchanged(unchanged)
                .build();
    }


    public static Delta<Port> comparePorts(List<Port> alpha, List<Port> beta) {
        // log.info("comparing ports");
        List<Port> added = new ArrayList<>();
        List<Port> modified = new ArrayList<>();
        List<Port> removed = new ArrayList<>();
        List<Port> unchanged = new ArrayList<>();

        for (Port aPort : alpha) {
            boolean found = false;
            boolean changed = false;
            for (Port bPort : beta) {
                if (aPort.getUrn().equals(bPort.getUrn())) {
                    found = true;
                    if (aPort.getIpv4Address() == null) {
                        if (bPort.getIpv4Address() != null) {
                            changed = true;
                        }

                    } else if (!aPort.getIpv4Address().equals(bPort.getIpv4Address())) {
                        changed = true;
                    }
                    if (!aPort.getCapabilities().equals(bPort.getCapabilities())) {
                        changed = true;
                    }
                    if (!aPort.getReservableIngressBw().equals(bPort.getReservableIngressBw())) {
                        changed = true;
                    }
                    if (!aPort.getReservableEgressBw().equals(bPort.getReservableEgressBw())) {
                        changed = true;
                    }
                    if (!aPort.getReservableVlans().equals(bPort.getReservableVlans())) {
                        changed = true;
                    }
                    if (aPort.getTags() == null) {
                        if (bPort.getTags() != null) {
                            changed = true;
                        }
                    } else if (!aPort.getTags().equals(bPort.getTags())) {
                        changed = true;
                    }
                }
            }
            if (!found) {
                // log.info("will remove "+aPort.getUrn());

                removed.add(aPort);
            } else {
                if (changed) {
                    // log.info("will modify "+aPort.getUrn());
                    modified.add(aPort);
                } else {
                    unchanged.add(aPort);
                }
            }
        }

        for (Port bPort : beta) {
            boolean found = false;
            for (Port aPort : alpha) {
                if (aPort.getUrn().equals(bPort.getUrn())) {
                    found = true;
                }
            }
            if (!found) {
                // log.info("will add "+bPort.getUrn());
                added.add(bPort);
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
        List<PortAdjcy> added = new ArrayList<>();
        List<PortAdjcy> modified = new ArrayList<>();
        List<PortAdjcy> removed = new ArrayList<>();
        List<PortAdjcy> unchanged = new ArrayList<>();

        for (PortAdjcy aAdjcy : alpha) {
            // log.info("verifying: "+ aAdjcy.getA().getUrn()+ " -- "+aAdjcy.getZ().getUrn());

            String a_a_urn = aAdjcy.getA().getUrn();
            String a_z_urn = aAdjcy.getZ().getUrn();
            boolean found = false;
            boolean changed = false;
            for (PortAdjcy bAdjcy : beta) {
                String b_a_urn = bAdjcy.getA().getUrn();
                String b_z_urn = bAdjcy.getZ().getUrn();
                if (a_a_urn.equals(b_a_urn) && a_z_urn.equals(b_z_urn)) {
                    found = true;
                    if (!aAdjcy.getMetrics().equals(bAdjcy.getMetrics())) {
                        changed = true;
                    }
                }
            }
            if (found) {
                if (changed) {
                    // log.info("will modify "+aAdjcy.getA().getUrn()+ " -- "+aAdjcy.getZ().getUrn());
                    modified.add(aAdjcy);
                } else {
                    unchanged.add(aAdjcy);
                }
            } else {
                // log.info("will remove "+aAdjcy.getA().getUrn()+ " -- "+aAdjcy.getZ().getUrn());
                removed.add(aAdjcy);
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
                added.add(bAdjcy);
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
