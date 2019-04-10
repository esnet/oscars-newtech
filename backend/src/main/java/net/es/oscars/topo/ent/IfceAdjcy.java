package net.es.oscars.topo.ent;

import lombok.*;
import net.es.oscars.topo.enums.Layer;

import javax.persistence.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@EqualsAndHashCode
public class IfceAdjcy {
    @Id
    @GeneratedValue
    private Long id;

    private String aIfceUrn;

    @ManyToOne
    private String zIfceUrn;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable
    private Map<Layer, Long> metrics = new HashMap<>();

    @ManyToOne
    private Version version;

    public String getUrn() {
        return aIfceUrn+" - "+zIfceUrn;
    }

    public Integer minimalReservableBandwidth() {
        Set<Integer> reservableBandwidths = new HashSet<>();
        reservableBandwidths.add(this.a.getPort().getReservableEgressBw());
        reservableBandwidths.add(this.z.getPort().getReservableEgressBw());
        reservableBandwidths.add(this.a.getPort().getReservableIngressBw());
        reservableBandwidths.add(this.z.getPort().getReservableIngressBw());
        // we can get() because the stream is not empty
        return reservableBandwidths.stream().min(Integer::compare).get();
    }
    public String toString() {
        return this.getClass().getSimpleName() + "-" +this.getUrn()+" "+ getId();
    }


}
