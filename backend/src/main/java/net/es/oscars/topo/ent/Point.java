package net.es.oscars.topo.ent;

import lombok.*;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.enums.Layer;
import org.jgrapht.graph.DefaultWeightedEdge;

import javax.persistence.Embeddable;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
@EqualsAndHashCode
public class Point {
    private String device;
    private String port;
    private String addr;
    private String ifce;

    public String getPortUrn() {
        return this.device+":"+this.port;
    }

    public String getUrn() {
        if (this.ifce == null || this.ifce.equals("")) {
            return this.getPortUrn();
        }
        return ifce;
    }


}
