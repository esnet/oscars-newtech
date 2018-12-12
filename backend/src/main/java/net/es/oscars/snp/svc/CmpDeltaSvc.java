package net.es.oscars.snp.svc;

import net.es.oscars.app.beans.Delta;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.resv.ent.Components;
import net.es.oscars.resv.ent.VlanFixture;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.resv.ent.VlanPipe;
import net.es.oscars.snp.beans.CmpDelta;
import net.es.oscars.snp.ent.ConfigSnippet;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Service
public class CmpDeltaSvc implements CmpDeltaAPI {

    public CmpDelta build(String deviceUrn, Components cmp) throws PSSException {
        CmpDelta result = CmpDelta.newEmptyDelta();
        Delta<VlanJunction> junctionDelta = result.getJunctionDelta();

        for (VlanJunction j : cmp.getJunctions()) {
            junctionDelta.getAdded().put(j.getRefId(), j);
        }
        /// fix and pipe

        result.setJunctionDelta(junctionDelta);

        return result;

    }

    public CmpDelta dismantle(String deviceUrn, Components cmp) throws PSSException {
        CmpDelta result = CmpDelta.newEmptyDelta();
        Delta<VlanJunction> junctionDelta = result.getJunctionDelta();

        for (VlanJunction j : cmp.getJunctions()) {
            junctionDelta.getRemoved().put(j.getRefId(), j);
        }
        /// fix and pipe

        result.setJunctionDelta(junctionDelta);

        return result;


    }


    public CmpDelta setIpv6Addresses(String deviceUrn, Components cmp, Set<String> ipAddresses) throws PSSException {
        // TODO: implement me
        // TODO: check for ip addr validity etc
        return null;


    }

    public CmpDelta setIpv4Addresses(String deviceUrn, Components cmp, Set<String> ipAddresses) throws PSSException {
        // TODO: check for ip addr validity etc


        CmpDelta result = CmpDelta.newEmptyDelta();
        Delta<VlanJunction> junctionDelta = result.getJunctionDelta();

        cmp.getJunctions().forEach(j -> {
            if (j.getRefId().equals(deviceUrn)) {
                if (!j.getIpv4Addresses().equals(ipAddresses)) {
                    VlanJunction modifiedJunction = VlanJunction.builder()
                            .connectionId(j.getConnectionId())
                            .deviceUrn(j.getDeviceUrn())
                            .commandParams(j.getCommandParams())
                            .ipv4Addresses(j.getIpv4Addresses())
                            .build();

                    junctionDelta.getModified().put(modifiedJunction.getRefId(), modifiedJunction);

                } else {
                    junctionDelta.getUnchanged().put(j.getRefId(), j);
                }

            } else {
                junctionDelta.getUnchanged().put(j.getRefId(), j);
            }
        });

        result.setJunctionDelta(junctionDelta);
        return result;
    }

}
