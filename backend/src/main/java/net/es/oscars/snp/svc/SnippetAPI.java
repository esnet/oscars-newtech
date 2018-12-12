package net.es.oscars.snp.svc;

import net.es.oscars.app.exc.SnippetException;
import net.es.oscars.snp.beans.CmpDelta;
import net.es.oscars.snp.ent.ConfigSnippet;

import java.util.Set;

public interface SnippetAPI {
    Set<ConfigSnippet> generateNeededSnippets(String deviceUrn, CmpDelta delta) throws SnippetException;
    boolean validateDeltas(CmpDelta delta) throws SnippetException;

    void hello();

}
