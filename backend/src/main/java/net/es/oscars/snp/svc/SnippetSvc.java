package net.es.oscars.snp.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.exc.SnippetException;
import net.es.oscars.snp.beans.CmpDelta;
import net.es.oscars.snp.ent.ConfigSnippet;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class SnippetSvc implements SnippetAPI {

    public void updateSnippetStatus(Set<ConfigSnippet> snippets) {
        // verify consistency & do DB write
    }

    public Set<ConfigSnippet> generateNeededSnippets(String deviceUrn, CmpDelta delta) throws SnippetException {
        // Validate Deltas
        // this.validateDeltas(delta);

        Set<ConfigSnippet> result = new HashSet<>();

        // decide what is needed for each delta added / removed / modified
        // 1st implementation only does:
        //      1. build a new junction & pipes & fixtures,
        //      2. add / remove an interface (i.e. modify junction deltas)
        //      3. remove everything

        ConfigSnippet ifceConfigSnippet = null;

        // TODO: implement me
        result.add(ifceConfigSnippet);

        return result;
    }

    public boolean validateDeltas(CmpDelta delta) throws SnippetException {
        // TODO: implement me
        return true;
    }

}
