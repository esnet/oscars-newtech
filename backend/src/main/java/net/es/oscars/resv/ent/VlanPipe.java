package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanPipe {
    @Id
    @JsonIgnore
    @GeneratedValue
    private Long id;

    @NonNull
    @ManyToOne
    private VlanJunction a;

    @NonNull
    @ManyToOne
    private VlanJunction z;

    @NonNull
    private Integer azBandwidth;
    @NonNull
    private Integer zaBandwidth;

    // EROs are optional
    @OneToMany(cascade = CascadeType.ALL)
    private List<EroHop> azERO;

    @OneToMany(cascade = CascadeType.ALL)
    private List<EroHop> zaERO;

    // leave the following empty when requesting
    private String connectionId;

    @ManyToOne
    private Schedule schedule;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<CommandParam> commandParams;


}
