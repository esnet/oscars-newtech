package net.es.oscars.snp.ent;

import lombok.Builder;
import lombok.Data;
import net.es.oscars.snp.beans.SnippetStatus;

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
