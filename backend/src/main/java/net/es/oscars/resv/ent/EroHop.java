package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import net.es.oscars.resv.enums.EroHopType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
@Entity
public class EroHop {
    @JsonCreator
    public EroHop(@JsonProperty("connectionId") @NonNull String urn,
                  @JsonProperty("archived") EroHopType type) {
        this.urn = urn;
        this.type = type;
    }

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    private EroHopType type;

    @NonNull
    private String urn;


}
