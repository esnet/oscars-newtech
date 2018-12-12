package net.es.oscars.snp.svc;

import net.es.oscars.app.exc.PSSException;
import net.es.oscars.resv.ent.Components;
import net.es.oscars.snp.beans.CmpDelta;

import java.util.Set;

// calculating what needs to change in order to perform the function
public interface CmpDeltaAPI {

    CmpDelta build(String deviceUrn, Components cmp) throws PSSException;

    CmpDelta dismantle(String deviceUrn, Components cmp) throws PSSException;

    // unsupported at the moment
    // CmpDelta changePath(String pathId, Path newPath, Components cmp) throws SnippetException;

    // unsupported at the moment
    // Delta<VlanJunction> addFixture(String junctionId, Components cmp) throws SnippetException

    // unsupported at the moment
    // Delta<VlanJunction> removeFixture(String junctionId, Components cmp) throws SnippetException;

    CmpDelta setIpv4Addresses(String deviceUrn, Components cmp, Set<String> ipAddresses)
            throws PSSException;

    // unsupported at the moment
    CmpDelta setIpv6Addresses(String deviceUrn, Components cmp, Set<String> ipAddresses)
            throws PSSException;
}
