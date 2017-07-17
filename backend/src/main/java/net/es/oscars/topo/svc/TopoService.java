package net.es.oscars.topo.svc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.beans.TopoAdjcy;
import net.es.oscars.topo.beans.TopoException;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.enums.UrnType;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@Data
public class TopoService {
    private Map<String, TopoUrn> topoUrnMap;
    private List<TopoAdjcy> topoAdjcies;

    public void updateTopo(List<Device> devices, List<PortAdjcy> portAdjcies) throws TopoException  {
        this.topoUrnMap = this.urnsFromDevices(devices);

        this.topoAdjcies = new ArrayList<>();
        this.topoAdjcies.addAll(adjciesFromDevices(devices));
        this.topoAdjcies.addAll(transformToTopoAdjcies(portAdjcies));

    }


    private Map<String, TopoUrn> urnsFromDevices(List<Device> devices) throws TopoException {
        Map<String, TopoUrn> urns = new HashMap<>();

        devices.forEach(d -> {
            // make a copy of the IntRanges otherwise it'd be set by reference
            Set<IntRange> drv = new HashSet<>();
            drv.addAll(IntRange.mergeIntRanges(d.getReservableVlans()));

            TopoUrn deviceUrn = TopoUrn.builder()
                    .urn(d.getUrn())
                    .urnType(UrnType.DEVICE)
                    .device(d)
                    .reservableVlans(drv)
                    .capabilities(d.getCapabilities())
                    .build();
            urns.put(d.getUrn(), deviceUrn);


            d.getPorts().forEach(p -> {
                // make a copy of the IntRanges otherwise it'd be set by reference
                Set<IntRange> prv = new HashSet<>();
                prv.addAll(IntRange.mergeIntRanges(p.getReservableVlans()));

                TopoUrn portUrn = TopoUrn.builder()
                        .urn(p.getUrn())
                        .urnType(UrnType.PORT)
                        .capabilities(p.getCapabilities())
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
                TopoUrn zUrn = this.topoUrnMap.get(pa.getA().getUrn());

                TopoAdjcy adjcy = TopoAdjcy.builder().a(aUrn).z(zUrn).metrics(pa.getMetrics()).build();
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
