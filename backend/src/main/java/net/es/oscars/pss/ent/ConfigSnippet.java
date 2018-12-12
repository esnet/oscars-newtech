package net.es.oscars.pss.ent;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class ConfigSnippet {
    private SnippetStatus status;
    private String dismantleConfigText;
    private String buildConfigText;
    private Set<ConfigSnippet> dependsOn;
    private Set<ConfigSnippet> dependOnThis;


}
