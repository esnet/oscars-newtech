package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
@Entity
public class Vlan {

    @JsonCreator
    public Vlan(@JsonProperty("connectionId") String connectionId,
                @JsonProperty("vlanExpression") @NonNull String vlanExpression,
                @JsonProperty("schedule") Schedule schedule,
                @JsonProperty("urn") String urn,
                @JsonProperty("vlan") Integer vlan) {
        this.connectionId = connectionId;
        this.urn = urn;
        this.vlan = vlan;
        this.vlanExpression = vlanExpression;
        this.schedule = schedule;
    }

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    // mandatory; must be set even if empty
    @NonNull
    private String vlanExpression;


    // these will be populated by the system when designing
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String connectionId;

    @ManyToOne
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Schedule schedule;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String urn;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Integer vlan;

}
