package net.es.oscars.topo.beans;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.app.beans.Delta;
import net.es.oscars.topo.ent.Device;
import net.es.oscars.topo.ent.Port;
import net.es.oscars.topo.ent.PortAdjcy;

@Slf4j
@Data
@Builder
public class VersionDelta {
    private boolean changed;
    private Delta<Device> deviceDelta;
    private Delta<Port> portDelta;
    private Delta<PortAdjcy> adjcyDelta;

}
