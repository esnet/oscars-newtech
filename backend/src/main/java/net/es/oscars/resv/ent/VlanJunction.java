package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor(suppressConstructorProperties = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "refId")
public class VlanJunction {
    @Id
    @JsonIgnore
    @GeneratedValue
    private Long id;

    // always have a refId; junctions really only exist to be referred to
    @NonNull
    private String refId;

    // mandatory; a junction is always associated with a specific device
    @NonNull
    private String deviceUrn;


    // leave the following empty when requesting
    @ManyToOne
    private Schedule schedule;

    private String connectionId;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<CommandParam> commandParams;

    // really only for reserving a vlan at a switch
    @ManyToOne(cascade = CascadeType.ALL)
    private Vlan vlan;

}
