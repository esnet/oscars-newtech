package net.es.oscars.topo.ent;

import lombok.Data;
import lombok.NonNull;
import net.es.oscars.dto.topo.Layer;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
@Entity
public class EIfce {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String urn;

    private Integer reservableBw;
    private IfceType type = IfceType.PORT;

    @ElementCollection
    @CollectionTable
    private List<EIntRange> reservableVlans;


    @ElementCollection
    @CollectionTable
    private Set<Layer> capabilities = new HashSet<>();

    public EIfce() {

    }

    public EIfce(String urn) {
        this.urn = urn;
    }

}