package net.es.oscars.topo.ent;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.enums.Layer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


@Data
@Entity
@Builder
@ToString(exclude={"device", "adjciesWhereA", "adjciesWhereZ"})
@AllArgsConstructor(suppressConstructorProperties=true)
@NoArgsConstructor
@EqualsAndHashCode(exclude={"device", "adjciesWhereA", "adjciesWhereZ"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
                  property = "urn")
public class Port {
    @Id
    @GeneratedValue
    private Long id;


    @ManyToOne
    private Version version;


    @NonNull
    @Column(unique = true)
    private String urn;

    @Basic
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ArrayList<String> tags;

    @NonNull
    @ManyToOne
    @JsonBackReference(value="device")
    private Device device;

    @Column
    @NonNull
    private Integer reservableIngressBw;

    @Column
    @NonNull
    private Integer reservableEgressBw;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String ipv4Address;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String ipv6Address;

    @ElementCollection
    @CollectionTable
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<IntRange> reservableVlans = new HashSet<>();

    @ElementCollection
    @CollectionTable
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<Layer> capabilities = new HashSet<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OneToMany(mappedBy = "a", cascade = CascadeType.ALL)
    private Set<PortAdjcy> adjciesWhereA = new HashSet<>();

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OneToMany(mappedBy = "z", cascade = CascadeType.ALL)
    private Set<PortAdjcy> adjciesWhereZ = new HashSet<>();


}