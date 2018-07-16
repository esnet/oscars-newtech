package net.es.oscars.topo.beans;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;

@Slf4j
@Data
@Builder
public class ConsistencyReport {
    private Instant generated;
    private Instant topologyUpdated;
    private Map<String, Set<String>> issuesByUrn;
    private Map<String, Set<String>> issuesByConnectionId;
    public void addConnectionError(String connectionId, String error) {
        if (!issuesByConnectionId.containsKey(connectionId)) {

            issuesByConnectionId.put(connectionId, new HashSet<>());
        }
        issuesByConnectionId.get(connectionId).add(error);
    }
    public void addUrnError(String urn, String error) {
        if (!issuesByUrn.containsKey(urn)) {
            issuesByUrn.put(urn, new HashSet<>());
        }
        issuesByUrn.get(urn).add(error);

    }
}
