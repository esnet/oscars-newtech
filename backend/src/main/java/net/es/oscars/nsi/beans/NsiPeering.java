package net.es.oscars.nsi.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NsiPeering {
    private String capacity;
    private String vlan;
    private NsiPeerEdge in;
    private NsiPeerEdge out;
}
