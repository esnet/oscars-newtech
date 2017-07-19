package net.es.oscars.topo.pop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsistencyException extends Exception {
    private Map<ConsistencyError, List<String >> errorMap = new HashMap<>();
    public Map<ConsistencyError, List<String >> getErrorMap() {
        return this.errorMap;
    }
    public ConsistencyException(String msg) {
        super(msg);
    }
}
