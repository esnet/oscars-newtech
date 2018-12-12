package net.es.oscars.resv.svc;

import net.es.oscars.app.beans.Delta;
import net.es.oscars.pss.ent.ConfigSnippet;
import net.es.oscars.resv.ent.Components;
import net.es.oscars.resv.ent.VlanFixture;
import net.es.oscars.resv.ent.VlanJunction;
import net.es.oscars.resv.ent.VlanPipe;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Service
public class SnippetSvc {

    public Delta<VlanJunction> build(String junctionId, Components cmp) {
        // populate added...
        return null;
    }

    public Delta<VlanJunction> dismantle(String junctionId, Components cmp) {
        return null;
        // populate removed...
    }

    public Delta<VlanJunction> addFixture(String junctionId, Components cmp) {
        return null;
        // populate added & unchanged
    }

    public Delta<VlanJunction> removeFixture(String junctionId, Components cmp) {
        return null;
        // populate removed & unchanged
    }

    public Delta<VlanJunction> removeIpv4Address(String junctionId, Components cmp) {
        return null;
        // populate removed & unchanged
    }


    public Delta<VlanJunction> addIpv4Address(String addr, String junctionId, Components cmp) {
        Delta<VlanJunction> delta = Delta.<VlanJunction>builder()
                .added(new HashMap<>())
                .removed(new HashMap<>())
                .modified(new HashMap<>())
                .unchanged(new HashMap<>())
                .build();

        cmp.getJunctions().forEach(j -> {
            if (j.getRefId().equals(junctionId)) {
                if (!j.getIpv4Addresses().contains(addr)) {
                    VlanJunction modifiedJunction = VlanJunction.builder()
                            .connectionId(j.getConnectionId())
                            .deviceUrn(j.getDeviceUrn())
                            .commandParams(j.getCommandParams())
                            .ipv4Addresses(j.getIpv4Addresses())
                            .build();

                    delta.getModified().put(modifiedJunction.getRefId(), modifiedJunction);

                } else {
                    delta.getUnchanged().put(j.getRefId(), j);
                }

            } else {
                delta.getUnchanged().put(j.getRefId(), j);
            }
        });
        return delta;
    }

    public void updateSnippetStatus(Set<ConfigSnippet> snippets) {
        // verify consistency & do DB write

    }

    public Set<ConfigSnippet> generateNeededSnippets(String deviceUrn,
                                                     Delta<VlanJunction> junctionDelta,
                                                     Delta<VlanFixture> fixtureDelta,
                                                     Delta<VlanPipe> pipeDelta) {

        // first do a this.validateDeltas() to double check :)

        Set<ConfigSnippet> result = new HashSet<>();

        /// decide what is needed for each delta added / removed / modified
        //  1st implementation only does:
        // 1. build a new junction & pipes & fixtures,
        // 2. add / remove an interface (i.e. modify junction deltas)
        // 3. remove everything
        ConfigSnippet ifceConfigSnippet = null;
        result.add(ifceConfigSnippet);
        //
        return result;
    }

    public boolean validateDeltas(String deviceUrn,
                                  Delta<VlanJunction> junctionDelta,
                                  Delta<VlanFixture> fixtureDelta,
                                  Delta<VlanPipe> pipeDelta) {
        // 1st implementation only allows:
        // 1. build a new junction & pipes & fixtures,
        // 2. add / remove an interface (i.e. modify junction deltas)
        // 3. remove everything
        //
        return false;
    }


}
