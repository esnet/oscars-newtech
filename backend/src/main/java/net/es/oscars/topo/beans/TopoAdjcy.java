package net.es.oscars.topo.beans;

import lombok.*;
import net.es.oscars.topo.enums.Layer;

import java.util.HashMap;
import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopoAdjcy {
    private TopoUrn a;

    private TopoUrn z;

    private Map<Layer, Long> metrics = new HashMap<>();

}
