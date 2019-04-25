package net.es.oscars.topo.ent;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.enums.Layer;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude={"device", "capabilities", "reservableVlans", "tags", "id"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
                  property = "urn")
public class Port {
    @Id
    @GeneratedValue
    private Long id;


    @ManyToOne
    private Version version;


    @NonNull
    @NaturalId
    @Column(unique = true)
    private String urn;

    @Basic
    @Column(length = 65535)
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
    private String ifce;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String ipv4Address;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String ipv6Address;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<IntRange> reservableVlans = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<Layer> capabilities = new HashSet<>();

    public String toString() {
        return this.getClass().getSimpleName() + "-" + getUrn();
    }

}