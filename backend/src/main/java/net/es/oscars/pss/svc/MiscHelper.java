package net.es.oscars.pss.svc;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.params.MplsHop;
import net.es.oscars.dto.pss.params.MplsPath;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.enums.UrnType;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class MiscHelper {

    @Autowired
    private TopoService topoService;


    public MplsPath mplsPathBuilder(VlanPipe pipe) throws PSSException {
        Map<String, TopoUrn> urnMap = topoService.getTopoUrnMap();
        String name = pipe.getConnectionId() + "-" + pipe.getZ();
        List<MplsHop> hops = new ArrayList<>();
        Integer order = 0;

        for (EroHop eroHop : pipe.getAzERO()) {
            TopoUrn topoUrn = urnMap.get(eroHop.getUrn());
            if (topoUrn != null) {
                if (topoUrn.getUrnType().equals(UrnType.PORT)) {
                    String addr = topoUrn.getPort().getIpv4Address();

                    MplsHop hop = MplsHop.builder()
                            .address(addr)
                            .order(order)
                            .build();
                    order += 1;
                    hops.add(hop);
                }

            } else {
                throw new PSSException("could not locate topo URN for " + eroHop.getUrn());
            }
        }

        return MplsPath.builder()
                .name(name)
                .hops(hops)
                .build();
    }
}