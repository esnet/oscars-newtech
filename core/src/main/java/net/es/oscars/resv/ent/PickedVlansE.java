package net.es.oscars.resv.ent;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickedVlansE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String connectionId;

    @NonNull
    private Instant holdUntil;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<ReservedVlanE> reservedVlans;

}
