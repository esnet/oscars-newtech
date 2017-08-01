package net.es.oscars.topo.ent;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.enums.DeviceType;
import net.es.oscars.topo.enums.Layer;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@AllArgsConstructor(suppressConstructorProperties=true)
@NoArgsConstructor
@EqualsAndHashCode(exclude={"capabilities", "reservableVlans"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
                  property = "urn")
public class Device {



    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String urn;

    @NonNull
    @Column
    private DeviceModel model;

    @NonNull
    @Column
    private DeviceType type;

    @NonNull
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

    @NonNull
    @OneToMany(cascade = CascadeType.ALL)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<Port> ports = new HashSet<>();


}