package net.es.oscars.topo.beans;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;

import java.util.List;
import java.util.Map;

@Slf4j
@Data
@Builder
public class Topology {
    private Map<String, Device> devices;
    private Map<String, Port> ports;
    private List<PortAdjcy> adjcies;

}
