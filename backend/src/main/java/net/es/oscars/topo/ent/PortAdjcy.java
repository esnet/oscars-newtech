package net.es.oscars.topo.ent;

import lombok.*;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.enums.Layer;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class PortAdjcy {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @ManyToOne
    private Port a;

    @NonNull
    @ManyToOne
    private Port z;

    @ElementCollection
    private Map<Layer, Long> metrics = new HashMap<>();

}
