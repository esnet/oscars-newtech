package net.es.oscars.topo.ent;

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
@AllArgsConstructor
@NoArgsConstructor
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
    private String ipv4Address;

    private String ipv6Address;

    @ElementCollection
    @CollectionTable
    private Set<IntRange> reservableVlans = new HashSet<>();

    @ElementCollection
    @CollectionTable
    private Set<Layer> capabilities = new HashSet<>();

    @NonNull
    @OneToMany (cascade = CascadeType.ALL)
    private Set<Port> ports = new HashSet<>();


}