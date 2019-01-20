package net.es.oscars.snp.ent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.resv.ent.CommandParam;
import net.es.oscars.resv.ent.Schedule;
import net.es.oscars.resv.ent.Vlan;
import net.es.oscars.resv.ent.VlanJunction;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class DeviceConfigState {
    @JsonCreator
    public DeviceConfigState(@JsonProperty("urn") @NonNull String urn,
                             @JsonProperty("model") @NonNull DeviceModel model,
                             @JsonProperty("connectionConfigNodes") HashMap<String, DeviceConfigNode> connectionConfigNodes) {
        this.urn = urn;
        this.model = model;
        this.connectionConfigNodes = connectionConfigNodes;
    }

    @NonNull
    private String urn;

    @NonNull
    private DeviceModel model;

    @OneToMany(cascade = CascadeType.ALL)
    private HashMap<String, DeviceConfigNode> connectionConfigNodes;

}
