package net.es.oscars.spec.ent;

import lombok.*;
import net.es.oscars.dto.pss.EthPipeType;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanPipeE {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private VlanJunctionE aJunction;

    @OneToOne(cascade = CascadeType.ALL)
    private VlanJunctionE zJunction;

    @NonNull
    private Integer azMbps;

    @NonNull
    private Integer zaMbps;

    @NonNull
    @ElementCollection
    private List<String> azERO;

    @NonNull
    @ElementCollection
    private List<String> zaERO;

    @NonNull
    private EthPipeType pipeType;


    @ElementCollection
    private Set<String> resourceIds;

}