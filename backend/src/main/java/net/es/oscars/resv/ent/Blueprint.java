package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Blueprint {

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NonNull
    private String connectionId;

    @OneToOne(cascade = CascadeType.ALL)
    private Schedule schedule;

    @NonNull
    @OneToMany(cascade = CascadeType.ALL)
    private List<VlanJunction> junctions;

    @NonNull
    @OneToMany(cascade = CascadeType.ALL)
    private List<VlanFixture> fixtures;

    @NonNull
    @OneToMany(cascade = CascadeType.ALL)
    private List<VlanPipe> pipes;


}
