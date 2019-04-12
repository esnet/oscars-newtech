package net.es.oscars.topo.ent;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.enums.DeviceType;
import net.es.oscars.topo.enums.Layer;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude={"capabilities", "reservableVlans", "ports", "id"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
                  property = "urn")
public class Device {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Version version;

    @NonNull
    @Column(unique = true)
    @NaturalId
    private String urn;

    @NonNull
    @Column
    private DeviceModel model;

    @NonNull
    @Column
    private Integer locationId = 0;

    @NonNull
    @Column
    private Double latitude = 0D;

    @NonNull
    @Column
    private Double longitude = 0D;

    @NonNull
    @Column
    private String location = "";

    @NonNull
    @Column
    private DeviceType type;

    @NonNull
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

    @NonNull
    @ElementCollection(fetch = FetchType.EAGER)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<Port> ports = new HashSet<>();

    public String toString() {
        return this.getClass().getSimpleName() + "-" + getUrn();
    }
}