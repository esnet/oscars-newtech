package net.es.oscars.pss.svc;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.dto.pss.params.MplsHop;
import net.es.oscars.dto.pss.params.MplsPath;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.ent.Port;
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


    public static List<MplsHop>  mplsHops(List<EroHop> hops, TopoService topoService) {

        // eroHops look like this:
        // 0: A
        // 1: A:1/1
        // 2: B:2/1
        // 3: B
        // 4: B:3/2
        // 5: C:4/1
        // 6: C
        // 7: C:8/2
        // 8: Z:2/1
        // 9: Z
        // to make the MPLS path, we take only hops that are index 2 mod 3

        List<MplsHop> mplsHops = new ArrayList<>();

        // output hop order field starts at 1 (not 0)
        Integer order = 1;
        for (int i = 0; i < hops.size(); i++) {
            if (i % 3 == 2) {
                EroHop hop = hops.get(i);
                Port port = topoService.getTopoUrnMap().get(hop.getUrn()).getPort();
                MplsHop mplsHop = MplsHop.builder()
                        .address(port.getIpv4Address())
                        .order(order)
                        .build();
                order = order + 1;
                mplsHops.add(mplsHop);
            }
        }
        return mplsHops;
    }
}