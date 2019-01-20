package net.es.oscars.snp.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.beans.Delta;
import net.es.oscars.app.exc.PSSException;
import net.es.oscars.resv.ent.*;
import net.es.oscars.snp.beans.CmpDelta;
import net.es.oscars.snp.ent.ConfigSnippet;
import org.apache.commons.validator.routines.InetAddressValidator;
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

//        log.info(String.valueOf(result));

        return result;
    }

    public CmpDelta addJunction(String refId, String connectionId, Components cmp) throws PSSException {
        CmpDelta result = CmpDelta.newEmptyDelta();
        Delta<VlanJunction> junctionDelta = result.getJunctionDelta();

        VlanJunction addedJunction = VlanJunction.builder()
                .refId(refId)
                .connectionId(connectionId)
                .deviceUrn("A")
                .build();

        junctionDelta.getAdded().put(addedJunction.getConnectionId(), addedJunction);
        result.setJunctionDelta(junctionDelta);

        log.info(String.valueOf(result));

        return result;
    }

    public CmpDelta addFixture(String junctionId, String connectionId, Components cmp) throws PSSException {
        CmpDelta result = CmpDelta.newEmptyDelta();
        Delta<VlanFixture> fixtureDelta = result.getFixtureDelta();

        VlanJunction requiredJunction = null;
        for (VlanJunction j : cmp.getJunctions()) {
            if (j.getRefId().equals(junctionId)) {
                requiredJunction = j;
            }
        }

        Vlan vlan = Vlan.builder()
                .connectionId(connectionId)
                .urn("A:1")
                .vlanId(150)
                .build();

        VlanFixture addedFixture = VlanFixture.builder()
                .junction(requiredJunction)
                .connectionId(connectionId)
                .portUrn("A:1")
                .ingressBandwidth(100)
                .egressBandwidth(100)
                .strict(false)
                .vlan(vlan)
                .build();

        fixtureDelta.getAdded().put(addedFixture.getConnectionId(), addedFixture);

        result.setFixtureDelta(fixtureDelta);

//        log.info(String.valueOf(result));
        return result;
    }

    // TODO
    public CmpDelta addPipe(String a, Components cmp) throws PSSException {
        CmpDelta result = CmpDelta.newEmptyDelta();
        Delta<VlanPipe> pipeDelta = result.getPipeDelta();

//        VlanPipe addedPipe = VlanPipe.builder()
//                .a(jmap.get(p.getA().getDeviceUrn()))
//                .z(jmap.get(p.getZ().getDeviceUrn()))
//                .azBandwidth(p.getAzBandwidth())
//                .zaBandwidth(p.getZaBandwidth())
//                .connectionId(p.getConnectionId())
//                .schedule(sch)
//                .protect(p.getProtect())
//                .azERO(copyEro(p.getAzERO()))
//                .zaERO(copyEro(p.getZaERO()))
//                .build();
//
//        pipeDelta.getAdded().put(addedPipe.getConnectionId(), addedPipe);
//        result.setPipeDelta(pipeDelta);

//        log.info(String.valueOf(result));
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

        // InetAddressValidator validator ...

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
