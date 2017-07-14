package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import net.es.oscars.resv.enums.EthFixtureType;

import javax.persistence.*;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VlanFixture {
    @Id
    @JsonIgnore
    @GeneratedValue
    private Long id;


    // mandatory; a fixture is always associated with a junction
    @NonNull
    @ManyToOne
    private VlanJunction junction;

    // mandatory; a fixture is always associated with a specific port
    @NonNull
    private String portUrn;

    // mandatory; a fixture always has ingress and egress bws (even if 0)
    @NonNull
    private Integer ingressBandwidth;

    @NonNull
    private Integer egressBandwidth;

    // mandatory; a fixture always has a vlan specification associated with it
    @ManyToOne(cascade = CascadeType.ALL)
    @NonNull
    private Vlan vlan;



    // leave the following empty when requesting
    private String connectionId;

    @ManyToOne
    private Schedule schedule;

    private EthFixtureType ethFixtureType;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<CommandParam> commandParams;


}
