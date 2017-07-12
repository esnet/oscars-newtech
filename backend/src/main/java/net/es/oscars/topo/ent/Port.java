package net.es.oscars.topo.ent;

import lombok.*;
import net.es.oscars.topo.beans.IntRange;
import net.es.oscars.topo.enums.Layer;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
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