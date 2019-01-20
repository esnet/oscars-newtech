package net.es.oscars.app.beans;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@Slf4j
@Data
@Builder
public class Delta<T> {
    Map<String, T> added;
    Map<String, T> removed;
    Map<String, T> modified;
    Map<String, T> unchanged;

    public Set<String> added() { return this.added.keySet(); }
    public Set<String> removed() {
        return this.removed.keySet();
    }
    public Set<String> modified() {
        return this.modified.keySet();
    }
    public Set<String> unchanged() {
        return this.unchanged.keySet();
    }

}
