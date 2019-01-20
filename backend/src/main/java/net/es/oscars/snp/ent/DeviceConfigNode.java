package net.es.oscars.snp.ent;

import com.fasterxml.jackson.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.snp.beans.ConfigNodeType;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import java.util.HashMap;
import java.util.Set;

@Data
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "nodeId")
public class DeviceConfigNode {
    @JsonCreator
    public DeviceConfigNode(@JsonProperty("nodeId") @NonNull String nodeId,
                            @JsonProperty("type") @NonNull ConfigNodeType type,
                            @JsonProperty("upstream") Set<DeviceConfigNode> upstream,
                            @JsonProperty("downstream") Set<DeviceConfigNode> downstream) {
        this.nodeId = nodeId;
        this.type = type;
        this.upstream = upstream;
        this.downstream = downstream;
    }

    @NonNull
    private String nodeId;

    @NonNull
    private ConfigNodeType type;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<DeviceConfigNode> upstream;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<DeviceConfigNode> downstream;
}
