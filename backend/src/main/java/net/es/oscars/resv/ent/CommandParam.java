package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import net.es.oscars.topo.enums.CommandParamType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties=true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "refId")
@Entity
public class CommandParam {
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NonNull
    private String urn;

    @NonNull
    @ManyToOne
    private Schedule schedule;

    @NonNull
    private String connectionId;

    @NonNull
    private CommandParamType paramType;

    private String refId;

    private String intent;

    @NonNull
    private Integer resource;

}
