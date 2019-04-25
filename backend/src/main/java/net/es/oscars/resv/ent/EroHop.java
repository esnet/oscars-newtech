package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EroHop {
    @JsonCreator
    public EroHop(@JsonProperty("urn") @NonNull String urn) {
        this.urn = urn;
    }

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;


    @NonNull
    private String urn;


}
