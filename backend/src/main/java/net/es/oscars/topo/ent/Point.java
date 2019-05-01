package net.es.oscars.topo.ent;

import lombok.*;
import net.es.oscars.topo.beans.TopoUrn;
import net.es.oscars.topo.enums.Layer;
import org.jgrapht.graph.DefaultWeightedEdge;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Point {
    @Id
    @GeneratedValue
    private Long id;

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
    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null ) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Point other = (Point) obj;
        return id != null && id.equals(other.getId());
    }

}
