package net.es.oscars.topo.beans;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@Slf4j
@Data
@Builder
public class Delta<T> {
    private Map<String, T> added;
    private Map<String, T> removed;
    private Map<String, T> modified;
    private Map<String, T> unchanged;

    public Set<String> addedUrns() {
        return this.added.keySet();
    }
    public Set<String> removedUrns() {
        return this.removed.keySet();
    }
    public Set<String> modifiedUrns() {
        return this.modified.keySet();
    }
    public Set<String> unchangedUrns() {
        return this.unchanged.keySet();
    }

}
