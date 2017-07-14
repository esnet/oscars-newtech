package net.es.oscars.topo.ent;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.enums.Layer;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Data
@Entity
@Builder
@AllArgsConstructor(suppressConstructorProperties=true)
@NoArgsConstructor
@EqualsAndHashCode(exclude="device")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
                  property = "urn")
public class Port {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String urn;

    @Column
    @NonNull
    private Integer reservableIngressBw;

    @NonNull
    @ManyToOne
    @JsonBackReference(value="device")
    private Device device;

    @Column
    @NonNull
    private Integer reservableEgressBw;

    private String ipv4Address;

    private String ipv6Address;

    @ElementCollection
    @CollectionTable
    private Set<IntRange> reservableVlans = new HashSet<>();

    @ElementCollection
    @CollectionTable
    private Set<Layer> capabilities = new HashSet<>();



}