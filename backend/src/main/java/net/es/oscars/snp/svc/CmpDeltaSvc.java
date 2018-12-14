package net.es.oscars.snp.svc;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CmpDeltaSvc implements CmpDeltaAPI {

    public CmpDelta build(String deviceUrn, Components cmp) throws PSSException {
        CmpDelta result = CmpDelta.newEmptyDelta();
        Delta<VlanJunction> junctionDelta = result.getJunctionDelta();
        Delta<VlanFixture> fixtureDelta = result.getFixtureDelta();
        Delta<VlanPipe> pipeDelta = result.getPipeDelta();

        for (VlanJunction j : cmp.getJunctions()) {
            junctionDelta.getAdded().put(j.getRefId(), j);
        }

        for (VlanFixture f : cmp.getFixtures()) {
            fixtureDelta.getAdded().put(f.getPortUrn(), f);
        }

        for (VlanPipe p : cmp.getPipes()) {
            pipeDelta.getAdded().put(p.getConnectionId(), p);
        }

        result.setJunctionDelta(junctionDelta);
        result.setFixtureDelta(fixtureDelta);
        result.setPipeDelta(pipeDelta);

        log.info(String.valueOf(result));

        return result;
    }

    public CmpDelta dismantle(String deviceUrn, Components cmp) throws PSSException {
        CmpDelta result = CmpDelta.newEmptyDelta();
        Delta<VlanJunction> junctionDelta = result.getJunctionDelta();
        Delta<VlanFixture> fixtureDelta = result.getFixtureDelta();
        Delta<VlanPipe> pipeDelta = result.getPipeDelta();

        for (VlanJunction j : cmp.getJunctions()) {
            junctionDelta.getRemoved().put(j.getRefId(), j);
        }

        for (VlanFixture f : cmp.getFixtures()) {
            fixtureDelta.getRemoved().put(f.getPortUrn(), f);
        }

        for (VlanPipe p : cmp.getPipes()) {
            pipeDelta.getRemoved().put(p.getConnectionId(), p);
        }

        result.setJunctionDelta(junctionDelta);
        result.setFixtureDelta(fixtureDelta);
        result.setPipeDelta(pipeDelta);

        log.info(String.valueOf(result));

        return result;
    }


    public CmpDelta setIpv6Addresses(String deviceUrn, Components cmp, Set<String> ipAddresses) throws PSSException {
        // TODO: check for ip address validity etc
        // ipAddresses.forEach(this::validIP);

        CmpDelta result = CmpDelta.newEmptyDelta();
        Delta<VlanJunction> junctionDelta = result.getJunctionDelta();

        cmp.getJunctions().forEach(j -> {
            if (j.getRefId().equals(deviceUrn)) {
                if (!String.valueOf(j.getIpv6Addresses()).equals(String.valueOf(ipAddresses))) {

                    VlanJunction modifiedJunction = VlanJunction.builder()
                            .refId(j.getRefId())
                            .connectionId(j.getConnectionId())
                            .deviceUrn(j.getDeviceUrn())
                            .commandParams(j.getCommandParams())
                            .ipv6Addresses(ipAddresses)
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

    public CmpDelta setIpv4Addresses(String deviceUrn, Components cmp, Set<String> ipAddresses) throws PSSException {
        // TODO: check for ip address validity etc
        // ipAddresses.forEach(this::validIP);

        CmpDelta result = CmpDelta.newEmptyDelta();
        Delta<VlanJunction> junctionDelta = result.getJunctionDelta();

        cmp.getJunctions().forEach(j -> {
            if (j.getRefId().equals(deviceUrn)) {
                if (!String.valueOf(j.getIpv4Addresses()).equals(String.valueOf(ipAddresses))) {

                    VlanJunction modifiedJunction = VlanJunction.builder()
                            .refId(j.getRefId())
                            .connectionId(j.getConnectionId())
                            .deviceUrn(j.getDeviceUrn())
                            .commandParams(j.getCommandParams())
                            .ipv4Addresses(ipAddresses)
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
