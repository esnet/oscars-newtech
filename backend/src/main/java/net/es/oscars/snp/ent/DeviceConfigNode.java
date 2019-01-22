package net.es.oscars.snp.ent;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.es.oscars.snp.beans.ConfigNodeType;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@JsonIdentityInfo(generator=ObjectIdGenerators.StringIdGenerator.class, property="nodeId")
public class DeviceConfigNode {
    public String toString() {
        return "";
    }

    private String nodeId;

    @NonNull
    private ConfigNodeType type;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<DeviceConfigNode> upstream;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<DeviceConfigNode> downstream;
}
